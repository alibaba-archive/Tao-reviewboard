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
package com.taobao.eclipse.plugin.reviewboard.core.config.preference;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;
import com.taobao.eclipse.plugin.reviewboard.core.ReviewboardCorePlugin;

/**
 * 类说明:ReviewBoard首选项 页面配置高级设置页面
 * 
 * @author 智清 
 * 创建时间：2011-7-13
 */
public class AdvancePagePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String P_COMPAREVESION_PRECOMMIT = "P_COMPAREVESION_PRECOMMIT"; //$NON-NLS-1$

    public static final String P_OPTIONAL_PAGE_COMMIT = "P_OPTIONAL_PAGE_COMMIT"; //$NON-NLS-1$

    public static final String P_OPTIONAL_BUGCLOSED = "P_OPTIONAL_BUGCLOSED"; //$NON-NLS-1$
    
    public static final String P_OPTIONAL_DRAFT = "P_OPTIONAL_DRAFT"; //$NON-NLS-1$
    
    private IPreferenceStore preferenceStore;
    
    private Button btnCompareVesionForPreCommit = null;
    private Button btnOptionalPageCommit = null;
    private Button btnOptionalBugClosed = null;
    private Button btnOptionalDraft = null;
    
    /**
     * Create the preference page.
     */
    public AdvancePagePreferencePage() {
        setPreferenceStore(ReviewboardCorePlugin.getDefault().getPreferenceStore());
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    public Control createContents(Composite parent) {
        
        preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Group group = null;
        
        group = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        group.setLayoutData(gd);
        group.setText(RbCoreMessages.getString("ADV_PAGESHOW_LABLE"));
        
        btnCompareVesionForPreCommit = new Button(group, SWT.CHECK);
        btnCompareVesionForPreCommit.setText(RbCoreMessages.getString("ADV_PAGESHOW_BASEVERSION_SHOW"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        btnCompareVesionForPreCommit.setLayoutData(gd);
        
        btnOptionalPageCommit = new Button(group, SWT.CHECK);
        btnOptionalPageCommit.setText(RbCoreMessages.getString("ADV_PAGESHOW_TESTINGDONE_SHOW"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        btnOptionalPageCommit.setLayoutData(gd);
        
        btnOptionalBugClosed = new Button(group, SWT.CHECK);
        btnOptionalBugClosed.setText(RbCoreMessages.getString("ADV_PAGESHOW_BUGCLOSED_SHOW"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        btnOptionalBugClosed.setLayoutData(gd);
        
        btnOptionalDraft = new Button( group, SWT.CHECK );
        btnOptionalDraft.setText(RbCoreMessages.getString("ADV_PAGESHOW_DRAFT_SHOW"));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        btnOptionalDraft.setLayoutData(gd);
        
        initialize();
        
        return composite;
    }
    
    
    /**
     * 参数初始化
     */
    private void initialize() {

        String allowCompareVersionForPreCommitStr = null;
        String allowOptionalPageCommitStr = null;
        String allowOptionBugClosedStr = null;
        String allowDraftStr = null;
        try{
        	allowCompareVersionForPreCommitStr = preferenceStore.getString(P_COMPAREVESION_PRECOMMIT );
        	allowOptionalPageCommitStr = preferenceStore.getString(P_OPTIONAL_PAGE_COMMIT );
        	allowOptionBugClosedStr = preferenceStore.getString(P_OPTIONAL_BUGCLOSED );
            allowDraftStr = preferenceStore.getString(P_OPTIONAL_DRAFT );
        } catch (Exception e) {
        }
        boolean allowCompareVersionForPreCommit = false;
        boolean allowOptionalPageCommit = false;
        boolean allowOptionBugClosed = false;
        boolean allowDraft = false;
        if ( allowCompareVersionForPreCommitStr != null && !allowCompareVersionForPreCommitStr.trim().isEmpty()) {
        	allowCompareVersionForPreCommit = Boolean.valueOf(allowCompareVersionForPreCommitStr).booleanValue();
        }
        if ( allowOptionalPageCommitStr != null && !allowOptionalPageCommitStr.trim().isEmpty()) {
        	allowOptionalPageCommit = Boolean.valueOf(allowOptionalPageCommitStr).booleanValue();
        }
        if ( allowOptionBugClosedStr != null && !allowOptionBugClosedStr.trim().isEmpty()) {
        	allowOptionBugClosed = Boolean.valueOf(allowOptionBugClosedStr).booleanValue();
        }
        if ( allowDraftStr != null && !allowDraftStr.trim().isEmpty()) {
            allowDraft = Boolean.valueOf(allowDraftStr).booleanValue();
        }
        
        btnCompareVesionForPreCommit.setSelection( allowCompareVersionForPreCommit );
        btnOptionalPageCommit.setSelection( allowOptionalPageCommit );
        btnOptionalBugClosed.setSelection( allowOptionBugClosed );
        btnOptionalDraft.setSelection( allowDraft );
        
    }
    
    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(ReviewboardCorePlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void performDefaults(){
        preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        btnCompareVesionForPreCommit.setSelection(false);
        btnOptionalPageCommit.setSelection(false);
        btnOptionalBugClosed.setSelection(false);
        btnOptionalDraft.setSelection(false);
    }
    
    @Override
    protected void performApply(){
        performOk();
    }
    

    @Override
    public boolean performOk(){
        preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        preferenceStore.setValue(P_COMPAREVESION_PRECOMMIT, Boolean.toString(btnCompareVesionForPreCommit.getSelection()));
        preferenceStore.setValue(P_OPTIONAL_PAGE_COMMIT, Boolean.toString(btnOptionalPageCommit.getSelection()));
        preferenceStore.setValue(P_OPTIONAL_BUGCLOSED, Boolean.toString(btnOptionalBugClosed.getSelection()));
        preferenceStore.setValue(P_OPTIONAL_DRAFT, Boolean.toString(btnOptionalDraft.getSelection()));
        return true;
        
    }

}
