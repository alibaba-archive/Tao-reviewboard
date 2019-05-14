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
package com.taobao.eclipse.plugin.reviewboard.subclipse.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.tigris.subversion.subclipse.ui.Policy;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.WizardDialogReviewBoardLocation;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.precommit.PreCommitRequestWizard;

/**
 * 类说明:提交ReviewBoard Action(pre-commit)，入口。
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings({"unchecked"})
public class PreCommitReviewBoardAction extends AbstarctReviewBoardAction {
    
    @Override
    protected boolean validateOther(){
        //modifiedResources不存在
        if (modifiedResources == null || modifiedResources.length == 0) {
            MessageDialog.openInformation(getShell(), 
                    RbSubclipseMessages.getString("UPDATEORCOMMIT_PRE_LABLE"), 
                    Policy.bind("GenerateSVNDiff.noDiffsFoundMsg")); //$NON-NLS-1$ 
            return false;
        }
        return true;
    }

    @Override
    protected boolean setOperationResource() throws InvocationTargetException, InterruptedException {
        resourcesSelectedByUser = getSelectedResources();
        return true;
    }

    @Override
    protected void openAndSetWizard() {
        IResource[] unaddedResources = new IResource[unaddedList.size()];
        unaddedList.toArray(unaddedResources);
        
        //进入向导页
        PreCommitRequestWizard wizardCommitRequestWizard = new PreCommitRequestWizard( getTargetPart(), getTargetPage(),
                 null == modifiedResources ? null : new StructuredSelection(modifiedResources), 
                 unaddedResources, resourcesSelectedByUser, statusMap, reviewboardClient);
        wizardCommitRequestWizard.setWindowTitle(RbSubclipseMessages.getString("PreReviewBoardAction.dialogTitle"));
        wizardCommitRequestWizard.setSelectedResources(getSelectedResources());
        int height = 580;
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        if( rbConfig.isAllowCompareVersionForPreCommit() ){
            height += 70;
        }
        if( rbConfig.isAllowOptionBugClosed() ){
            height += 30;
        }
        WizardDialog dialogCommitRequestWizard = new WizardDialogReviewBoardLocation(getShell(), 
                wizardCommitRequestWizard, 820, height); //$NON-NLS-1$
        dialogCommitRequestWizard.setMinimumPageSize(350, 500);
        dialogCommitRequestWizard.open();
    }
    
}
