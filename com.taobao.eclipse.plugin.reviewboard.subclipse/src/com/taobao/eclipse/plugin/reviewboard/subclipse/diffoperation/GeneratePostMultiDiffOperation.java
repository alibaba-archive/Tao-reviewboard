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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.util.GuessStreamEncoding;
import com.taobao.eclipse.plugin.reviewboard.core.util.IOUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNDiffGeneratorUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;

/**
 * 类说明:为post-commit方式生成diff。支持多文件、多文件夹、跨project
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class GeneratePostMultiDiffOperation implements IRunnableWithProgress {
    
    private String start;
    private String stop;
    private boolean success;
    private IResource[] resources;
    

    /**ReviewBoard对文件编码的支持还不够好，不得不采用这种方式（当同一次提交的文件包含多种编码时）*/
    Map< String, StringBuilder > diffContentSBByCharset = new HashMap< String, StringBuilder >();
    /**最终生成的diff的文件内容*/
    private File[] fileDiffs;

    public GeneratePostMultiDiffOperation(IResource[] resources, 
            String start, String stop) {
        this.start = start;
        this.stop = stop;
        this.resources = resources;
    }

    /**
     * @see IRunnableWithProgress#run(IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        
        try {
            monitor.beginTask("", 300+100*(resources.length)); //$NON-NLS-1$
            monitor.setTaskName(Policy.bind("GenerateSVNDiff.working")); //$NON-NLS-1$
            
            Set<IResource> includedResources = new HashSet<IResource>();
            includedResources.addAll(Arrays.asList(resources));
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

            /*diffContentSB.append( ECLIPSE_PATCH_HEADER );
            diffContentSB.append( EOL );*/
            
            for ( Iterator<Entry<IProject, List<IResource>>> iEntry = projectToResources.entrySet().iterator(); iEntry.hasNext(); ) {
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
                    monitor.worked(100);  
                    File tempDiffFile = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
                    monitor.worked(100);
                    String[] baseSVNUrl = RbSVNUrlUtils.getBaseSVNUrlForDiffUpload(resourceTmp, false);
                    String stopUrl = baseSVNUrl[2];
                    String startUrl = baseSVNUrl[2];
                    SVNUrl fromUrl = new SVNUrl(stopUrl);
                    SVNUrl toUrl = new SVNUrl(startUrl);
                    try {
                        SVNRevision[] fromAndToRevision = RbSVNUrlUtils.formateSVNRevisionUnify(start, stop);
                        SVNRevision startVersion = fromAndToRevision[0];
                        SVNRevision stopVersion = fromAndToRevision[1];
                        //修复版本号
                        if( stopVersion instanceof SVNRevision.Number ){
                            stopVersion = RbSVNUrlUtils.reviseSVNRevision( resourceTmp, 
                                    (SVNRevision.Number)fromAndToRevision[1], true, true, null );
                        }
                        if( startVersion instanceof SVNRevision.Number ){
                            startVersion = RbSVNUrlUtils.reviseSVNRevision( resourceTmp, 
                                    (SVNRevision.Number)fromAndToRevision[0], false, true
                                    , stopVersion instanceof SVNRevision.Number ? (SVNRevision.Number)stopVersion : null );
                        }
                        //尽可能少地访问SVN库，以提高性能
                        if( startVersion instanceof SVNRevision.Number && stopVersion instanceof SVNRevision.Number ){
                            if( ((SVNRevision.Number)startVersion).getNumber() == ((SVNRevision.Number)stopVersion).getNumber() ){
                                continue;
                            }
                        }
                        if( !(startVersion instanceof SVNRevision.Number) && !(stopVersion instanceof SVNRevision.Number) ){
                            if( startVersion.equals(stopVersion) ){
                                continue;
                            }
                        }
                        //生成diff内容
                        RbSVNDiffGeneratorUtils.getDiffRelativeContent( resourceTmp, 
                                relativeRepositoryRootUrl,
                                fromUrl, startVersion, toUrl, stopVersion, tempDiffFile, false);
                    } catch (Exception e) {
                        throw e;
                    }
                    if( null == tempDiffFile || !tempDiffFile.exists() ){
                        continue;
                    }
                    
                    String charsetName = GuessStreamEncoding.getFileEncoding(tempDiffFile);
                    
                    RbConfig rbConfig = RbConfigReader.getRbConfig(null);
                    if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
                        charsetName = rbConfig.getCharsetEncoding();
                    }
                    
                    String diffContentTmp = IOUtils.getContentFromFile( tempDiffFile, charsetName );

                    tempDiffFile.delete();
                    
                    if( null == diffContentTmp || diffContentTmp.trim().isEmpty()){
                        continue;
                    }
                    
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
        }               
        finally {
            monitor.done();
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
