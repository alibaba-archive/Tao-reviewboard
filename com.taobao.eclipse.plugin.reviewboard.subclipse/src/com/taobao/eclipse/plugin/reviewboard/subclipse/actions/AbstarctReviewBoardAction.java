/*******************************************************************************
 * Copyright (c) 2003, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.commands.GetStatusCommand;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.actions.WorkspaceAction;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IReviewboardClientRepository;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.clientmanager.SVNClientRepositoryAwair;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;

/**
 * 类说明:Pre-commit Action的基类
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings("unchecked")
public abstract class AbstarctReviewBoardAction extends WorkspaceAction {

    protected IResource[] modifiedResources;
    protected List unaddedList;
    protected IResource[] resourcesSelectedByUser;
    protected HashMap statusMap;
    protected IReviewboardClient reviewboardClient = null;
    
    /**
     * 其它验证
     */
    protected abstract boolean validateOther();
    
    /**
     * 打开页面向导
     */
    protected abstract void openAndSetWizard();

    /**
     * 设置资源文件
     */
    protected abstract boolean setOperationResource()throws InvocationTargetException, InterruptedException;

    
    /**
     * Returns the selected resources.
     * 
     * @return the selected resources
     */
    protected IResource[] getSelectedResourcesByUser() {
        ArrayList resourceArray = new ArrayList();
        IResource[] resources = (IResource[])getSelectedResources(IResource.class);
        for (int i = 0; i < resources.length; i++) resourceArray.add(resources[i]);
        ResourceMapping[] resourceMappings = (ResourceMapping[])getSelectedAdaptables(selection, ResourceMapping.class);
        for (int i = 0; i < resourceMappings.length; i++) {
            ResourceMapping resourceMapping = (ResourceMapping)resourceMappings[i];
            try {
                ResourceTraversal[] traversals = resourceMapping.getTraversals(null, null);
                for (int j = 0; j < traversals.length; j++) {
                    IResource[] traversalResources = traversals[j].getResources();
                    for (int k = 0; k < traversalResources.length; k++) {
                        if (!resourceArray.contains(traversalResources[k]))
                            resourceArray.add(traversalResources[k]);
                    }
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }       
        IResource[] selectedResources = new IResource[resourceArray.size()];
        resourceArray.toArray(selectedResources);
        return selectedResources;
    }
    
    /** (Non-javadoc)
     * Method declared on IActionDelegate.
     * @throws InterruptedException 
     * @throws InvocationTargetException 
     */
    public void execute(IAction action) throws InvocationTargetException, InterruptedException {
        statusMap = new HashMap();
        unaddedList = new ArrayList();
        if( !setOperationResource()){
            return ;
        }
        
        //当跨越多个Project时，应该确保隶属于同一个Repository，不然无法生成ReviewBoard能够识别的diff
        Set< IProject > projectSet = new HashSet< IProject >();
        if ( null != resourcesSelectedByUser && resourcesSelectedByUser.length > 0) {
            for( IResource resourcesTmp : resourcesSelectedByUser ){
                projectSet.add( resourcesTmp.getProject() );
            }
        }
        try {
            if( !RbSVNUrlUtils.isProjectWithSameSVNRepository( projectSet ) ){
                MessageDialog.openError(getShell(), 
                        RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                        RbSubclipseMessages.getString("ERROR_PROJECT_NOTSAME_REPOSITORY")); //$NON-NLS-1$
                return ;
            }
        } catch (ReviewboardException e) {
            MessageDialog.openError(getShell(), 
                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                    RbSubclipseMessages.getString("ERROR_PREFIX") + e.getMessage()); //$NON-NLS-1$
            return ;
        }
        
        //计算相关变量，准备进入向导页
        run(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                 try {
                    modifiedResources = getModifiedResources(resourcesSelectedByUser, monitor);
                } catch (SVNException e) {
                    e.printStackTrace();
                }       
            }
        }, true, PROGRESS_BUSYCURSOR);
        
        if( !validateOther() ){
            return ;
        }
        
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

        openAndSetWizard();
        
    }
    
    protected boolean isEnabled() throws TeamException {
        boolean isEnabled = super.isEnabled();
        return isEnabled;
    }

    /**
     * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForMultipleResources()
     */
    protected boolean isEnabledForMultipleResources() {
        return true;
    }

    /**
     * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForUnmanagedResources()
     */
    protected boolean isEnabledForUnmanagedResources() {
        return true;
    }

    /*
     * @see org.tigris.subversion.subclipse.ui.actions.ReplaceableIconAction#getImageId()
     */
    protected String getImageId() {
        return ISVNUIConstants.IMG_MENU_DIFF;
    }
    
    protected IResource[] getModifiedResources(IResource[] resources, IProgressMonitor iProgressMonitor) throws SVNException {
        final List modified = new ArrayList();
        List unversionedFolders = new ArrayList();
        for (int i = 0; i < resources.length; i++) {
             IResource resource = resources[i];
             ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
             
             // This check is for when the action is called with unmanaged resources
             if (svnResource.getRepository() == null) {
                 continue;
             }
             
             // get adds, deletes, updates and property updates.
             GetStatusCommand command = new GetStatusCommand(svnResource, true, false);
             command.run(iProgressMonitor);
             ISVNStatus[] statuses = command.getStatuses();
             for (int j = 0; j < statuses.length; j++) {
                 if (SVNStatusUtils.isReadyForCommit(statuses[j]) || SVNStatusUtils.isMissing(statuses[j])) {
                     IResource currentResource = SVNWorkspaceRoot.getResourceFor(resource, statuses[j]);
                     if (currentResource != null) {
                         ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(currentResource);
                         if (!localResource.isIgnored()) {
                             if (!SVNStatusUtils.isManaged(statuses[j])) {
                                if (!isSymLink(currentResource)) {
                                    if (currentResource.getType() != IResource.FILE)
                                        unversionedFolders.add(currentResource);
                                    else
                                        if (!modified.contains(currentResource)) {
                                            modified.add(currentResource);
                                            if (currentResource instanceof IContainer){
                                                statusMap.put(currentResource, statuses[j].getPropStatus());
                                            }
                                            else{
                                                statusMap.put(currentResource, statuses[j].getTextStatus());
                                            }
                                            if (addToUnadded(currentResource)){
                                                unaddedList.add(currentResource);
                                            }
                                        }
                                }
                             } else
                                 if (!modified.contains(currentResource)) {
                                     modified.add(currentResource);
                                     if (currentResource instanceof IContainer){
                                         statusMap.put(currentResource, statuses[j].getPropStatus());
                                     }
                                     else{
                                         statusMap.put(currentResource, statuses[j].getTextStatus());
                                     }
                                 }
                         }
                     }
                 }
             }
        }
        // get unadded resources and add them to the list.
        IResource[] unaddedResources = getUnaddedResources(unversionedFolders, iProgressMonitor);
        for (int i = 0; i < unaddedResources.length; i++) {
            if (!modified.contains(unaddedResources[i])) {
                if (unaddedResources[i].getType() == IResource.FILE) {
                    modified.add(unaddedResources[i]);
                    statusMap.put(unaddedResources[i], SVNStatusKind.UNVERSIONED);
                }
                if (addToUnadded(unaddedResources[i])) unaddedList.add(unaddedResources[i]);
            }
        }
        return (IResource[]) modified.toArray(new IResource[modified.size()]);
    }   
    
    protected IResource[] getUnaddedResources(List resources, IProgressMonitor iProgressMonitor) throws SVNException {
        final List unadded = new ArrayList();
        final SVNException[] exception = new SVNException[] { null };
        for (Iterator iter = resources.iterator(); iter.hasNext();) {
            IResource resource = (IResource) iter.next();
            if (resource.exists()) {
                // visit each resource deeply
                try {
                    resource.accept(new IResourceVisitor() {
                    public boolean visit(IResource aResource) {
                        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(aResource);
                        // skip ignored resources and their children
                        try {
                            if (svnResource.isIgnored()){
                                return false;
                            }
                            // visit the children of shared resources
                            if (svnResource.isManaged()){
                                return true;
                            }
                            if ((aResource.getType() == IResource.FOLDER) && isSymLink(aResource)){ // don't traverse into symlink folders
                                return false;
                            }
                        } catch (SVNException e) {
                            exception[0] = e;
                        }
                        // file/folder is unshared so record it
                        unadded.add(aResource);
                        return aResource.getType() == IResource.FOLDER;
                    }
                }, IResource.DEPTH_INFINITE, false /* include phantoms */);
                } catch (CoreException e) {
                    throw SVNException.wrapException(e);
                }
                if (exception[0] != null){
                    throw exception[0];
                }
            }
        }
        return (IResource[]) unadded.toArray(new IResource[unadded.size()]);
    }
        
    
    protected boolean isSymLink(IResource resource) {
        File file = resource.getLocation().toFile();
        try {
            if (!file.exists())
                return true;
            else {
                String cnnpath = file.getCanonicalPath();
                String abspath = file.getAbsolutePath();
                return !abspath.equals(cnnpath);
            }
        } catch(IOException ex) {
          return true;
        }   
    }
    
    private boolean addToUnadded(IResource resource) {
        IResource parent = resource;
        while (parent != null) {
            parent = parent.getParent();
            if (unaddedList.contains(parent)) return false;
        }
        return true;
    }

}
