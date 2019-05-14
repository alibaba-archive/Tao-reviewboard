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
package com.taobao.eclipse.plugin.reviewboard.subclipse.wizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class WizardDialogReviewBoardLocation  extends WizardDialog{
    
    private Shell shell;
    
    private int width;
    private int height;
    
    public WizardDialogReviewBoardLocation(Shell parentShell, IWizard newWizard, int width, int height) {
        super(parentShell, newWizard);
        this.shell = parentShell;
        this.width = width;
        this.height = height;
        //如果小于0，随便设置一个值
        if( this.width < 0 ){
            this.width = 400;
        }
        if( this.height < 0 ){
            this.height = 250;
        }
    }

    protected void cancelPressed() {
        super.cancelPressed();
    }

    protected void okPressed() {
        super.okPressed();
    }

    protected Point getInitialLocation(Point initialSize) {
        try {
            int width = shell.getMonitor().getClientArea().width;
            int height = shell.getMonitor().getClientArea().height;
            int xp = (width - this.width) / 2;
            int yp = (height - this.height) / 2;
            if( xp < 0 ) xp = 0;
            if( yp < 0 ) yp = 0;
            return new Point(xp, yp);
        } catch (NumberFormatException e) {}
        return super.getInitialLocation(initialSize);
    }
    
    protected Point getInitialSize() {
        try {
            return new Point(this.width, this.height);
        } catch (NumberFormatException e) {}        
         return super.getInitialSize();
    }
}
