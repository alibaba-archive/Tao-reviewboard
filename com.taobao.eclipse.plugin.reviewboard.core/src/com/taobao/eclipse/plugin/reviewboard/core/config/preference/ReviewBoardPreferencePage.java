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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARACTER_ENCODING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.SERVER_DEFAULT;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;
import com.taobao.eclipse.plugin.reviewboard.core.ReviewboardCorePlugin;
import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IReviewboardClientRepository;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.ReviewboardClientRepository;
import com.taobao.eclipse.plugin.reviewboard.core.service.IReviewboardService;
import com.taobao.eclipse.plugin.reviewboard.core.service.impl.ReviewboardServiceImpl;
import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;

/**
 * 类说明:ReviewBoard首选项 基本设置页面
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ReviewBoardPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    
    public static final String P_SERVER = "P_SERVER"; //$NON-NLS-1$

    public static final String P_USERID = "P_USERID"; //$NON-NLS-1$

    public static final String P_PASSWORD = "P_PASSWORD"; //$NON-NLS-1$

    public static final String P_BRANCH = "P_BRANCH"; //$NON-NLS-1$
    
    public static final String P_GROUPS = "P_GROUPS"; //$NON-NLS-1$
    
    public static final String P_REPOSITORY = "P_REPOSITORY"; //$NON-NLS-1$
    
    public static final String P_PEOPLES = "P_PEOPLES"; //$NON-NLS-1$
    
    private IPreferenceStore preferenceStore;
    
    private Composite parent;
    
    private Text txtServer;
    private Text txtUserId;
    private Text txtPassword;
    
    private Text txtGroups;
    protected AutoCompleteField txtGroupCompleteField;
    private Text txtPeoples;
    protected AutoCompleteField txtPeopleCompleteField;
    private Text txtBranch;
    
    /**
     * Create the preference page.
     */
    public ReviewBoardPreferencePage() {
        setPreferenceStore(ReviewboardCorePlugin.getDefault().getPreferenceStore());
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    public Control createContents(Composite parent) {
        
        this.parent = parent;
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
        layout.numColumns = 3;
        group.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        group.setLayoutData(gd);
        group.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_LABLE"));

        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_0"));
        
        txtServer = new Text(group, SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtServer.setLayoutData(gridData);
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_0_DES"));

        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_1"));
        
        txtUserId = new Text(group, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtUserId.setLayoutData(gridData);
        txtUserId.setText(preferenceStore.getString(P_USERID));
        txtUserId.setEnabled(true);
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_1_DES"));
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_2"));
        
        txtPassword = new Text(group, SWT.BORDER | SWT.PASSWORD);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtPassword.setLayoutData(gridData);
        txtPassword.setEnabled(true);
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_SERVER_2_DES"));
        
        group = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        group.setLayoutData(gd);
        group.setText(RbCoreMessages.getString("PREFERPAGE_ATTRIBUTE_LABLE"));
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_ATTRIBUTE_0"));
        
        txtGroups = new Text(group, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtGroups.setLayoutData(gridData);
        
        label = new Label(group, SWT.NONE);
        label.setText(RbCoreMessages.getString("PREFERPAGE_ATTRIBUTE_1"));
        
        txtPeoples = new Text(group, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtPeoples.setLayoutData(gridData);

        initialize();
        
        return composite;
    }
    
    
    /**
     * 参数初始化
     */
    private void initialize() {
    	if( null != preferenceStore.getString(P_SERVER) 
    			&& !preferenceStore.getString(P_SERVER).trim().isEmpty()){
            txtServer.setText(preferenceStore.getString(P_SERVER));
    	}else{
    		txtServer.setText(SERVER_DEFAULT);
    	}
        txtPassword.setText(preferenceStore.getString(P_PASSWORD));
        if( null != txtBranch ){
            txtBranch.setText(preferenceStore.getString(P_BRANCH));
        }
        txtGroups.setText(preferenceStore.getString(P_GROUPS));
        txtPeoples.setText(preferenceStore.getString(P_PEOPLES));
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
        txtServer.setText(SERVER_DEFAULT);
        txtUserId.setText(EMPTY_STRING);
        txtPassword.setText(EMPTY_STRING);
        if( null != txtBranch ){
            txtBranch.setText(EMPTY_STRING);
        }
        txtGroups.setText(EMPTY_STRING);
        txtPeoples.setText(EMPTY_STRING);
    }
    
    @Override
    protected void performApply(){
        performOk();
    }
    

    @Override
    public boolean performOk(){
        if( !txtUserId.getText().trim().isEmpty() || !txtPassword.getText().trim().isEmpty() ){
            if( txtServer.getText().trim().isEmpty() 
                    || txtUserId.getText().trim().isEmpty() 
                    || txtPassword.getText().trim().isEmpty()){
                MessageDialog.openError( this.parent.getShell(), 
                        RbCoreMessages.getString("ERROR_LABLE"), 
                        RbCoreMessages.getString("ERROR_USERNAMEPASSWORD_0"));
                return false;
            }
        }

        IReviewboardClientRepository rbClientRepository = ReviewboardClientRepository.getSingletonInstance();
        
        if( !txtServer.getText().trim().isEmpty() ){
            String serverUrl =  ReviewboardUtil.norminateRepositoryUrl( txtServer.getText().trim() ) ;
            if( !txtUserId.getText().trim().isEmpty() && !txtPassword.getText().trim().isEmpty() ){
                if( null == rbClientRepository ){
                    return false;
                }
                IReviewboardClient reviewboardClient = rbClientRepository.getClientFromUrlToClientMap();
                if( null == reviewboardClient || null == reviewboardClient.getServerUrl()
                        || !reviewboardClient.getServerUrl().trim().equalsIgnoreCase(serverUrl.trim()) ){
                    reviewboardClient = rbClientRepository.getClient( false, serverUrl, 
                            txtUserId.getText().trim(), txtPassword.getText().trim(), CHARACTER_ENCODING );
                }
                if( null == reviewboardClient ){
                    reviewboardClient = rbClientRepository.getClient( false, serverUrl, 
                            txtUserId.getText().trim(), txtPassword.getText().trim(), CHARACTER_ENCODING );
                }
                IReviewboardService reviewboardService = new ReviewboardServiceImpl( reviewboardClient );
                //验证用户名和密码是否正确
                boolean isSuccess = reviewboardService.validCredentials( txtUserId.getText().trim(), txtPassword.getText().trim(), null );
                if( !isSuccess ){
                    MessageDialog.openError( this.parent.getShell(), 
                            RbCoreMessages.getString("ERROR_LABLE"), 
                            RbCoreMessages.getString("ERROR_USERNAMEPASSWORD_1"));
                    return false;
                }
            }
        }
        
        preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        preferenceStore.setValue(P_SERVER, ReviewboardUtil.norminateRepositoryUrl(txtServer.getText().trim()));
        preferenceStore.setValue(P_USERID, txtUserId.getText().trim());
        preferenceStore.setValue(P_PASSWORD, txtPassword.getText().trim());
        if( null != txtBranch ){
            preferenceStore.setValue(P_BRANCH, txtBranch.getText().trim());
        }else{
            preferenceStore.setValue(P_BRANCH, EMPTY_STRING);
        }
        preferenceStore.setValue(P_GROUPS, txtGroups.getText().trim());
        preferenceStore.setValue(P_PEOPLES, txtPeoples.getText().trim());
        
        return true;
        
    }

}
