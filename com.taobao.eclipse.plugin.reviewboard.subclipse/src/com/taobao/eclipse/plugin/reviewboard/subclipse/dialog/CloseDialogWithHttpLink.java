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

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;

/**
 * ��˵��:�ر�ʱ�ĶԻ����ҶԻ�������һ��URL
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class CloseDialogWithHttpLink extends Dialog {

    private String url;
    
    private String title;
    
    private String information;
    
    private Color informationColor;

    public CloseDialogWithHttpLink(Shell parentShell, String url, String title, String information) {
        super(parentShell);
        this.url = ReviewboardUtil.norminateRepositoryUrl(url);
        this.title = title;
        if( null == title || title.trim().isEmpty() ){
            this.title = RbSubclipseMessages.getString("PROMPTING_INFORMATION_LABLE");
        }
        this.information = information;
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.numColumns = 1;

        
        if( null != information && !information.trim().isEmpty() ){
            Label labelInfo = new Label(container, SWT.SHADOW_NONE);
            if( null != this.getInformationColor() ){
                labelInfo.setForeground(this.getInformationColor());
            }
            labelInfo.setText(information);
        }
        
        Link link = new Link(container, SWT.SHADOW_NONE);
        link.setText("<a>" + url + "</a>");
        //���������¼�
        link.addSelectionListener(new SelectionAdapter(){
          public void widgetSelected(SelectionEvent event){
            //��������Ӻ�Ķ���...
            try {
                IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
                support.getExternalBrowser().openURL(new URL(url));
            } catch (Exception e) {
            }
          }
        });

        return container;
    }

    /**
     * Create contents of the button bar.
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(450, 160);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    public Color getInformationColor() {
        return informationColor;
    }

    public void setInformationColor(Color informationColor) {
        this.informationColor = informationColor;
    }

}
