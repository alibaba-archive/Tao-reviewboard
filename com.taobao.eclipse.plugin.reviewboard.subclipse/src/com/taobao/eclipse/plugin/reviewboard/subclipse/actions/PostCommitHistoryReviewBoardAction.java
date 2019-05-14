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
package com.taobao.eclipse.plugin.reviewboard.subclipse.actions;

import org.eclipse.jface.wizard.WizardDialog;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.WizardDialogReviewBoardLocation;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.postcommit.PostCommitRequestWizard;

/**
 * ��˵��:��ʷ��¼�����ύReviewBoard Action����ڡ�
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class PostCommitHistoryReviewBoardAction extends AbstractPostHistoryReviewBoardAction {

    @Override
    public void openAndSetWizard() {
        //������ҳ
        PostCommitRequestWizard wizardCommitRequestWizard = new PostCommitRequestWizard( 
                getTargetPart(), getTargetPage(), selectedResources, reviewboardClient, startVersion, stopVersion);
        wizardCommitRequestWizard.setWindowTitle(RbSubclipseMessages.getString("PostReviewBoardAction.dialogTitle"));
        wizardCommitRequestWizard.setSelectedResources(getSelectedResources());
        
        if( null != selectedSVNResources && selectedSVNResources.length > 0 ){
            wizardCommitRequestWizard.setSvnHistoryUrl(selectedSVNResources[0].getUrl().toString());
            wizardCommitRequestWizard.setSvnHistoryUrlIsFolder(selectedSVNResources[0].isFolder());
            if( selectedSVNResources[0].isFolder() ){
                wizardCommitRequestWizard.setSvnHistoryUrlForBaseUpload(selectedSVNResources[0].getUrl().toString());
            }else{
                wizardCommitRequestWizard.setSvnHistoryUrlForBaseUpload(selectedSVNResources[0].getUrl().getParent().toString());
            }
        }
        
        int height = 645;
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        if( rbConfig.isAllowOptionBugClosed() ){
            height += 30;
        }
        
        WizardDialog dialogCommitRequestWizard = new WizardDialogReviewBoardLocation(getShell(), wizardCommitRequestWizard, 820, height); //$NON-NLS-1$
        dialogCommitRequestWizard.setMinimumPageSize(350, 500);
        dialogCommitRequestWizard.open();
    }
    
}