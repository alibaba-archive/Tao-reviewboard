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

import org.eclipse.jface.action.ToolBarManager;

/**
 * 类说明:Resuorce Selection Tree 下的ToolBarControlCreator
 * 
 * @author 智清 
 * 创建时间：2011-4-8
 */
public interface IToolbarControlCreator {
    
    public void createToolbarControls(ToolBarManager toolbarManager);
    
    public int getControlCount();
    
}
