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
package com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.postcommit;

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
import com.taobao.eclipse.plugin.reviewboard.subclipse.diffoperation.GeneratePostDiffBySVNUrlOperation;
import com.taobao.eclipse.plugin.reviewboard.subclipse.diffoperation.GeneratePostMultiDiffOperation;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.AbstractRequestWizard;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.RequestOptionInfoWizardPage;

/**
 * ��˵��:CommitRequestWizard���ύ Request 
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
@SuppressWarnings({"unchecked"})
public class PostCommitRequestWizard extends AbstractRequestWizard {

    protected PostCommitRequestWizardPage mainPage = null;
    protected RequestOptionInfoWizardPage requestOptionInfoWizardPage;

    //����ļ�����������һ�ֺ�����ķ�ʽ֧��SVN��Դ���д򿪵���ʷ��¼
    protected String svnHistoryUrl;
    protected String svnHistoryUrlForBaseUpload;
    protected boolean svnHistoryUrlIsFolder;
    
    @Override
    protected IResource[] getResources() {
        return mainPage.getSelectedResources();
    }
    
    public PostCommitRequestWizard( IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            IStructuredSelection selection, IResource[] resourcesSelectedByUser, 
            HashMap statusMap, IReviewboardClient reviewboardClient) {
        super(targetPart, targetPage, selection, null, resourcesSelectedByUser, statusMap, reviewboardClient);
        setWindowTitle(RbSubclipseMessages.getString("PostReviewBoardAction.dialogTitle"));
    }

    public PostCommitRequestWizard( IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            IResource[] resourcesSelectedByUser, IReviewboardClient reviewboardClient, 
            long startVersion, long stopVersion) {
        super(targetPart, targetPage, resourcesSelectedByUser, reviewboardClient, startVersion, stopVersion);
        setWindowTitle(RbSubclipseMessages.getString("PostReviewBoardAction.dialogTitle"));
    }

    @Override
    public void addPages() {
        StringBuilder pageDescriptionSB = new StringBuilder();
        String pageTitle = RbSubclipseMessages.getString("PostReviewBoardAction.dialogTitle"); 
        mainPage = new PostCommitRequestWizardPage(targetPart, targetPage, 
                pageTitle, pageTitle,
                SVNUIPlugin.getPlugin().getImageDescriptor(ISVNUIConstants.IMG_WIZBAN_DIFF),
                resourcesSelectedByUser, selection, statusMap, reviewboardClient, startAndStopVersion);//$NON-NLS-1$
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
        
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        
        if( !mainPage.validateTextField() ){
            return false;
        }
        try {
            File[] fileDiffs = null;
            //����SVN����diffʱ���·�������⣬���ò����ô˱���
            //���飺��client.diff(fromUrl, fromRevision, toUrl, toRevision, file, true)��fromUrl��toUrl����Ŀ��baseUrlDirʱ��diff���ݵ�·������
            //���ǣ���fromUrl��toUrl��ѡ����resouce��urlʱ��diff���ݵ�·����������·��ȱ�ٲ���ǰ׺
            String diffUploadBaseUrl = null;
            
            String[] startAndStop = mainPage.getStartAndStopVersion();
            
            try {
                
                //��ʱ���������������Է���������ʽ�ṩ
                if( null != rbConfig.getCharsetEncoding() && !rbConfig.getCharsetEncoding().trim().equals(CHARSET_AUTO) ){
                    boolean isAcceptCharset = MessageDialog.openConfirm(getShell(), 
                            RbSubclipseMessages.getString("WARNING_LABLE"),
                            MessageFormat.format(RbSubclipseMessages.getString("CHECK_DIFF_0"), 
                                    new Object[]{ rbConfig.getCharsetEncoding(), CHARSET_AUTO }));
                    if(!isAcceptCharset){
                        return false;
                    }
                }
                
                if( null == resourcesSelectedByUser || resourcesSelectedByUser.length == 0 ){
                    /*History Post-commit �����������
                     * ��1���Ҽ�Team����ʾ��Դ��ʷ��¼
                     * ��2��������֧�� ��SVN��Դ��ֱ�Ӵ���ʷ��¼�����(����������޷����IResource)
                     * �������2�������޷����IResource����˱�������������ⷽʽ����
                     */
                    GeneratePostDiffBySVNUrlOperation generatePostDiffBySVNUrlOperation = new GeneratePostDiffBySVNUrlOperation( 
                            null, !this.getSvnHistoryUrlIsFolder(), startAndStop[0], startAndStop[1], 
                            this.getSvnHistoryUrl(), this.getSvnHistoryUrl());
                    getContainer().run(true, true, generatePostDiffBySVNUrlOperation);
                    fileDiffs = generatePostDiffBySVNUrlOperation.getFileDiffs();
                    diffUploadBaseUrl = this.getSvnHistoryUrlForBaseUpload();
                }
                else{
                    IResource[] mainPageSelectedResources = getResources();
                    if( null == mainPageSelectedResources || mainPageSelectedResources.length == 0 ){
                        MessageDialog.openError(getShell(), 
                                RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                                RbSubclipseMessages.getString("CHECK_DIFF_1")); //$NON-NLS-1$
                        return false;
                    }
                    try {
                        String repositoryRootUrl = RbSVNUrlUtils.getRepositoryRootUrlForResource( 
                                (IResource)mainPageSelectedResources[0].getProject() );
                        diffUploadBaseUrl = repositoryRootUrl;
                    } catch (ReviewboardException e) {
                        MessageDialog.openError(getShell(), RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), e.getMessage()); //$NON-NLS-1$
                        return false;
                    }
                    
                    GeneratePostMultiDiffOperation generatePostCommitDiffOperation = new GeneratePostMultiDiffOperation( 
                            mainPageSelectedResources, startAndStop[0], startAndStop[1]);
                    getContainer().run(true, true, generatePostCommitDiffOperation);
                    fileDiffs = generatePostCommitDiffOperation.getFileDiffs();
                    Map<String, StringBuilder> diffContentSBByCharset = generatePostCommitDiffOperation.getDiffContentSBByCharset();
                    if( null != diffContentSBByCharset && !diffContentSBByCharset.isEmpty() && diffContentSBByCharset.size() > 1){
                        boolean isConfirm = MessageDialog.openConfirm(getShell(), 
                                RbSubclipseMessages.getString("WARNING_LABLE"),
                                RbSubclipseMessages.getString("PROMPTING_ENCODING_0"));
                        if(!isConfirm){
                            return false;
                        }
                    }
                    
                }
            } catch (Exception e) {
                MessageDialog.openError(getShell(), 
                        RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),  
                        RbSubclipseMessages.getString("ERROR_COMMIT_1") + e.getMessage());
                return false;
            }
            
            if( null == fileDiffs || fileDiffs.length == 0 ){
                //δ���ֲ���,�����ύ��Ϣ��ReviewBoard��
                MessageDialog.openInformation(
                        getShell(),
                        Policy.bind("GenerateSVNDiff.noDiffsFoundTitle"), //$NON-NLS-1$
                        Policy.bind("GenerateSVNDiff.noDiffsFoundMsg")); //$NON-NLS-1$
                return false;
            }

            //�ύ��Ϣ��ReviewBoard������
            if( null == reviewboardClient ){
                MessageDialog.openError(getShell(), 
                        RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                        RbSubclipseMessages.getString("ERROR_SERVER_REQURIED")); //$NON-NLS-1$
                return false;
            }
            
            ModelReviewRequest reviewRequest = null;
            //������޸�
            if(mainPage.isUpdate()){
                //�Ȳ��ҳ�
                reviewRequest = mainPage.getReviewRequest();
                //���ö�����Ϣ
                if( rbConfig.isAllowOptionalPageCommit() ){
                    requestOptionInfoWizardPage.setReviewRequestOptionalInfo(reviewRequest);
                }
            }else{//���������
                reviewRequest = mainPage.getReviewRequest();
                //���ö�����Ϣ
                if( rbConfig.isAllowOptionalPageCommit() ){
                    requestOptionInfoWizardPage.setReviewRequestOptionalInfo(reviewRequest);
                }
            }

            if( null != reviewRequest
                    && null != reviewRequest.getRepository() 
                    && null != reviewRequest.getRepository().getPath() 
                    && !reviewRequest.getRepository().getPath().trim().isEmpty()){
                try {
                    boolean repositoryCorrect = true;
                    if( null == resourcesSelectedByUser || resourcesSelectedByUser.length == 0 ){
                        //��һ�ֺ�����ķ�ʽȡ��url��ص�ֵ������֧��֧��SVN��Դ���д򿪵���ʷ��¼
                        if( !this.getSvnHistoryUrl().toLowerCase().startsWith(reviewRequest.getRepository().getPath().trim())){
                            repositoryCorrect = false;
                        }
                    }
                    else{
                        IResource[] mainPageSelectedResources = getResources();
                        String svnUrlProject = RbSVNUrlUtils.getSVNUrlForProject((mainPageSelectedResources[0]));
                        if( !svnUrlProject.trim().toLowerCase().startsWith(reviewRequest.getRepository().getPath().trim().toLowerCase())){
                            repositoryCorrect = false;
                        }
                    }
                    
                    if( !repositoryCorrect ){
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
            
            //������޸�
            if(mainPage.isUpdate()){
                
                if( null == reviewRequest || reviewRequest.getId() == 0 ){
                    MessageDialog.openError(getShell(), 
                            RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                            MessageFormat.format(RbSubclipseMessages.getString("ERRO_REQUEST_0"), new Object[]{ String.valueOf(mainPage.getRequestId()) }));
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
                
                //���޸�����
                reviewboardService.updateReviewRequestFilterNoValue(reviewRequest, new NullProgressMonitor());
                //���ϴ�diff
                reviewboardService.uploadDiff(reviewRequest.getId(), diffUploadBaseUrl, fileDiffs, null, new NullProgressMonitor());
                
                //�����publish
                if( !mainPage.isDraft() ){
                    reviewboardService.publishReviewRequest(reviewRequest.getId(), new NullProgressMonitor());
                }
                
                CloseDialogWithHttpLink closeDialogWithLink = new CloseDialogWithHttpLink(getShell(), 
                        ReviewboardUtil.norminateRepositoryUrl(reviewboardClient.getServerUrl())+"/r/"+reviewRequest.getId(), 
                        RbSubclipseMessages.getString("SUCCESS_UPDATE"),
                        RbSubclipseMessages.getString("SUCCESS_UPDATE_1")+reviewRequest.getId() +"");
                closeDialogWithLink.open();
                
            }
            else{//���������
                
                ModelReviewRequest reviewRequestNew = reviewboardService.newReviewRequestAll( reviewRequest, 
                        diffUploadBaseUrl, fileDiffs, new NullProgressMonitor());
                
                //�����publish
                if( !mainPage.isDraft()){
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
                    RbSubclipseMessages.getString("ERROR_COMMIT_0") + e.getMessage()); //$NON-NLS-1$
            return false;
        } catch (Exception e) {
            MessageDialog.openError(getShell(), 
                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                    RbSubclipseMessages.getString("ERROR_COMMIT_0") + e.getMessage()); //$NON-NLS-1$
            return false;
        }
    }
    
    public String getSvnHistoryUrl() {
        return svnHistoryUrl;
    }

    public void setSvnHistoryUrl(String svnHistoryUrl) {
        this.svnHistoryUrl = svnHistoryUrl;
    }

    public String getSvnHistoryUrlForBaseUpload() {
        return svnHistoryUrlForBaseUpload;
    }

    public void setSvnHistoryUrlForBaseUpload(String svnHistoryUrlForBaseUpload) {
        this.svnHistoryUrlForBaseUpload = svnHistoryUrlForBaseUpload;
    }
    
    public boolean getSvnHistoryUrlIsFolder() {
        return this.svnHistoryUrlIsFolder;
    }

    public void setSvnHistoryUrlIsFolder(boolean svnHistoryUrlIsFolder) {
        this.svnHistoryUrlIsFolder = svnHistoryUrlIsFolder;
    }

}
