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
package com.taobao.eclipse.plugin.reviewboard.subclipse.dialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;

/**
 * ��˵��:postcommit������ITableLabelProvider
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class ResourceTableViewerLabelProvider implements ITableLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 * ����ÿ����¼ǰ���ͼ��
	 */
	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object arg0, int arg1) {
		IResource resource = (IResource)arg0;
		String resourcePath = resource.getProjectRelativePath().toString();
		if(arg1==0){
            IProject project =  resource.getProject();
		    if( null == resourcePath || resourcePath.trim().isEmpty()){
		        resourcePath = project.getName()+"(" + RbSubclipseMessages.getString("PROJECTS_SELECTED") + project.getName() + ")";
		    }else{
		        resourcePath = resourcePath + "(" + project.getName() + ")";
		    }
		    return resourcePath;
		}
		return "";
	}

	public void addListener(ILabelProviderListener arg0) {

	}

	public void dispose() {

	}

	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	public void removeListener(ILabelProviderListener arg0) {

	}

}
