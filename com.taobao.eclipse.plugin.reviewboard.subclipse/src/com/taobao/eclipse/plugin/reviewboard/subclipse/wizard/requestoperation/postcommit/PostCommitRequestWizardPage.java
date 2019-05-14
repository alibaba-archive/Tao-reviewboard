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
package com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.postcommit;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.TAG_BRANCHES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.compare.ResourceEditionNode;
import org.tigris.subversion.subclipse.ui.compare.SVNCompareEditorInput;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.dialog.IToolbarControlCreator;
import com.taobao.eclipse.plugin.reviewboard.subclipse.dialog.ResourceSelectionTreePostCommit;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.AbstarctRequestWizardPage;

/**
 * 类说明:CommitRequestWizardPage，提交 Request 
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings({"unchecked"})
public class PostCommitRequestWizardPage extends AbstarctRequestWizardPage {

    protected ResourceSelectionTreePostCommit resourceSelectionTreePostCommit ;

    protected Text txtStart;
    protected Text txtStop;
    
    /**
     * Create the wizard.
     */
    public PostCommitRequestWizardPage(IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            String pageName, String title, ImageDescriptor image, 
            IResource[] resourcesSelectedByUser, IStructuredSelection selection, 
            HashMap statusMap, IReviewboardClient reviewboardClient, Long[] startAndStopVersion) {
        super(targetPart, targetPage, pageName, title, image, 
                resourcesSelectedByUser, selection, statusMap, reviewboardClient, startAndStopVersion); 
    }
    
    @Override
    public void beforeCreateDetail(Composite composite){
        ;
    }
    
    @Override
    protected void createPreOrPostInfo(Composite composite){

        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Group groupRequsetInfo = new Group(composite, SWT.NONE);
        GridLayout layoutGroupPropertiesDefault = new GridLayout();
        layoutGroupPropertiesDefault.numColumns = 7;
        groupRequsetInfo.setLayout(layoutGroupPropertiesDefault);
        groupRequsetInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupRequsetInfo.setText(RbSubclipseMessages.getString("PAGE_VERSION_0"));
        
        Label lblStart = new Label(groupRequsetInfo, SWT.NONE);
        lblStart.setText("start");
        
        txtStart = new Text(groupRequsetInfo, SWT.BORDER);
        GridData gridData_7 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_7.widthHint = 88;
        txtStart.setLayoutData(gridData_7);
        txtStart.setTextLimit(9);
        txtStart.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
            }
        });
        
        Label lblStop = new Label(groupRequsetInfo, SWT.NONE);
        lblStop.setText("stop");
        
        txtStop = new Text(groupRequsetInfo, SWT.BORDER);
        GridData gridData_8 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_8.widthHint = 88;
        txtStop.setLayoutData(gridData_8);
        txtStop.setTextLimit(9);
        txtStop.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
            }
        });
        initStartAndStop();
    }
    
    /**
     * 设置start和stop的初始值
     */
    private void initStartAndStop(){
        Set< IProject > projectSet = new HashSet< IProject >();
        if ( null != resourcesSelectedByUser && resourcesSelectedByUser.length > 0) {
            for( IResource resourcesTmp : resourcesSelectedByUser ){
                projectSet.add( resourcesTmp.getProject() );
            }
        }
        if( !projectSet.isEmpty() ){
            try {
                SVNRevision.Number svnRevisionHead = null;
                for( IProject project : projectSet ){
                    SVNRevision.Number svnRevisionHeadTmp = RbSVNUrlUtils.getSVNRevisionBaseOrHead(project, true);
                    if( null == svnRevisionHead ){
                        svnRevisionHead = svnRevisionHeadTmp;
                    }else{
                        if( svnRevisionHead.getNumber() < svnRevisionHeadTmp.getNumber() ){
                            svnRevisionHead = svnRevisionHeadTmp;
                        }
                    }
                }
                if( null != svnRevisionHead ){
                    txtStop.setText( String.valueOf( svnRevisionHead.getNumber() ) );
                }
                RbConfig rbConfig = RbConfigReader.getRbConfig( null );
                //标记是否需要自动最小值
                boolean allowComputeStart = false;
                if( rbConfig.isAllowAllStartVersionGet() ){
                    allowComputeStart = true;
                }else if( rbConfig.isAllowBranchesStartVersionGet() ){
                    boolean isAllTag = RbSVNUrlUtils.isSvnUrlAllContainStr(projectSet, TAG_BRANCHES);
                    if( isAllTag ){
                        allowComputeStart = true;
                    }
                }
                if( !allowComputeStart ){
                    return ;
                }
                SVNRevision.Number svnRevisionMin = null;
                for( IProject project : projectSet ){
                    SVNRevision.Number svnRevisionMinTmp = RbSVNUrlUtils.getSVNRevisionMin( project );
                    if( null == svnRevisionMin ){
                        svnRevisionMin = svnRevisionMinTmp;
                    }else{
                        if( svnRevisionMin.getNumber() > svnRevisionMinTmp.getNumber() ){
                            svnRevisionMin = svnRevisionMinTmp;
                        }
                    }
                }
                if( null != svnRevisionMin ){
                    txtStart.setText( String.valueOf( svnRevisionMin.getNumber() ) );
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void createSourceSelectionTreeOrTable(Composite composite) {
        IToolbarControlCreator toolbarControlCreator = getToolbarControlCreator();
        
        if( null != resourcesSelectedByUser && resourcesSelectedByUser.length > 0 ){
            
            resourceSelectionTreePostCommit = new ResourceSelectionTreePostCommit(composite, SWT.NONE, 
                    RbSubclipseMessages.getString("PostReviewBoardAction.dialogTitle_1"), 
                    resourcesSelectedByUser, toolbarControlCreator);
            resourceSelectionTreePostCommit.getTableViewer().setAllChecked(true);
            resourceSelectionTreePostCommit.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
                
                public void doubleClick(final DoubleClickEvent event) {
                    List<String> errorInfos = new ArrayList<String>();
                    validateTextFieldSpecial( errorInfos );
                    if( null != errorInfos && !errorInfos.isEmpty() ){
                        openErrorInformationBox(errorInfos);
                        return ;
                    }
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    Object sel0 = sel.getFirstElement();
                    final IResource resource= (IResource)sel0;
                    CompareEditorInput compareEditorInput = null;
                    ISVNRemoteResource svnRemoteResource1 = null;
                    ISVNRemoteResource svnRemoteResource2 = null;
                    final ISVNLocalResource localResource= SVNWorkspaceRoot.getSVNResourceFor(resource);
                    String[] startAndStopVersion = getStartAndStopVersion();
                    SVNRevision[] fromAndToRevision = RbSVNUrlUtils.formateSVNRevisionUnify(startAndStopVersion[0], startAndStopVersion[1]);
                    
                    SVNRevision startVersion = fromAndToRevision[0];
                    SVNRevision stopVersion = fromAndToRevision[1];
                    //修复版本号
                    if( stopVersion instanceof SVNRevision.Number ){
                        stopVersion = RbSVNUrlUtils.reviseSVNRevision( resource, (SVNRevision.Number)fromAndToRevision[1], true, true, null );
                    }
                    if( startVersion instanceof SVNRevision.Number ){
                        startVersion = RbSVNUrlUtils.reviseSVNRevision( resource, (SVNRevision.Number)fromAndToRevision[0], false, true, 
                                stopVersion instanceof SVNRevision.Number ? (SVNRevision.Number)stopVersion : null );
                    }
                    //尽可能少地访问SVN库，以提高性能
                    if( startVersion instanceof SVNRevision.Number && stopVersion instanceof SVNRevision.Number ){
                        if( ((SVNRevision.Number)startVersion).getNumber() == ((SVNRevision.Number)stopVersion).getNumber() ){
                            MessageDialog.openError(getShell(), 
                                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                                    RbSubclipseMessages.getString("ERROR_COMPARE_1")); //$NON-NLS-1$
                            return ;
                        }
                    }
                    if( !(startVersion instanceof SVNRevision.Number) && !(stopVersion instanceof SVNRevision.Number) ){
                        if( startVersion.equals(stopVersion) ){
                            MessageDialog.openError(getShell(), 
                                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                                    RbSubclipseMessages.getString("ERROR_COMPARE_1")); //$NON-NLS-1$
                            return ;
                        }
                    }
                    
                    try {
                        svnRemoteResource1 = localResource.getRemoteResource( startVersion );
                        svnRemoteResource2 = localResource.getRemoteResource( stopVersion );
                    } catch (Exception e) {
                        MessageDialog.openError(getShell(), RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                                RbSubclipseMessages.getString("ERROR_COMPARE")+e.getMessage()); //$NON-NLS-1$
                        return ;
                    }
                    
                    if( svnRemoteResource1.isFolder() ){
                        MessageDialog.openInformation(getShell(), RbSubclipseMessages.getString("PROMPTING_INFORMATION_LABLE"),
                                RbSubclipseMessages.getString("ERROR_COMPARE_1")); //$NON-NLS-1$
                        return ;
                    }else{
                        ResourceEditionNode left = new ResourceEditionNode( svnRemoteResource1 );
                        ResourceEditionNode right = new ResourceEditionNode( svnRemoteResource2 );
                        compareEditorInput = new SVNCompareEditorInput(left, right);
                    }
                    if( null != compareEditorInput ){
                        try {
                            compareEditorInput.run( new NullProgressMonitor() );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        compareViewerPane.setTitleArgument( "Version Compare:" + svnRemoteResource1.getLastChangedRevision() + ":" + svnRemoteResource2.getLastChangedRevision());
                        compareViewerPane.setInput(compareEditorInput.getCompareResult());
                        showComparePane(true);
                        showCompareButton.setSelection(true);
                    }
                }
            
            });
            
        }
    }
    
    protected void createCompareView( Composite composite ){
        super.createCompareView(composite);
        verticalSash.setWeights(new int[] {2000, 680});
        horizontalSash.setWeights(new int[] {20, 65});
    }
    
    @Override
    protected IResource[] getSelectedResources() {
        IResource[] selectedResources = null;
        if( null != resourceSelectionTreePostCommit ){
            selectedResources = resourceSelectionTreePostCommit.getResourceForPostCommit();
        }
        return selectedResources;
    }
    
    /**
     * 返回是否是preCommit。如果是preCommit，则返回true，否则返回false
     * @return
     */
    @Override
    public boolean isPreCommit(){
        return false;
    }

    @Override
    protected void validateOtherField() {
        if( null == resources || resources.length == 0 ){
            if( null != startAndStopVersion ){
                this.setStartAndStopVersion(startAndStopVersion[0], startAndStopVersion[1]);
            }
        }
    }

    @Override
    protected void validateTextFieldSpecial(List<String> errorInfos) {
        this.validateStartAndStop(errorInfos);
    }
    
    private void validateStartAndStop(List<String> errorInfos) {
        if( null == errorInfos ){
            errorInfos = new ArrayList<String>();
        }
        if(!isPreCommit()){
            if( txtStart.getText().trim().equals("") && txtStop.getText().trim().equals("") ){
                errorInfos.add(RbSubclipseMessages.getString("VALID_START_STOP_0"));
            }else{
                int start = 0;
                int stop = 0;
                if(txtStart.getText().trim().isEmpty()){
                    ;
                }else{
                    try {
                        start = Integer.parseInt(txtStart.getText().trim());
                    } catch (NumberFormatException e1) {
                        errorInfos.add(RbSubclipseMessages.getString("VALID_START_STOP_1"));
                    }
                }
                if(txtStop.getText().trim().isEmpty()){
                    ;
                }else{
                    try {
                        stop = Integer.parseInt(txtStop.getText().trim());
                    } catch (NumberFormatException e1) {
                        errorInfos.add(RbSubclipseMessages.getString("VALID_START_STOP_2"));
                    }
                }
                if( stop > 0 && stop <= start  ){
                    errorInfos.add(RbSubclipseMessages.getString("VALID_START_STOP_3"));
                }
            }
        }
    }

    /**
     * 设置start和stop版本号
     */
    public void setStartAndStopVersion(long start, long stop){
        if( start > 0 && stop > 0){
            txtStart.setText(start + EMPTY_STRING);
            txtStop.setText(stop + EMPTY_STRING);
        }else if( start > 0 && stop <= 0){
            txtStart.setText(start + EMPTY_STRING);
            txtStop.setText(EMPTY_STRING);
        }else if( start <= 0 && stop > 0){
            txtStart.setText( (stop - 1 ) + EMPTY_STRING );
            txtStop.setText(stop + EMPTY_STRING);
        }else{
            txtStart.setText(EMPTY_STRING );
            txtStop.setText(EMPTY_STRING);
        }
    }
    
    /**
     * 返回start和stop版本号
     */
    public String[] getStartAndStopVersion(){
        int start = 0;
        int stop = 0;
        if(txtStart.getText().trim().isEmpty()){
            ;
        }else{
            try {
                start = Integer.parseInt(txtStart.getText().trim());
            } catch (NumberFormatException e1) {
            }
        }
        if(txtStop.getText().trim().isEmpty()){
            ;
        }else{
            try {
                stop = Integer.parseInt(txtStop.getText().trim());
            } catch (NumberFormatException e1) {
            }
        }
        if( start > 0 && stop > 0){
            return new String[]{ start + EMPTY_STRING, stop + EMPTY_STRING };
        }else if( start > 0 && stop <= 0){
            return new String[]{ start + EMPTY_STRING, EMPTY_STRING };
        }else if( start <= 0 && stop > 0){
            txtStart.setText( (stop - 1 ) + EMPTY_STRING );
            return new String[]{ (stop - 1 ) + EMPTY_STRING , stop + EMPTY_STRING };
        }else{
            //这种情况不可能出现
            return new String[]{ EMPTY_STRING, EMPTY_STRING };
        }
    }
    

}
