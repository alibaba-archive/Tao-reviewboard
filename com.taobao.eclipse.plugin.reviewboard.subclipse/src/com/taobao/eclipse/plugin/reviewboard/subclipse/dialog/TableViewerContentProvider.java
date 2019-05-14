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
package com.taobao.eclipse.plugin.reviewboard.subclipse.dialog;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 类说明:postcommit内容器.此类对List集合记录进行筛选转换.
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class TableViewerContentProvider implements IStructuredContentProvider {

	@SuppressWarnings("unchecked")
    public Object[] getElements(Object arg0) {
		if(arg0 instanceof List){
			return ((List)arg0).toArray();//将数据集List转换为数组
		}
		else{
			return new Object[0];//如果非List类型则返回一个空数组
		}
	}

	public void dispose() {

	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

	}

}
