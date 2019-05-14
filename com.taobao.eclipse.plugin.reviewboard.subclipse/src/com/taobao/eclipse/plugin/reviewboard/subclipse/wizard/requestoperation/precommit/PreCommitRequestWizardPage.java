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
package com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.precommit;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.compare.SVNLocalCompareInput;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.dialog.IToolbarControlCreator;
import com.taobao.eclipse.plugin.reviewboard.subclipse.dialog.ResourceSelectionTreePreCommit;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;
import com.taobao.eclipse.plugin.reviewboard.subclipse.wizard.requestoperation.AbstarctRequestWizardPage;

/**
 * 类说明:CommitRequestWizardPage，提交 Request 
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings({"unchecked"})
public class PreCommitRequestWizardPage extends AbstarctRequestWizardPage {

    protected ResourceSelectionTreePreCommit resourceSelectionTree;
    
    protected Text txtStart;
    
    /**
     * Create the wizard.
     */
    public PreCommitRequestWizardPage(IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            String pageName, String title, ImageDescriptor image, IResource[] resourcesSelectedByUser, 
            IStructuredSelection selection, HashMap statusMap, IReviewboardClient reviewboardClient, Long[] startAndStopVersion) {
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
        
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        if( rbConfig.isAllowCompareVersionForPreCommit() ){
            Group groupRequsetInfo = new Group(composite, SWT.FULL_SELECTION);
            GridLayout layoutGroupPropertiesDefault = new GridLayout();
            layoutGroupPropertiesDefault.numColumns = 3;
            groupRequsetInfo.setLayout(layoutGroupPropertiesDefault);
            groupRequsetInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            groupRequsetInfo.setText(RbSubclipseMessages.getString("PAGE_START_0"));
            
            Label lblStart = new Label(groupRequsetInfo, SWT.NONE);
            lblStart.setText(RbSubclipseMessages.getString("PAGE_START_1"));
            
            txtStart = new Text(groupRequsetInfo, SWT.BORDER);
            GridData gridData_7 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gridData_7.widthHint = 88;
            txtStart.setLayoutData(gridData_7);
            txtStart.setTextLimit(9);
            txtStart.addVerifyListener(new VerifyListener() {
                public void verifyText(VerifyEvent e) {
                    //检查输入的字符是否为数字0123456789，不在则返回-1
                    boolean b=("0123456789".indexOf(e.text)>=0);
                    e.doit=b;//其含义是如果doit==true，则允许输入，否则不允许
                }
            });

            Label lblStartDescription = new Label(groupRequsetInfo, SWT.NONE);
            lblStartDescription.setText(RbSubclipseMessages.getString("PAGE_START_2"));
        }
        
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void createSourceSelectionTreeOrTable(Composite composite) {
        IToolbarControlCreator toolbarControlCreator = getToolbarControlCreator();
        if( null != resources && resources.length != 0 ){
            
            resourceSelectionTree = new ResourceSelectionTreePreCommit(composite, SWT.NONE, 
                    RbSubclipseMessages.getString("PreReviewBoardAction.dialogTitle_1"),
                    resources, statusMap, null, true, toolbarControlCreator, null); //$NON-NLS-1$
            ((CheckboxTreeViewer)resourceSelectionTree.getTreeViewer()).setAllChecked(true);
            resourceSelectionTree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    //validatePage();
                }           
            });
            
            resourceSelectionTree.getTreeViewer().addDoubleClickListener(new IDoubleClickListener(){
                public void doubleClick(DoubleClickEvent event) {
                    IStructuredSelection sel = (IStructuredSelection)event.getSelection();
                    Object sel0 = sel.getFirstElement();
                    if (sel0 instanceof IFile) {
                        IFile ifile = (IFile)sel0;
                        final ISVNLocalResource localResource= SVNWorkspaceRoot.getSVNResourceFor( ifile );
                        SVNRevision compareVersion = SVNRevision.BASE;
                        //compareVersion = SVNRevision.START;
                        ISVNRemoteResource svnRemoteResource = null;
                        int compareVersionInt = 0;
                        if( null != txtStart ){
                            compareVersionInt = getStartVersion();
                        }
                        if( compareVersionInt > 0 ){
                            compareVersion = new SVNRevision.Number(compareVersionInt);
                        }
                        //修复版本号
                        if( compareVersion instanceof SVNRevision.Number ){
                            compareVersion = RbSVNUrlUtils.reviseSVNRevisionAdaptByMaxOrMin( (IResource)ifile, 
                                    (SVNRevision.Number)compareVersion, false, null );
                        }
                        SVNLocalCompareInput compareEditorInput = null;
                        
                        try {
                            svnRemoteResource = localResource.getRemoteResource( compareVersion );
                            if( null == svnRemoteResource ){
                                compareEditorInput = new SVNLocalCompareInput(localResource, SVNRevision.BASE, true);
                            }else{
                                compareEditorInput = new SVNLocalCompareInput(localResource, svnRemoteResource );
                            }
                        } catch (SVNException e) {
                            MessageDialog.openError(getShell(), 
                                    RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                                    RbSubclipseMessages.getString("ERROR_COMPARE") + e.getMessage()); //$NON-NLS-1$
                            return ;
                        }
                        
                        if( null != compareEditorInput ){
                            try {
                                compareEditorInput.run( new NullProgressMonitor() );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            compareViewerPane.setInput(compareEditorInput.getCompareResult());
                            compareViewerPane.setTitleArgument( RbSubclipseMessages.getString("COMPARE.dialogTitle_1")
                                    + (null != svnRemoteResource ? svnRemoteResource.getRevision() : "") );
                            showComparePane(true);
                            showCompareButton.setSelection(true);
                        }
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
        if( null != resourceSelectionTree ){
            selectedResources = resourceSelectionTree.getSelectedResources();
        }
        return selectedResources;
    }

    @Override
    protected void validateTextFieldSpecial(List<String> errorInfos) {
        ;
    }
    
    /**
     * 验证所有其它属性
     */
    @Override
    public void validateOtherField() {
        ;
    }

    /**
     * 返回是否是preCommit。如果是preCommit，则返回true，否则返回false
     * @return
     */
    @Override
    public boolean isPreCommit(){
        return true;
    }
    
    
    /**
     * 返回start版本号
     */
    public int getStartVersion(){
        int start = 0;
        if( null == txtStart ){
            return start;
        }
        if(txtStart.getText().trim().isEmpty()){
            ;
        }else{
            try {
                start = Integer.parseInt(txtStart.getText().trim());
            } catch (NumberFormatException e1) {
            }
        }
        return start;
    }
    
}
