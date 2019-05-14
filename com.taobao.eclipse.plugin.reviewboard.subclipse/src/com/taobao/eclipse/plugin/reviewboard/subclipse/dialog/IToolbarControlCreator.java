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

import org.eclipse.jface.action.ToolBarManager;

/**
 * ��˵��:Resuorce Selection Tree �µ�ToolBarControlCreator
 * 
 * @author ���� 
 * ����ʱ�䣺2011-4-8
 */
public interface IToolbarControlCreator {
    
    public void createToolbarControls(ToolBarManager toolbarManager);
    
    public int getControlCount();
    
}
