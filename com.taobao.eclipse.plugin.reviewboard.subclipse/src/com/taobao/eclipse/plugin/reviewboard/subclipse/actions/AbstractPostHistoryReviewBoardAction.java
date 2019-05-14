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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.actions.WorkbenchWindowAction;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IReviewboardClientRepository;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.clientmanager.SVNClientRepositoryAwair;

/**
 * 类说明:History Post-commit Action的基类。<BR/>
 * 有两种情况：<BR/>
 * （1）右键Team，显示资源历史记录<BR/>
 * （2）尽可能支持 从SVN资源库直接打开历史记录的情况(这种情况下无法获得IResource)<BR/>
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public abstract class AbstractPostHistoryReviewBoardAction extends WorkbenchWindowAction  {
    
    protected long startVersion = 0;
    protected long stopVersion = 0;
    protected IResource[] selectedResources;
    protected IReviewboardClient reviewboardClient = null;
    protected ISVNResource[] selectedSVNResources = null;
    
    /**
     * 打开页面向导
     */
    public abstract void openAndSetWizard();
    
    protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
        //先初始化变量
        startVersion = 0;
        stopVersion = 0;
        reviewboardClient = null;
        
        SVNRevision fromRevision = null;
        SVNRevision toRevision = null;
        Object[] selectedObjects = selection.toArray();
        
        //请求方式：（1）右键Team，显示资源历史记录
        if (selectedObjects[0] instanceof ILogEntry) {
            selectedSVNResources = new ISVNResource[2];
            selectedSVNResources[0] = ((ILogEntry)selectedObjects[0]).getResource();
            toRevision = ((ILogEntry)selectedObjects[0]).getRevision();
            
            if (selectedObjects.length > 1) {
                selectedSVNResources[1] = ((ILogEntry)selectedObjects[1]).getResource();
                fromRevision = ((ILogEntry)selectedObjects[1]).getRevision();                  
            }
        }
        
        //请求方式：（2）从SVN资源库直接打开历史记录的情况(这种情况下无法获得IResource)
        if ( null == selectedSVNResources || selectedSVNResources.length == 0) {
            selectedSVNResources = getSelectedRemoteResources();
            if ( null != selectedSVNResources && selectedSVNResources.length > 0 ) {
                if (selectedSVNResources[0] instanceof ISVNRemoteResource)
                    toRevision = ((ISVNRemoteResource)selectedSVNResources[0]).getRevision();
                if (selectedSVNResources.length > 1 && selectedSVNResources[1] instanceof ISVNRemoteResource)
                    fromRevision = ((ISVNRemoteResource)selectedSVNResources[1]).getRevision();  
            }
        }
        
        if( null == selectedSVNResources || selectedSVNResources.length == 0 ){
            return ;
        }

        ArrayList<IResource> selectedResourceList = new ArrayList<IResource>();
        for( ISVNResource svnResourceTmp : selectedSVNResources ){
            if( null == svnResourceTmp ){
                continue;
            }
            IResource resource = svnResourceTmp.getResource();
            if( null == resource ){
                continue;
            }
            //确认resourceList已经不包含重复值
            boolean isExsit = false;
            for( IResource resourceTmp : selectedResourceList){
                if( resourceTmp.getLocation().toString().equals(resource.getLocation().toString())){
                    isExsit = true;
                    break;
                }
            }
            if( !isExsit ){
                selectedResourceList.add( resource );
            }
        
        }
        selectedResources = selectedResourceList.toArray( new IResource[selectedResourceList.size()] );

        final IReviewboardClientRepository reviewboardClientRepository = SVNClientRepositoryAwair.getReviewboardClientRepository();
        
        //友好化用户体验
        if( null == reviewboardClientRepository.getClientFromUrlToClientMap()){
            run(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    if( null != reviewboardClientRepository ){
                        if( null != monitor ){
                            monitor.setTaskName(RbSubclipseMessages.getString("PROGRESS_READINGCACHE")); //$NON-NLS-1$
                            monitor.worked(1); 
                        } 
                        try {
                            reviewboardClient = reviewboardClientRepository.getClient(false, monitor);
                        } catch (OperationCanceledException e) {
                            return ;
                        }
                    }
                }
            }, true, PROGRESS_DIALOG);
        }else{
            try {
                reviewboardClient = reviewboardClientRepository.getClient(false, null);
            } catch (OperationCanceledException e) {
                return ;
            }
        }
        
        if( null == reviewboardClient ){ 
            MessageDialog.openError(getShell(), 
                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"), 
                    RbSubclipseMessages.getString("ERROR_SERVER_NOT_CONFIFIGURED")); //$NON-NLS-1$
            return;
        }
        
        if( null != fromRevision ){
            try {
                startVersion = Integer.parseInt(fromRevision.toString());
            } catch (NumberFormatException e) {
            }
        }
        if( null != toRevision ){
            try {
                stopVersion = Integer.parseInt(toRevision.toString());
            } catch (NumberFormatException e) {
            }
        }
        
        if( startVersion > stopVersion ){
            long temp = startVersion;
            startVersion = stopVersion;
            stopVersion = temp;
        }
        
        openAndSetWizard();
    }

    protected boolean isEnabled() throws TeamException {
        Object[] selectedObjects = selection.toArray();
        if (selectedObjects.length == 0 || selectedObjects.length > 2){
            return false;
        }
        ISVNResource svnResource1 = null;
        ISVNResource svnResource2 = null;
        if (selectedObjects[0] instanceof ISVNResource){
            svnResource1 = (ISVNResource)selectedObjects[0];
        }
        else {
            if (selectedObjects[0] instanceof ILogEntry)
                svnResource1 = ((ILogEntry)selectedObjects[0]).getResource();
        }
        if (svnResource1 == null){
            return false;
        }
        if (selectedObjects.length > 1) {
            if (selectedObjects[1] instanceof ISVNResource){
                svnResource2 = (ISVNResource)selectedObjects[1];
            }
            else {
                if (selectedObjects[1] instanceof ILogEntry)
                    svnResource2 = ((ILogEntry)selectedObjects[1]).getResource();       
            }
            if (!svnResource1.getRepository().getRepositoryRoot().toString().equals(
                    svnResource2.getRepository().getRepositoryRoot().toString())){
                return false;
            }
            return (svnResource1.isFolder() == svnResource2.isFolder());            
        }
        return true;
    }

    /*
     * @see org.tigris.subversion.subclipse.ui.actions.ReplaceableIconAction#getImageId()
     */
    protected String getImageId() {
        return ISVNUIConstants.IMG_MENU_DIFF;
    }
}
