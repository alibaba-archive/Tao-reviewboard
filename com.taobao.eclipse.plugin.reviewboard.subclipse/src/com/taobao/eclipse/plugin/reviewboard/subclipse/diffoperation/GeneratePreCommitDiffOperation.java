/*
* (C) 2007-2011 Alibaba Group Holding Limited
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
*
* If you have any question, please contact:千丫 <qianya@taobao.com>
* Authors:智清 <zhiqing.ht@taobao.com>；银时<yinshi.nc@taobao.com>
*
*/
package com.taobao.eclipse.plugin.reviewboard.subclipse.diffoperation;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_AUTO;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.DIFF_SUFFIX;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EOL;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.FOLDER_TMP_DIFF;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.util.GuessStreamEncoding;
import com.taobao.eclipse.plugin.reviewboard.core.util.IOUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;

/**
 * 类说明:为pre-commit方式生成diff。支持多文件、跨project
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings( { "unchecked" })
public class GeneratePreCommitDiffOperation implements IRunnableWithProgress {

    private IResource[] resources;
    private IResource[] unaddedAllResourceList;
    private ArrayList newFiles;
    private SVNRevision compareVersion;

    /** ReviewBoard对文件编码的支持还不够好，不得不采用这种方式（当同一次提交的文件包含多种编码时） */
    Map< String, StringBuilder > diffContentSBByCharset = new HashMap< String, StringBuilder >();
    /** 最终生成的diff的文件内容 */
    private File[] fileDiffs;

    public GeneratePreCommitDiffOperation(IResource[] resources, IResource[] unaddedAllResourceList,
            File file, Shell shell, SVNRevision compareVersion) {
        this.resources = resources;
        this.unaddedAllResourceList = unaddedAllResourceList;
        if( null == compareVersion ){
            compareVersion = SVNRevision.BASE;
        }
        this.compareVersion = compareVersion;
    }

    /**
     * Convenience method that maps the given resources to their providers. The
     * returned Hashtable has keys which are ITeamProviders, and values which
     * are Lists of IResources that are shared with that provider.
     * 
     * @return a hashtable mapping providers to their resources
     */
    protected Hashtable getProviderMapping(IResource[] resources) {
        Hashtable result = new Hashtable();
        for (int i = 0; i < resources.length; i++) {
            RepositoryProvider provider = RepositoryProvider.getProvider(resources[i].getProject());
            List list = (List) result.get(provider);
            if (list == null) {
                list = new ArrayList();
                result.put(provider, list);
            }
            list.add(resources[i]);
        }
        return result;
    }

    /**
     * @see IRunnableWithProgress#run(IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        
        try {
            monitor.beginTask("", 500); //$NON-NLS-1$
            monitor.setTaskName(Policy.bind("GenerateSVNDiff.working")); //$NON-NLS-1$

            ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resources[0]);

            newFiles = new ArrayList();
            if ( null != unaddedAllResourceList && unaddedAllResourceList.length > 0) {
                for (  IResource unAddedResourceTmp : unaddedAllResourceList ){
                    for( IResource resourceTmp : resources ){
                        if( unAddedResourceTmp.getLocation().toString().equals(resourceTmp.getLocation().toString()) ){
                            newFiles.add( resourceTmp );
                            break;
                        }
                    }
                }
                if (newFiles.size() > 0) {
                    try {
                        // associate the resources with their respective
                        // RepositoryProvider
                        Hashtable table = getProviderMapping((IResource[]) newFiles.toArray(new IResource[newFiles.size()]));
                        Set keySet = table.keySet();
                        Iterator iterator = keySet.iterator();
                        while (iterator.hasNext()) {
                            IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 100);
                            SVNTeamProvider provider = (SVNTeamProvider) iterator.next();
                            List list = (List) table.get(provider);
                            IResource[] providerResources = (IResource[]) list.toArray(new IResource[list.size()]);

                            provider.add(providerResources, IResource.DEPTH_INFINITE, subMonitor);
                        }
                    } catch (TeamException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            }

            ISVNClientAdapter svnClient = svnResource.getRepository().getSVNClient();
            Set<IResource> includedResources = new HashSet<IResource>();
            if ( null != resources && resources.length > 0) {
                includedResources.addAll(Arrays.asList(resources));
            }
            //将includedResources按照project进行分组
            Map<IProject, List<IResource>> projectToResources = new HashMap<IProject, List<IResource>>();
            for( IResource resourceTmp : includedResources ){
                IProject project = resourceTmp.getProject();
                List<IResource> resources = projectToResources.get(project);
                if (resources == null) {
                    resources = new ArrayList<IResource>();
                    projectToResources.put(project, resources);
                }
                resources.add(resourceTmp);
            }
            
            //确保所有的project都在同一个Repository Root里（否则，无法正常生成ReviewBoard能够识别的diff）
            if( !RbSVNUrlUtils.isProjectWithSameSVNRepository( projectToResources.keySet() ) ){
                throw new Exception( RbSubclipseMessages.getString("ERROR_PROJECT_NOTSAME_REPOSITORY") );
            }
            
            //再逐个生成resources的diff
            SVNRevision toRevision = SVNRevision.WORKING;
            monitor.worked(200);
            for ( Iterator<Entry<IProject, List<IResource>>> iEntry = projectToResources.entrySet().iterator(); iEntry.hasNext(); ) {
                monitor.worked(100);  
                Entry<IProject, List<IResource>> entry = (Entry<IProject, List<IResource>>) iEntry.next();
                IProject project = (IProject) entry.getKey();
                List<IResource> resources = (List<IResource>) entry.getValue();
                
                String repositoryRootUrl = RbSVNUrlUtils.getRepositoryRootUrlForResource( project );
                String projectSVNUrl = RbSVNUrlUtils.getSVNUrlForProject( project );
                if( !repositoryRootUrl.endsWith("/") ){
                    repositoryRootUrl += "/";
                }
                if( !projectSVNUrl.endsWith("/") ){
                    projectSVNUrl += "/";
                }
                String relativeRepositoryRootUrl = projectSVNUrl.replaceFirst( repositoryRootUrl, EMPTY_STRING );
                
                for( IResource resourceTmp : resources ){
                    if( ! (resourceTmp instanceof IFile) ){
                        continue;
                    }
                    File tempDiffFile = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
                    File resourceFileTmp = ((IFile)resourceTmp).getLocation().toFile(); 
                    SVNRevision compareVersionReal = compareVersion;
                    //修复版本号
                    if( compareVersionReal instanceof SVNRevision.Number ){
                        compareVersionReal = RbSVNUrlUtils.reviseSVNRevisionAdaptByMaxOrMin( resourceTmp, (SVNRevision.Number)compareVersion, false, null );
                        if( null == compareVersionReal ){
                            compareVersionReal = compareVersion;
                        }
                    }
                    svnClient.diff(resourceFileTmp, compareVersionReal, resourceFileTmp, toRevision,
                            tempDiffFile, false, false, false, false);
                    
                    if( null == tempDiffFile || !tempDiffFile.exists() ){
                        continue;
                    }
                    
                    String charsetName = GuessStreamEncoding.getFileEncoding(tempDiffFile);

                    RbConfig rbConfig = RbConfigReader.getRbConfig(null);
                    if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
                        charsetName = rbConfig.getCharsetEncoding();
                    }
                    
                    String diffContentTmp = IOUtils.getContentFromFile( tempDiffFile, charsetName );
                    //diffContentTmp = IOUtils.getContentFromFile( tempDiffFile.getAbsolutePath() );
                    tempDiffFile.delete();
                    
                    if( null == diffContentTmp || diffContentTmp.trim().isEmpty()){
                        continue;
                    }
                    //替换为相对路径
                    File releativeFile = resourceTmp.getProject().getLocation().toFile();
                    String relativeFilePathStr = null;
                    //先计算出相对文件的路径。注意，相对路径的方式与unaddedResources保持一致
                    try {
                        relativeFilePathStr = releativeFile.getCanonicalPath();
                    } catch (IOException e1) {
                        relativeFilePathStr = releativeFile.getAbsolutePath();
                    }
                    if(!relativeFilePathStr.endsWith("/")){
                        relativeFilePathStr+= "/";
                    }
                    relativeFilePathStr = relativeFilePathStr.replace('\\', '/');
                    
                    String relativeRepositoryRootUrlCharset = relativeRepositoryRootUrl;
                    try {
                        relativeFilePathStr = new String(relativeFilePathStr.getBytes(), charsetName);  
                        relativeRepositoryRootUrlCharset = new String(relativeRepositoryRootUrlCharset.getBytes(), charsetName);
                    } catch (Exception e) {
                    }  
                    
                    diffContentTmp = diffContentTmp.replaceAll("Index: *"+relativeFilePathStr, "Index: "+relativeRepositoryRootUrlCharset);
                    diffContentTmp = diffContentTmp.replaceAll("--- *"+relativeFilePathStr, "--- "+relativeRepositoryRootUrlCharset);
                    diffContentTmp = diffContentTmp.replaceAll("[+][+][+] *"+relativeFilePathStr, "+++ "+relativeRepositoryRootUrlCharset);

                    StringBuilder diffContentSBTmp = diffContentSBByCharset.get(charsetName); 
                    if( null == diffContentSBTmp ){
                        diffContentSBTmp = new StringBuilder();
                    }
                    diffContentSBTmp.append( diffContentTmp );
                    diffContentSBTmp.append( EOL );
                    diffContentSBByCharset.put(charsetName, diffContentSBTmp);
                }
            }

            //最后，构造需要上传的diff文件
            this.fileDiffs = new File[ diffContentSBByCharset.size() ];
            int index = 0;
            for( String charsetNameTmp : diffContentSBByCharset.keySet() ){
                StringBuilder diffContentSBTmp = diffContentSBByCharset.get(charsetNameTmp);
                if( null == diffContentSBTmp || diffContentSBTmp.toString().isEmpty() ){
                    continue;
                }
                this.fileDiffs[ index ] = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
                IOUtils.saveFile(this.fileDiffs[ index ], diffContentSBTmp.toString(), charsetNameTmp);
                index++;
            }

        } catch (Exception e) {
            throw new InterruptedException(e.getMessage());
        } finally {
            if (newFiles.size() > 0) {
                for (int i = 0; i < newFiles.size(); i++) {
                    IResource resource = (IResource) newFiles.get(i);
                    try {
                        SVNWorkspaceRoot.getSVNResourceFor(resource).revert();
                    } catch (Exception e) {
                    }
                }
            }
            monitor.done();
        }
    }

    public File[] getFileDiffs() {
        return fileDiffs;
    }

    public void setFileDiffs(File[] fileDiffs) {
        this.fileDiffs = fileDiffs;
    }

    public Map<String, StringBuilder> getDiffContentSBByCharset() {
        return diffContentSBByCharset;
    }

    
}
