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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_AUTO;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_GBK;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_UTF_8;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;
import com.taobao.eclipse.plugin.reviewboard.core.ReviewboardCorePlugin;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IReviewboardClientRepository;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.ReviewboardClientRepository;

/**
 * 类说明:ReviewBoard首选项 功能高级设置页面
 * 
 * @author 智清 
 * 创建时间：2011-7-13
 */
public class AdvanceFuctionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    
    public static final String P_BRANCHS_STARTVERSION_GET = "P_BRANCHS_STARTVERSION_GET"; //$NON-NLS-1$
    
    public static final String P_ALL_STARTVERSION_GET = "P_ALL_STARTVERSION_GET"; //$NON-NLS-1$
    
    public static final String P_CHARSET = "P_CHARSET"; //$NON-NLS-1$
    
    private IPreferenceStore preferenceStore;
    
    private Combo charsetCombo;
    
    private Button cacheResetButton;
    
    protected Combo comboRepository;
    protected AutoCompleteField comboRepositoryCompleteField;

    private Button btnBranchesStartVersionGet = null;
    private Button btnAllStartVersionGet = null;
    
    /**
     * Create the preference page.
     */
    public AdvanceFuctionPreferencePage() {
        setPreferenceStore(ReviewboardCorePlugin.getDefault().getPreferenceStore());
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    public Control createContents(Composite parent) {
        
        Label label = null;
        
        preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Group group = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        group.setLayoutData(gd);
        group.setText(RbCoreMessages.getString("CACHE_RESET_LABLE"));
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("CACHE_RESET_BTN_DES"));
        
        cacheResetButton = new Button(group, SWT.NONE);
        cacheResetButton.setText(RbCoreMessages.getString("CACHE_RESET_BTN"));
        cacheResetButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	IReviewboardClientRepository rbClientRepository = ReviewboardClientRepository.getSingletonInstance();
                if( null != rbClientRepository ){
                    rbClientRepository.resetCache();
                }
                MessageDialog.openInformation( getShell(), 
                        RbCoreMessages.getString("TITLE_INFORMATION"), 
                        RbCoreMessages.getString("CACHE_RESET_ALREADY"));
            }
        });
        
        group = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 1;
        group.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        group.setLayoutData(gd);
        group.setText(RbCoreMessages.getString("START_VERSION_AUTO_LABLE"));
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("START_VERSION_AUTO_DES"));

        btnBranchesStartVersionGet = new Button(group, SWT.CHECK);
        btnBranchesStartVersionGet.setText(RbCoreMessages.getString("START_VERSION_AUTO_SVNBRANCHES")); //$NON-NLS-1$
        
        btnAllStartVersionGet = new Button(group, SWT.CHECK);
        btnAllStartVersionGet.setText(RbCoreMessages.getString("START_VERSION_AUTO_SVNALL")); //$NON-NLS-1$
        
        group = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        group.setLayoutData(gd);
        group.setText(RbCoreMessages.getString("SETTING_ADV_OTHERS_LABLE"));
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("SETTING_ADV_DIFF_LABLE"));

        ComboViewer charsetComboViewer = new ComboViewer(group, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY );
        //先用硬编码的方式实现
        charsetComboViewer.add(CHARSET_AUTO);
        charsetComboViewer.add(CHARSET_GBK);
        charsetComboViewer.add(CHARSET_UTF_8);
        charsetCombo =  charsetComboViewer.getCombo();
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        charsetCombo.setLayoutData(gridData); 
        charsetCombo.select(0);
        
        initialize();
        
        return composite;
    }
    
    
    /**
     * 参数初始化
     */
    private void initialize() {
        String allowBranchesStartVersionGetStr = null;
        String allowAllStartVersionGetStr = null;
        boolean allowBranchesStartVersionGet = false;
        boolean allowAllStartVersionGet = false;
        try {
            allowAllStartVersionGetStr = preferenceStore.getString(P_ALL_STARTVERSION_GET);
            allowBranchesStartVersionGetStr = preferenceStore.getString(P_BRANCHS_STARTVERSION_GET);
            if (allowBranchesStartVersionGetStr != null && !allowBranchesStartVersionGetStr.trim().isEmpty()) {
                allowBranchesStartVersionGet = Boolean.valueOf(allowBranchesStartVersionGetStr).booleanValue();
            }
            if (allowAllStartVersionGetStr != null && !allowAllStartVersionGetStr.trim().isEmpty()) {
                allowAllStartVersionGet = Boolean.valueOf(allowAllStartVersionGetStr).booleanValue();
            }

        } catch (Exception e) {
        }
        btnBranchesStartVersionGet.setSelection( allowBranchesStartVersionGet );
        btnAllStartVersionGet.setSelection( allowAllStartVersionGet );
        
        String charset = preferenceStore.getString( P_CHARSET );
        if( null == charset || charset.equals(CHARSET_AUTO) ){
        	charsetCombo.select(0);
        }else if( charset.equals(CHARSET_GBK) ){
        	charsetCombo.select(1);
        }else if( charset.equals(CHARSET_UTF_8) ){
        	charsetCombo.select(2);
        }else{
        	charsetCombo.select(0);
        }
        
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
        btnBranchesStartVersionGet.setSelection(false);
        btnAllStartVersionGet.setSelection(false);
        charsetCombo.select(0);
    }
    
    @Override
    protected void performApply(){
        performOk();
    }
    

    @Override
    public boolean performOk(){
        preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        preferenceStore.setValue(P_BRANCHS_STARTVERSION_GET, Boolean.toString(btnBranchesStartVersionGet.getSelection()));
        preferenceStore.setValue(P_ALL_STARTVERSION_GET, Boolean.toString(btnAllStartVersionGet.getSelection()));
        preferenceStore.setValue(P_CHARSET, charsetCombo.getText());
        return true;
        
    }

}
