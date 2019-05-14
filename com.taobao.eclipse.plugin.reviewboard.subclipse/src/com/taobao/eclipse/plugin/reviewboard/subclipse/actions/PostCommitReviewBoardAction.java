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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;

import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.WizardDialogReviewBoardLocation;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.postcommit.PostCommitRequestWizard;

/**
 * 类说明:提交ReviewBoard Action(pre-commit)，入口。
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings({"unchecked"})
public class PostCommitReviewBoardAction extends AbstarctReviewBoardAction {
    
    @Override
    protected boolean validateOther(){
        return true;
    }

    @Override
    protected boolean setOperationResource() throws InvocationTargetException, InterruptedException{
        IResource[] resourcesSelectedByUserTmp = getSelectedResourcesByUser();
        List<IResource> resourcesSelectedByUserList = new ArrayList<IResource>();
        if( null != resourcesSelectedByUserTmp && resourcesSelectedByUserTmp.length > 0 ){
            for( IResource resource : resourcesSelectedByUserTmp ){
                try {
                    if(RbSVNUrlUtils.isResourceHasSVNProperty(resource)){
                        resourcesSelectedByUserList.add(resource);
                    }
                } catch (ReviewboardException e) {
                }
            }
        }
        if( resourcesSelectedByUserList.isEmpty() ){
            resourcesSelectedByUser = null;
            MessageDialog.openError(getShell(), RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                    RbSubclipseMessages.getString("ERROR_FOLDERNOTSVN")); 
            return false;
        }else{
            resourcesSelectedByUser = new IResource[resourcesSelectedByUserList.size()];
            resourcesSelectedByUser = resourcesSelectedByUserList.toArray(resourcesSelectedByUser);
            return true;
        }
    }

    @Override
    protected void openAndSetWizard() {
        IResource[] unaddedResources = new IResource[unaddedList.size()];
        unaddedList.toArray(unaddedResources);
        
        //进入向导页
        PostCommitRequestWizard wizardCommitRequestWizard = new PostCommitRequestWizard( getTargetPart(), getTargetPage(),
                null == modifiedResources ? null : new StructuredSelection(modifiedResources), 
                        resourcesSelectedByUser, statusMap, reviewboardClient);
        wizardCommitRequestWizard.setWindowTitle(RbSubclipseMessages.getString("PostReviewBoardAction.dialogTitle"));
        wizardCommitRequestWizard.setSelectedResources(getSelectedResources());
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
