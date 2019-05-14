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
package com.taobao.eclipse.plugin.reviewboard.subclipse.util;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.BUFFER_SIZE_DEFAULT;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_AUTO;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.DIFF_SUFFIX;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.ECLIPSE_PATCH_HEADER;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.ECLIPSE_PROJECT_MARKER;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EOL;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.FOLDER_TMP_DIFF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.SVNConflictVersion.NodeKind;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.exception.DiffGenerateException;
import com.taobao.eclipse.plugin.reviewboard.core.util.GuessStreamEncoding;
import com.taobao.eclipse.plugin.reviewboard.core.util.IOUtils;

/**
 * 类说明:生成diff的相关Util
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public final class RbSVNDiffGeneratorUtils {

    private RbSVNDiffGeneratorUtils() {
        super();
    }
        
    /**
     * 
     * 根据受影响的文件(或文件夹)生成Reviewboard能识别的diff。返回diff文件
     * 
     * @param resource 根据哪个路径生成的diff
     * @param prefixPath 补充前缀（该字段充分考虑了跨project提交的情况）
     * @param fromUrl
     * @param fromRevision
     * @param toUrl
     * @param toRevision
     * @param fileDiff 最后生成的diff存储到哪个文件
     * @param localNewVersionFlag  localNewVersionFlag的作用是：如果localNewVersionFlag=true，那么当localResource != null时，取本地的最新版本
     * @throws DiffGenerateException
     * @throws MalformedURLException
     */
    public static void getDiffRelativeContent(IResource resource, String prefixPath, 
            SVNUrl fromUrl, SVNRevision fromRevision, SVNUrl toUrl, SVNRevision toRevision, 
            File fileDiff, boolean localNewVersionFlag)throws DiffGenerateException, MalformedURLException{

        prefixPath = ( null == prefixPath ? EMPTY_STRING : prefixPath.trim() );
        if( !prefixPath.isEmpty() && !prefixPath.endsWith("/") ){
            prefixPath += "/";
        }
        
        File resourceFile = resource.getLocation().toFile();
        boolean isFile = resourceFile.isFile();
        
        String relativeFileStr = null;
        String resourcePath = null;
        String relativeStr = null;
        //先计算出相对文件的路径
        File relativeFile = resource.getProject().getLocation().toFile();
        try {
            if (relativeFile.isDirectory())
                relativeFileStr = relativeFile.getCanonicalPath();
            else
                relativeFileStr = relativeFile.getParentFile().getCanonicalPath();
        } catch (IOException e1) {
            if (relativeFile.isDirectory())
                relativeFileStr = relativeFile.getAbsolutePath();
            else
                relativeFileStr = relativeFile.getParentFile().getAbsolutePath();
        }
        if(!relativeFileStr.endsWith("/"))relativeFileStr += "/";
        relativeFileStr = relativeFileStr.replace('\\', '/');
        
        //再计算出最终需要替换的相对路径
        File resourceFileTmp = null;
        if( isFile ){//如果是文件，则处理方式不一样
            resourceFileTmp = resourceFile.getParentFile();
        }else{
            resourceFileTmp = resourceFile;
        }
        try {
            resourcePath = resourceFileTmp.getCanonicalPath();
        } catch (IOException e) {
            resourcePath = resourceFileTmp.getAbsolutePath();
        }
        if(!resourcePath.endsWith("/"))resourcePath = resourcePath + "/";
        resourcePath = resourcePath.replace('\\', '/');
        
        relativeStr = resourcePath.replaceFirst(relativeFileStr, "");
        if(relativeStr.startsWith("/")){//防止Resource和RelativeFile事实上指向同一个文件的情况
            relativeStr = relativeStr.replaceFirst("/", "");
        }
        
        File tempDiffFile = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
        diff(isFile, fromUrl, fromRevision, toUrl, toRevision, tempDiffFile, localNewVersionFlag, null );
        
        //无需转换的话：
        if( null == tempDiffFile || !tempDiffFile.exists() ){
            return ;
        }
        
        //String resourceSVNUrl = getSVNUrlForResouce(resource, false);
        String charsetName = GuessStreamEncoding.getFileEncoding(tempDiffFile);
        
        // FIXME 临时编码解决方案。请以方法参数方式提供
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
            charsetName = rbConfig.getCharsetEncoding();
        }
        
        String diffContent = IOUtils.getContentFromFile( tempDiffFile, charsetName );

        if( null != tempDiffFile && tempDiffFile.exists() ){
            tempDiffFile.delete();
        }
        
        if( null == diffContent || diffContent.trim().isEmpty()){
            //此时fileDiff文件内容为空
            return ;
        }

        try {
            prefixPath = new String(prefixPath.getBytes(), charsetName);  
            relativeStr = new String(relativeStr.getBytes(), charsetName);
        } catch (UnsupportedEncodingException e) {
        }  
        
        if(isFile){//如果是文件
            String fileName = resourceFile.getName();
            try {
                fileName = new String(fileName.getBytes(), charsetName);
            } catch (UnsupportedEncodingException e) {
            }  
            diffContent = diffContent.replaceAll("Index: *"+fileName, "Index: "+ prefixPath + relativeStr + fileName);
            diffContent = diffContent.replaceAll("--- *"+fileName, "--- "+ prefixPath + relativeStr + fileName);
            diffContent = diffContent.replaceAll("[+][+][+] *"+fileName, "+++ "+ prefixPath + relativeStr + fileName);
        }else{//文件夹则需要进一步操作。根据svnDiffSummarys进行判断
            SVNDiffSummary[] svnDiffSummarys = RbSVNDiffUtils.diffSummarize(fromUrl, fromRevision, toUrl, toRevision, false, null);
            
            List<String> pathList = new ArrayList<String>();
            if( null != svnDiffSummarys && svnDiffSummarys.length != 0 ){
                //paths = new String[svnDiffSummarys.length];
                for( int index = 0; index < svnDiffSummarys.length ; index ++ ){
                    //paths[index] = svnDiffSummarys[index].getPath();
                    SVNDiffSummary svnDiffSummary = svnDiffSummarys[index];
                    if( svnDiffSummary.getNodeKind() == NodeKind.file ){
                        pathList.add(svnDiffSummary.getPath());
                    }
                }
            }          
            String[] paths = pathList.toArray(new String[pathList.size()]);
            
            //不是文件的话，为了减少出错的可能，这里一个个文件地处理
            if( null != paths && paths.length > 0 ){
                //逐个生成diff
                for( String path : paths ){
                    path = path.trim();
                    if( path.isEmpty() ){
                        continue;
                    }
                    try {
                        path = new String(path.getBytes(), charsetName);
                    } catch (UnsupportedEncodingException e) {
                    }  
                    diffContent = diffContent.replaceAll("Index: *"+path, "Index: "+ prefixPath + relativeStr + path);
                    diffContent = diffContent.replaceAll("--- *"+path, "--- "+ prefixPath + relativeStr + path);
                    diffContent = diffContent.replaceAll("[+][+][+] *"+path, "+++ "+ prefixPath + relativeStr + path);
                }
            }
        }
        
        IOUtils.saveFile(fileDiff, diffContent, charsetName);
        
        return ;
        
    }
    
    /**
     * 生成diff
     * 
     * @param isFile 如果当前url是文件，则true；否则false
     * @param fromUrl 
     * @param fromRevision
     * @param toUrl
     * @param toRevision
     * @param fileDiff 最后生成的diff存储到哪个文件
     * @param localNewVersionFlag localNewVersionFlag的作用是：如果localNewVersionFlag=true，那么当localResource != null时，取本地的最新版本
     * @param localResource 
     * @throws DiffGenerateException
     */
    public static void diff(boolean isFile, SVNUrl fromUrl, SVNRevision fromRevision, 
            SVNUrl toUrl, SVNRevision toRevision, File fileDiff,
            boolean localNewVersionFlag,ISVNResource localResource) throws DiffGenerateException {
        try {
            
            File tmpFile = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
            
            //生成diff
            diff(fromUrl, fromRevision, toUrl, toRevision, tmpFile, localNewVersionFlag, localResource);

            //先计算出有哪些受影响的文件
            String charsetName = GuessStreamEncoding.getFileEncoding(tmpFile);
            
            // FIXME 临时编码解决方案。请以方法参数方式提供
            RbConfig rbConfig = RbConfigReader.getRbConfig(null);
            if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
                charsetName = rbConfig.getCharsetEncoding();
            }
            
            String diffContent = IOUtils.getContentFromFile( tmpFile, charsetName );
            
            if( null != tmpFile && tmpFile.exists() ){
                tmpFile.delete();
            }
            
            if( isFile ){//如果是文件，则直接判断文件名是否匹配
                IOUtils.saveFile(fileDiff, diffContent, charsetName);
                return ;
            }else{//文件夹则需要进一步操作。根据svnDiffSummarys进行判断
                SVNDiffSummary[] svnDiffSummarys = RbSVNDiffUtils.diffSummarize(fromUrl, fromRevision, toUrl, toRevision, false, null);  
                List<String> pathList = new ArrayList<String>();
                if( null != svnDiffSummarys && svnDiffSummarys.length != 0 ){
                    //paths = new String[svnDiffSummarys.length];
                    for( int index = 0; index < svnDiffSummarys.length ; index ++ ){
                        //paths[index] = svnDiffSummarys[index].getPath();
                        SVNDiffSummary svnDiffSummary = svnDiffSummarys[index];
                        if( svnDiffSummary.getNodeKind() == NodeKind.file ){
                            pathList.add(svnDiffSummary.getPath());
                        }
                    }
                }          
                String[] paths = pathList.toArray(new String[pathList.size()]);
                
                if( null != paths && paths.length > 0 ){
                    //逐个生成diff
                    for( String path : paths ){
                        path = path.trim();
                        if( path.isEmpty() ){
                            continue;
                        }
                        try {
                            path = new String(path.getBytes(), charsetName);
                        } catch (UnsupportedEncodingException e) {
                        }  
                        diffContent = diffContent.replaceAll("Index: *"+path, "Index: "+ path);
                        diffContent = diffContent.replaceAll("--- *"+path, "--- "+ path);
                        diffContent = diffContent.replaceAll("[+][+][+] *"+path, "+++ "+ path);
                    }
                }
            }
            
            IOUtils.saveFile(fileDiff, diffContent, charsetName);
            
        }catch ( DiffGenerateException e ) {
            throw e;
        }finally {
        }      
    }



    /**
     * 生成diff
     * 
     * @param fromUrl 
     * @param fromRevision
     * @param toUrl
     * @param toRevision
     * @param pathFilterMatch 如需匹配，则匹配的正则表达式
     * @param localNewVersionFlag localNewVersionFlag的作用是：如果localNewVersionFlag=true，那么当localResource != null时，取本地的最新版本
     * @param localResource 
     * @throws DiffGenerateException
     */
    public static void diff(SVNUrl fromUrl, SVNRevision fromRevision, 
            SVNUrl toUrl, SVNRevision toRevision, File fileDiff,
            boolean localNewVersionFlag,ISVNResource localResource) throws DiffGenerateException {
        try {
            ISVNClientAdapter client = null;
            ISVNRepositoryLocation repository = SVNProviderPlugin.getPlugin().getRepository(fromUrl.toString());
            if (repository != null){
                client = repository.getSVNClient();
            }
            
            if( null == client ){
                return ;
            }
            SVNRevision pegRevision = null;
            if ( localNewVersionFlag && fromUrl.toString().equals(toUrl.toString()) && localResource != null) {
                if (localResource.getResource() == null) pegRevision = SVNRevision.HEAD;
                else {
                    IResource resource = localResource.getResource();
                    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                    pegRevision = svnResource.getRevision();
                }
            }
            if (pegRevision == null){
                client.diff(fromUrl, fromRevision, toUrl, toRevision, fileDiff, true);
            }
            else{
                client.diff(fromUrl, pegRevision, fromRevision, toRevision, fileDiff, true); 
            }
            
        }catch (SVNClientException e) {
            throw new DiffGenerateException(e);
        }catch ( SVNException e2 ) {
            throw new DiffGenerateException(e2);
        }catch ( Exception e3 ) {
            throw new DiffGenerateException(e3);
        }finally {
        }      
    }
    
    /**
     * 创建eclipseFormat的eclipse diff文件。针对版本之间进行比较。如果stop=0，则取head
     * @param paths
     * @param outputFile
     * @param recurse
     * @throws SVNClientException
     */
    @SuppressWarnings("unchecked")
    public static void createEclipsePatchForRemoteSVN(IResource[] paths, int start, int stop, 
            File outputFile, boolean recurse) throws SVNClientException {
        FileOutputStream os = null;
        InputStream is = null;
        
        try {
            byte[] buffer = new byte[BUFFER_SIZE_DEFAULT];
            
            os = new FileOutputStream(outputFile);
            if (paths.length > 0) {
                os.write(ECLIPSE_PATCH_HEADER.getBytes());
                os.write(EOL.getBytes());
            }
            
            Map projectToResources = new HashMap();
            
            for (int i = 0; i < paths.length; i++) {
                IResource resource = paths[i];
                IProject project = resource.getProject();
                List files = (List) projectToResources.get(project);
                if (files == null) {
                    files = new ArrayList();
                    projectToResources.put(project, files);
                }
                files.add(resource.getLocation().toFile());
            }
            
            for (Iterator iEntry = projectToResources.entrySet().iterator(); iEntry.hasNext();) {
                Entry entry = (Entry) iEntry.next();
                
                IResource project = (IResource) entry.getKey();
                List files = (List) entry.getValue();
                
                ISVNClientAdapter client = SVNWorkspaceRoot.getSVNResourceFor(project).getRepository().getSVNClient();

                os.write(ECLIPSE_PROJECT_MARKER.getBytes());
                os.write(project.getName().getBytes());
                os.write(EOL.getBytes());
                
                File tempFile = File.createTempFile("tempDiff" + getRandomNumber() , DIFF_SUFFIX);
                client.createPatch((File[]) files.toArray(new File[files.size()]), project.getLocation().toFile(), tempFile, recurse);
                
                try {
                    is = new FileInputStream(tempFile);
                    
                    int bytes_read;
                    while ((bytes_read = is.read(buffer)) != -1)
                        os.write(buffer, 0, bytes_read);                
                } finally {
                    if (null != is ) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                    if( null != tempFile && tempFile.exists()){
                        try {
                            tempFile.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SVNClientException(e);
        } finally {
            if (os != null) try {os.close();} catch (IOException e) {}
        }
    }
    
    /**
     * @return 随机的2位数字
     */
    private static int getRandomNumber(){
        int a = -1;
        int b = -1;
        java.util.Random rand=new java.util.Random();
        while( a <= 0)
            a= ( rand.nextInt()) % 9;
        while(b <= 0)
            b= ( rand.nextInt())% 9;
        String str = String.valueOf(a) + String.valueOf(b);
        return Integer.parseInt(str);
    }
}
