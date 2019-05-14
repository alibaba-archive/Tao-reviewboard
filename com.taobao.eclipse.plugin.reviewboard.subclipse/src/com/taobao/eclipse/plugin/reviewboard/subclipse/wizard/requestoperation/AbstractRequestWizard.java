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
package com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.service.IReviewboardService;
import com.taobao.eclipse.plugin.reviewboard.core.service.impl.ReviewboardServiceImpl;

/**
 * 类说明:RequestWizard的基类
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings("unchecked")
public abstract class AbstractRequestWizard extends Wizard {

    protected IWorkbenchPart targetPart;
    protected IWorkbenchPage targetPage;
    
    protected IStructuredSelection selection;
    protected IResource[] unaddedResources;
    protected HashMap statusMap;
    protected IResource[] selectedResources;
    protected IResource[] resourcesSelectedByUser;
    
    protected IReviewboardClient reviewboardClient;
    
    protected IReviewboardService reviewboardService;
    
    protected Long[] startAndStopVersion;
    
    protected abstract IResource[] getResources();
    
    protected AbstractRequestWizard( IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            IResource[] resourcesSelectedByUser, 
            IReviewboardClient reviewboardClient, long startVersion, long stopVersion) {
        this(targetPart, targetPage, null, null, resourcesSelectedByUser, null, reviewboardClient);
        if(startVersion <= 0 && stopVersion <= 0){
            ;
        }else{
            startAndStopVersion = new Long[]{startVersion,stopVersion};
        }
    }
    
    protected AbstractRequestWizard( IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            IStructuredSelection selection, IResource[] unaddedResources, 
            IResource[] resourcesSelectedByUser, HashMap statusMap, IReviewboardClient reviewboardClient) {
        super();
        this.targetPart = targetPart;
        this.targetPage = targetPage;
        this.selection = selection;
        this.unaddedResources = unaddedResources;
        this.statusMap = statusMap;
        this.resourcesSelectedByUser = resourcesSelectedByUser;
        this.reviewboardClient = reviewboardClient;
        this.reviewboardService = new ReviewboardServiceImpl( reviewboardClient );
        initializeDefaultPageImageDescriptor();
    }
    
    /**
     * Initializes this creation wizard using the passed workbench and
     * object selection.
     *
     * @param workbench the current workbench
     * @param selection the current object selection
     */
    protected void init(IWorkbench workbench, IStructuredSelection selection) {
    }
    
    /**
     * Declares the wizard banner iamge descriptor
     */
    protected void initializeDefaultPageImageDescriptor() {
        String iconPath;
        iconPath = "icons/full/"; //$NON-NLS-1$
        try {
            URL installURL = SVNUIPlugin.getPlugin().getBundle().getEntry("/"); //$NON-NLS-1$
            URL url = new URL(installURL, iconPath + "wizards/newconnect_wiz.gif"); //$NON-NLS-1$
            ImageDescriptor desc = ImageDescriptor.createFromURL(url);
            setDefaultPageImageDescriptor(desc);
        } catch (MalformedURLException e) {
            // Should not happen.  Ignore.
        }
    }
    
    @Override
    public boolean needsProgressMonitor() {
        return true;
    }

    protected IResource[] getUnaddedResources() {
        ArrayList unaddedResourceList = new ArrayList();
        for (int i = 0; i < unaddedResources.length; i++)
            unaddedResourceList.add(unaddedResources[i]);
        ArrayList selectedUnaddedResourceList = new ArrayList();
        IResource[] selectedResources = getResources();
        for (int i = 0; i < selectedResources.length; i++) {
            if (unaddedResourceList.contains(selectedResources[i])) {
                selectedUnaddedResourceList.add(selectedResources[i]);
            } else {
                IResource unaddedParent = getUnaddedParent(selectedResources[i], unaddedResourceList);
                if (unaddedParent != null && !selectedUnaddedResourceList.contains(unaddedParent))
                    selectedUnaddedResourceList.add(unaddedParent);
            }
        }
        IResource[] unaddedResourceArray = new IResource[selectedUnaddedResourceList.size()];
        selectedUnaddedResourceList.toArray(unaddedResourceArray);
        return unaddedResourceArray;
    }
    
    protected IResource getUnaddedParent(IResource resource, ArrayList unaddedResourceList) {
        IResource parent = resource;
        while (parent != null) {
            parent = parent.getParent();
            int index = unaddedResourceList.indexOf(parent);
            if (index != -1) return (IResource)unaddedResourceList.get(index);
        }
        return null;
    }

    public void setSelectedResources(IResource[] selectedResources) {
        this.selectedResources = selectedResources;
    }
    
    
    
}
