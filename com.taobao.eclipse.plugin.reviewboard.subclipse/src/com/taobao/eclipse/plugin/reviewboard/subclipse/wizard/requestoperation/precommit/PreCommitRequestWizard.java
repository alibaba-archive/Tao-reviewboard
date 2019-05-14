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
package com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.precommit;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_AUTO;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.Policy;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.exception.OperationException;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequest;
import com.taobao.eclipse.plugin.reviewboard.core.model.ReviewRequestStatus;
import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.dialog.CloseDialogWithHttpLink;
import com.taobao.eclipse.plugin.reviewboard.subclipse.diffoperation.GeneratePreCommitDiffOperation;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.AbstractRequestWizard;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.RequestOptionInfoWizardPage;

/**
 * 类说明:CommitRequestWizard，提交 Request 
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings({"unchecked"})
public class PreCommitRequestWizard extends AbstractRequestWizard {

    protected PreCommitRequestWizardPage mainPage = null;
    protected RequestOptionInfoWizardPage requestOptionInfoWizardPage;
    
    @Override
    protected IResource[] getResources() {
        return mainPage.getSelectedResources();
    }
    
    public PreCommitRequestWizard( IWorkbenchPart targetPart, IWorkbenchPage targetPage,
            IStructuredSelection selection, IResource[] unaddedResources,
            IResource[] resourcesSelectedByUser, HashMap statusMap, IReviewboardClient reviewboardClient) {
        super(targetPart, targetPage, selection, unaddedResources, resourcesSelectedByUser, statusMap, reviewboardClient);
        setWindowTitle(RbSubclipseMessages.getString("PreReviewBoardAction.dialogTitle"));
    }

    public PreCommitRequestWizard( IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            IResource[] resourcesSelectedByUser, IReviewboardClient reviewboardClient, 
            int startVersion, int stopVersion) {
        super(targetPart, targetPage, resourcesSelectedByUser, reviewboardClient, startVersion, stopVersion);
        setWindowTitle(RbSubclipseMessages.getString("PreReviewBoardAction.dialogTitle"));
    }

    @Override
    public void addPages() {
        String pageTitle = RbSubclipseMessages.getString("PreReviewBoardAction.dialogTitle"); 
        StringBuilder pageDescriptionSB = new StringBuilder();
        mainPage = new PreCommitRequestWizardPage(targetPart, targetPage, pageTitle, pageTitle, 
                SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_DIFF),
                resourcesSelectedByUser, selection, statusMap, reviewboardClient, startAndStopVersion);
        mainPage.setDescription(pageDescriptionSB.toString());
        
        requestOptionInfoWizardPage = new RequestOptionInfoWizardPage();
        
        addPage(mainPage);
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        if( rbConfig.isAllowOptionalPageCommit() ){
            addPage(requestOptionInfoWizardPage);
        }
    }

    @Override
    public boolean performFinish() {
        if( !mainPage.validateTextField() ){
            
            return false;
        }
        try {
            File[] fileDiffs = null;
            //由于SVN生成diff时相对路径的问题，不得不引用此变量
            //详情：当client.diff(fromUrl, fromRevision, toUrl, toRevision, file, true)中fromUrl和toUrl是项目的baseUrlDir时，diff内容的路径正常
            //可是：当fromUrl和toUrl是选定的resouce的url时，diff内容的路径不正常，路径缺少部分前缀
            String repositoryRootUrl = null;
            
            IResource[] mainPageSelectedResources= getResources();
            IResource[] unaddedAllResourceList= getUnaddedResources();
            if( null == mainPageSelectedResources || mainPageSelectedResources.length == 0 ){
                MessageDialog.openError(getShell(), 
                        RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                        RbSubclipseMessages.getString("ERROR_FILE_REQURIED")); //$NON-NLS-1$
                return false; 
            }
            try {
                repositoryRootUrl = RbSVNUrlUtils.getRepositoryRootUrlForResource( 
                        (IResource)mainPageSelectedResources[0].getProject() );
            } catch (ReviewboardException e) {
                MessageDialog.openError(getShell(), 
                        RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),  
                        e.getMessage() ); //$NON-NLS-1$
                return false;
            }
            
            //临时编码解决方案。请以方法参数方式提供
            RbConfig rbConfig = RbConfigReader.getRbConfig(null);
            if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
                boolean isAcceptCharset = MessageDialog.openConfirm(getShell(), 
                        RbSubclipseMessages.getString("WARNING_LABLE"),
                        MessageFormat.format(RbSubclipseMessages.getString("CHECK_DIFF_0"), 
                                new Object[]{ rbConfig.getCharsetEncoding(), CHARSET_AUTO }));
                if(!isAcceptCharset){
                    return false;
                }
            }
            
            SVNRevision compareVersion = null;
            if( mainPage.getStartVersion() > 0 ){
                compareVersion = new SVNRevision.Number(mainPage.getStartVersion());
            }
            GeneratePreCommitDiffOperation generateDiffFileOperation = new GeneratePreCommitDiffOperation( 
                    mainPageSelectedResources, unaddedAllResourceList, null, getShell(), compareVersion );
            getContainer().run(true, true, generateDiffFileOperation);
            fileDiffs = generateDiffFileOperation.getFileDiffs();
            Map<String, StringBuilder> diffContentSBByCharset = generateDiffFileOperation.getDiffContentSBByCharset();
            if( null != diffContentSBByCharset && !diffContentSBByCharset.isEmpty() && diffContentSBByCharset.size() > 1){
                boolean isConfirm = MessageDialog.openConfirm(getShell(), 
                        RbSubclipseMessages.getString("WARNING_LABLE"),
                        RbSubclipseMessages.getString("PROMPTING_ENCODING_0"));
                if(!isConfirm){
                    return false;
                }
            }
            
            if( null == fileDiffs || fileDiffs.length == 0 ){
                //未发现差异,无需提交信息到ReviewBoard。
                MessageDialog.openInformation(
                        getShell(),
                        Policy.bind("GenerateSVNDiff.noDiffsFoundTitle"), //$NON-NLS-1$
                        Policy.bind("GenerateSVNDiff.noDiffsFoundMsg")); //$NON-NLS-1$
                return false;
            }

            //提交信息到ReviewBoard服务器
            if( null == reviewboardClient ){
                MessageDialog.openError(getShell(), 
                        RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                        RbSubclipseMessages.getString("ERROR_SERVER_REQURIED")); //$NON-NLS-1$
                return false;
            }
            
            ModelReviewRequest reviewRequest = null;
            //如果是修改
            if(mainPage.isUpdate()){
                //先查找出
                reviewRequest = mainPage.getReviewRequest();
                //设置额外信息
                if( rbConfig.isAllowOptionalPageCommit() ){
                    requestOptionInfoWizardPage.setReviewRequestOptionalInfo(reviewRequest);
                }
            }else{//如果是新增
                reviewRequest = mainPage.getReviewRequest();
                //设置额外信息
                if( rbConfig.isAllowOptionalPageCommit() ){
                    requestOptionInfoWizardPage.setReviewRequestOptionalInfo(reviewRequest);
                }
            }
            
            if( null != reviewRequest
                    && null != reviewRequest.getRepository() 
                    && null != reviewRequest.getRepository().getPath() 
                    && !reviewRequest.getRepository().getPath().trim().isEmpty()){
                try {
                    String svnUrlProject = RbSVNUrlUtils.getSVNUrlForProject((mainPageSelectedResources[0]));
                    if( !svnUrlProject.trim().toLowerCase().startsWith(reviewRequest.getRepository().getPath().trim().toLowerCase())){
                        CloseDialogWithHttpLink closeDialogWithLink = new CloseDialogWithHttpLink(getShell(), 
                            ReviewboardUtil.norminateRepositoryUrl(reviewboardClient.getServerUrl())+"/r/"+reviewRequest.getId(), 
                            RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                            RbSubclipseMessages.getString("ERROR_NOTVALID_REPOSITORY"));
                        Display display = Display.getCurrent();
                        Color informationColor = display.getSystemColor(SWT.COLOR_RED);
                        closeDialogWithLink.setInformationColor(informationColor);
                        closeDialogWithLink.open();
                        return false;
                    }
                } catch (Exception e) {
                }
            }
            
            //如果是修改
            if(mainPage.isUpdate()){
                
                if( null == reviewRequest || reviewRequest.getId() == 0 ){
                    MessageDialog.openError(getShell(), 
                            RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                            MessageFormat.format(RbSubclipseMessages.getString("ERRO_REQUEST_0"), new Object[]{ String.valueOf(mainPage.getRequestId()) })); //$NON-NLS-1$
                    return false;
                }
                
                if( null != reviewRequest.getStatus() ){
                    if( reviewRequest.getStatus().equals( ReviewRequestStatus.SUBMITTED ) ){
                        CloseDialogWithHttpLink closeDialogWithLink = new CloseDialogWithHttpLink(getShell(), 
                                ReviewboardUtil.norminateRepositoryUrl(reviewboardClient.getServerUrl())+"/r/"+reviewRequest.getId(), 
                                RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                                MessageFormat.format(RbSubclipseMessages.getString("ERRO_REQUEST_1"), new Object[]{ String.valueOf(mainPage.getRequestId()) }));
                        Display display = Display.getCurrent();
                        Color informationColor = display.getSystemColor(SWT.COLOR_RED);
                        closeDialogWithLink.setInformationColor(informationColor);
                        closeDialogWithLink.open();
                        return false;
                    }
                    if( reviewRequest.getStatus().equals( ReviewRequestStatus.DISCARDED ) ){
                        CloseDialogWithHttpLink closeDialogWithLink = new CloseDialogWithHttpLink(getShell(), 
                                ReviewboardUtil.norminateRepositoryUrl(reviewboardClient.getServerUrl())+"/r/"+reviewRequest.getId(), 
                                RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                                MessageFormat.format(RbSubclipseMessages.getString("ERRO_REQUEST_2"), new Object[]{ String.valueOf(mainPage.getRequestId()) }));
                        Display display = Display.getCurrent();
                        Color informationColor = display.getSystemColor(SWT.COLOR_RED);
                        closeDialogWithLink.setInformationColor(informationColor);
                        closeDialogWithLink.open();
                        return false;
                    }
                }
                
                //再修改内容 
                reviewboardService.updateReviewRequestFilterNoValue(reviewRequest, new NullProgressMonitor());
                //再上传diff
                reviewboardService.uploadDiff(reviewRequest.getId(), repositoryRootUrl, fileDiffs, null, new NullProgressMonitor());
                //如果是publish
                if( !mainPage.isDraft() ){
                    reviewboardService.publishReviewRequest(reviewRequest.getId(), new NullProgressMonitor());
                }
                
                CloseDialogWithHttpLink closeDialogWithLink = new CloseDialogWithHttpLink(getShell(), 
                        ReviewboardUtil.norminateRepositoryUrl(reviewboardClient.getServerUrl())+"/r/"+reviewRequest.getId(), 
                        RbSubclipseMessages.getString("SUCCESS_UPDATE"),
                        RbSubclipseMessages.getString("SUCCESS_UPDATE_1")+reviewRequest.getId() +"");
                closeDialogWithLink.open();
            }else{//如果是新增
                
                ModelReviewRequest reviewRequestNew = reviewboardService.newReviewRequestAll( reviewRequest, 
                        repositoryRootUrl, fileDiffs, new NullProgressMonitor());
                
                //如果是publish
                if( !mainPage.isDraft() ){
                    reviewboardService.publishReviewRequest(reviewRequestNew.getId(), new NullProgressMonitor());
                }
                
                CloseDialogWithHttpLink closeDialogWithLink = new CloseDialogWithHttpLink(getShell(), 
                        ReviewboardUtil.norminateRepositoryUrl(reviewboardClient.getServerUrl())+"/r/"+reviewRequestNew.getId(), 
                        RbSubclipseMessages.getString("SUCCESS_COMMIT"), 
                        RbSubclipseMessages.getString("SUCCESS_COMMIT_1")+reviewRequestNew.getId() +"");
                closeDialogWithLink.open();
                
            }
            
            return true;
        } catch (OperationException e) {
            MessageDialog.openError(getShell(), 
                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                    RbSubclipseMessages.getString("ERROR_COMMIT_0")+e.getMessage()); //$NON-NLS-1$
            return false;
        } catch (Exception e) {
            MessageDialog.openError(getShell(), 
                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                    RbSubclipseMessages.getString("ERROR_COMMIT_0")+e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }
    
}
