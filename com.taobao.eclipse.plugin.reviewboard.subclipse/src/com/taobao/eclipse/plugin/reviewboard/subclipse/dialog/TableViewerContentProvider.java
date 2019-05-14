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

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * ��˵��:postcommit������.�����List���ϼ�¼����ɸѡת��.
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class TableViewerContentProvider implements IStructuredContentProvider {

	@SuppressWarnings("unchecked")
    public Object[] getElements(Object arg0) {
		if(arg0 instanceof List){
			return ((List)arg0).toArray();//�����ݼ�Listת��Ϊ����
		}
		else{
			return new Object[0];//�����List�����򷵻�һ��������
		}
	}

	public void dispose() {

	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {

	}

}
