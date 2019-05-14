/*
* (C) 2007-2011 Alibaba Group Holding Limited
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
*
* If you have any question, please contact:ǧѾ <qianya@taobao.com>
* Authors:���� <zhiqing.ht@taobao.com>����ʱ<yinshi.nc@taobao.com>
*
*/
package com.taobao.eclipse.plugin.reviewboard.subclipse.diffoperation;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_AUTO;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.DIFF_SUFFIX;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.FOLDER_TMP_DIFF;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.util.GuessStreamEncoding;
import com.taobao.eclipse.plugin.reviewboard.core.util.IOUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNDiffGeneratorUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;

/**
 * ��˵��:Ϊpost-commit��ʽ����diff��ע�⣺��֧�ֻ���SVNUrl�İ汾�Ƚ�
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class GeneratePostDiffBySVNUrlOperation implements IRunnableWithProgress {
    
    private String start;
    private String stop;
    private String startUrl;
    private String stopUrl;
    private boolean success;
    private IResource resource;
    private boolean isFile;
    
    
    /**
     * �������ɵ�diff���ļ�����
     */
    private File[] fileDiffs;

    /**
     * @param resource ��ǰ������resource ������Ϊnull
     * @param isFile ��ǰ���������ļ������ļ��С���� resource != null,��isFile�����resource�ٴ����¼���ֵ
     * @param start
     * @param stop
     * @param startUrl
     * @param stopUrl
     */
    public GeneratePostDiffBySVNUrlOperation(IResource resource, boolean isFile,
            String start, String stop, 
            String startUrl, String stopUrl) {
        this.start = start;
        this.stop = stop;
        this.startUrl = startUrl;
        this.stopUrl = stopUrl;
        this.success = true;
        this.resource = resource;
        this.isFile = isFile;
        //��� resource != null,��isFile�����resource�ٴ����¼���ֵ
        if( null != this.resource ){
            if( this.resource instanceof IFile ){
                this.isFile = true;
            }else{
                this.isFile = false;
            }
        }
    }

    /**
     * @see IRunnableWithProgress#run(IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        
        try {
            monitor.beginTask("", 500); //$NON-NLS-1$
            monitor.setTaskName(Policy.bind("GenerateSVNDiff.working")); //$NON-NLS-1$
            
            try {
                monitor.worked(100);
                
                //����diff
                SVNUrl fromUrl = new SVNUrl(stopUrl);
                SVNUrl toUrl = new SVNUrl(startUrl);
                SVNRevision[] fromAndToRevision = RbSVNUrlUtils.formateSVNRevisionUnify(start, stop);
                ISVNLocalResource localResource = null;
                if( null != resource ){
                    localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                }
                File tempDiffFile = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
                //����diff
                RbSVNDiffGeneratorUtils.diff( isFile, fromUrl, fromAndToRevision[0], 
                        toUrl, fromAndToRevision[1], tempDiffFile, 
                        false, localResource );
                if( null == tempDiffFile || !tempDiffFile.exists() ){
                    return ;
                }
                
                String charsetName = GuessStreamEncoding.getFileEncoding(tempDiffFile);
                
                RbConfig rbConfig = RbConfigReader.getRbConfig(null);
                if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
                    charsetName = rbConfig.getCharsetEncoding();
                }
                
                String diffContentTmp = IOUtils.getContentFromFile( tempDiffFile, charsetName );

                tempDiffFile.delete();
                
                if( null == diffContentTmp || diffContentTmp.trim().isEmpty()){
                    return ;
                }
                
                //��󣬹�����Ҫ�ϴ���diff�ļ�
                this.fileDiffs = new File[ 1 ];
                this.fileDiffs[0] = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
                IOUtils.saveFile(this.fileDiffs[0], diffContentTmp, charsetName);

            } catch (Exception e) {
                success = false;
                throw e;
            }
            //�������diffʧ��
            if( !this.success ){
                return ;
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
    
}
