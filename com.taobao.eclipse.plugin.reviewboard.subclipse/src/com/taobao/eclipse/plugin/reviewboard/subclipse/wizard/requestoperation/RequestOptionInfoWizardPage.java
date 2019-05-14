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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequest;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;

/**
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class RequestOptionInfoWizardPage extends WizardPage {
    private Text textTestingDone;

    /**
     * Create the wizard.
     */
    public RequestOptionInfoWizardPage() {
        super(RbSubclipseMessages.getString("OPTIONAL.dialogTitle"));
        setTitle(RbSubclipseMessages.getString("OPTIONAL.dialogTitle"));
        setDescription("");
    }

    /**
     * Create contents of the wizard.
     * @param parent
     */
    public void createControl(Composite parent) {

        Composite composite= new Composite(parent, SWT.NULL);
        GridLayout layout= new GridLayout();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        setControl(composite);
        initializeDialogUnits(composite);

        Group groupTestingDone = new Group(composite, SWT.NONE);
        GridLayout layoutGroupTestingDone = new GridLayout();
        layoutGroupTestingDone.numColumns = 1;
        groupTestingDone.setLayout(layoutGroupTestingDone);
        GridData gdGroupTestingDone = new GridData(GridData.FILL_HORIZONTAL);
        gdGroupTestingDone.horizontalSpan = 1;
        groupTestingDone.setLayoutData(gdGroupTestingDone);
        
        Label lblTheTestingDone = new Label(groupTestingDone, SWT.NONE);
        lblTheTestingDone.setText(RbSubclipseMessages.getString("PAGE_TESTINGDONE"));
        
        textTestingDone = new Text(groupTestingDone, SWT.BORDER | SWT.MULTI);
        GridData gridData_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_2.heightHint = 139;
        gridData_2.widthHint = 720;
        textTestingDone.setLayoutData(gridData_2);
        
    }
    
    /**
     * 返回补充了Optional信息后的Request
     * @return
     */
    public void setReviewRequestOptionalInfo(ModelReviewRequest reviewRequestNew) {
        if( null == reviewRequestNew ){
            reviewRequestNew = new ModelReviewRequest();
        }
        reviewRequestNew.setTestingDone(textTestingDone.getText());
    }

}
