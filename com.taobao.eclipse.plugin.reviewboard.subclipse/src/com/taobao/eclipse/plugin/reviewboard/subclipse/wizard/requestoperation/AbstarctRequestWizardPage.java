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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHANGE_NUMBER_ENABLE_REPOSITORY;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.ISVNUIConstants;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.actions.SVNPluginAction;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.client.ReviewboardClientData;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelRepository;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewGroup;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequest;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelUser;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelUtil;
import com.taobao.eclipse.plugin.reviewboard.core.service.IReviewboardService;
import com.taobao.eclipse.plugin.reviewboard.core.service.impl.ReviewboardServiceImpl;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;
import com.taobao.eclipse.plugin.reviewboard.subclipse.dialog.IToolbarControlCreator;
import com.taobao.eclipse.plugin.reviewboard.subclipse.model.RecentDescriptionBeansManager;
import com.taobao.eclipse.plugin.reviewboard.subclipse.model.RecentDescriptionBeans.RecentDescriptionBean;
import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNUrlUtils;

/**
 * 类说明:提交页面的基类，提交 Request 
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstarctRequestWizardPage extends WizardPage {

    protected IWorkbenchPart targetPart;
    protected IWorkbenchPage targetPage;
    /**
     * 代码比较功能相关组件
     */
    protected SashForm verticalSash;
    protected SashForm horizontalSash;
    protected boolean showCompare;
    protected Button showCompareButton;
    protected CompareViewerSwitchingPane compareViewerPane;
    protected Composite cTop = null;
    protected Composite cBottom2 = null;
    
    /**
     * 其它组件
     */
    private Button btnUpdateOrCommit;
    protected Text textRequestId;
    //是否提交为草稿
    private Button btnIsDraft;
    
    protected Text txtSummary;
    protected Text txtGroup;
    protected AutoCompleteField txtGroupCompleteField;
    protected Text txtPeople;
    protected AutoCompleteField txtPeopleCompleteField;
    protected Text txtDescription;
    protected Combo comboDescriptionHistory;
    
    protected Text changeNumText;
    
    protected Text textBugsClosed;
    protected Text textBranch;

    /**
     * 相关变量
     */
    protected IResource[] resources;
    
    protected IProject project;
    protected HashMap statusMap;
    
    protected Long[] startAndStopVersion;
    protected IResource[] resourcesSelectedByUser;
    
    protected List<ModelRepository> modelRepositoryList = new ArrayList<ModelRepository>();
    protected List<ModelUser> modelUserList = new ArrayList<ModelUser>();
    protected List<ModelReviewGroup> modelReviewGroupList = new ArrayList<ModelReviewGroup>();
    
    protected List<String> repositoryNameList = new ArrayList<String>();
    protected List<String> userNameList = new ArrayList<String>();
    protected List<String> reviewGroupNameList = new ArrayList<String>();
    
    protected IReviewboardClient reviewboardClient;
    
    protected IReviewboardService reviewboardService;
    
    //提交Request时需要用到的数据
    protected List<ModelUser> peopleList = new ArrayList<ModelUser>();
    protected List<ModelReviewGroup> groupList = new ArrayList<ModelReviewGroup>();

    /**
     * 在创建页面元素前的准备工作
     */
    protected abstract void beforeCreateDetail(Composite composite);
    
    /**
     * 生成pre-commit或post-commit的页面信息，如start,stop等
     */
    protected abstract void createPreOrPostInfo(Composite composite);
    
    /**
     * 生成文件或目录列表
     */
    protected abstract void createSourceSelectionTreeOrTable(Composite composite);
    
    /**
     * 返回最后确定的选定的文件
     */
    protected abstract IResource[] getSelectedResources();
    
    /**
     * 提交前的认证。对基类认证的补充
     */
    protected abstract void validateTextFieldSpecial(List<String> errorInfos);
    
    /**
     * 页面特有组件的验证
     */
    protected abstract void validateOtherField();
    
    /**
     * 返回是否是preCommit。如果是preCommit，则返回true，否则返回false
     * @return
     */
    public abstract boolean isPreCommit();
    
    /**
     * Create the wizard.
     */
    public AbstarctRequestWizardPage(IWorkbenchPart targetPart, IWorkbenchPage targetPage, 
            String pageName, String title, ImageDescriptor image, 
            IResource[] resourcesSelectedByUser, IStructuredSelection selection, 
            HashMap statusMap, IReviewboardClient reviewboardClient, Long[] startAndStopVersion) {
        super(pageName, title, image);
        this.targetPart = targetPart;
        this.targetPage = targetPage;
        if( null != resourcesSelectedByUser && resourcesSelectedByUser.length > 0 ){
            this.project = resourcesSelectedByUser[0].getProject();
        }
        if( null != selection ){
            Object[] selectedResources = selection.toArray();
            resources = new IResource[selectedResources.length];
            for (int i = 0; i < selectedResources.length; i++){
                resources[i] = (IResource)selectedResources[i];
            }
        }
        this.statusMap = statusMap;
        this.resourcesSelectedByUser = resourcesSelectedByUser;
        this.startAndStopVersion = startAndStopVersion;
        setPageComplete(false);

        this.reviewboardClient = reviewboardClient;
        ReviewboardClientData clientData = reviewboardClient.getClientData();

        this.reviewboardService = new ReviewboardServiceImpl( reviewboardClient );
        
        modelRepositoryList.clear();
        repositoryNameList.clear();
        modelRepositoryList.addAll(clientData.getRepositoryList());
        repositoryNameList = ModelUtil.getModelRepositoryNameList(modelRepositoryList);

        modelReviewGroupList.clear();
        reviewGroupNameList.clear();
        modelReviewGroupList.addAll(clientData.getReviewGroupList());
        
        reviewGroupNameList = ModelUtil.getModelReviewGroupNameList(modelReviewGroupList);
        
        modelUserList.clear();
        userNameList.clear();
        modelUserList.addAll(clientData.getUserList());
        userNameList = ModelUtil.getModelUserNameList(modelUserList);
    }

    public void createControl(Composite parent) {
        Composite composite= new Composite(parent, SWT.NULL);
        GridLayout layout= new GridLayout();
        composite.setLayout(layout);
        GridData gridData = new GridData();
        composite.setLayoutData(gridData);
        setControl(composite);

        beforeCreateDetail( composite );
        createControlComposite( composite );
        createControlDetail( null != cTop ? cTop : composite );
        createSourceSelectionTreeOrTable( null != cBottom2 ? cBottom2 : composite );
        createCompareView( composite );
        //给界面设置默认值，便于用户更方便地使用
        setDefaultValue();
        
        //验证
        validateChangeNumberTextField();
        btnUpdateOrCommitChanged();
        validateOtherField();
        setPageComplete( true );
    }
    
    /**
     * 新增 页面Composite
     * @param composite
     */
    public void createControlComposite( Composite composite ){
        horizontalSash = new SashForm(composite, SWT.HORIZONTAL);
        horizontalSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        verticalSash = new SashForm(horizontalSash, SWT.VERTICAL);
        //verticalSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        verticalSash.setLayout(gridLayout);
        verticalSash.setLayoutData(new GridData(GridData.FILL_BOTH)); 
                
        cTop = new Composite(verticalSash, SWT.NULL);
        GridLayout topLayout = new GridLayout();
        topLayout.marginHeight = 0;
        topLayout.marginWidth = 0;
        cTop.setLayout(topLayout);
        cTop.setLayoutData(new GridData(GridData.FILL_BOTH));
                
        Composite cBottom1 = new Composite(verticalSash, SWT.NULL);
        GridLayout bottom1Layout = new GridLayout();
        bottom1Layout.marginHeight = 0;
        bottom1Layout.marginWidth = 0;
        cBottom1.setLayout(bottom1Layout);
        cBottom1.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        cBottom2 = new Composite(cBottom1, SWT.NULL);
        GridLayout bottom2Layout = new GridLayout();
        bottom2Layout.marginHeight = 0;
        bottom2Layout.marginWidth = 0;
        cBottom2.setLayout(bottom2Layout);
        cBottom2.setLayoutData(new GridData(GridData.FILL_BOTH));
    }
    
    protected void createCompareView( Composite composite ){
        compareViewerPane = new CompareViewerSwitchingPane(horizontalSash, SWT.BORDER | SWT.FLAT) {
            protected Viewer getViewer(Viewer oldViewer, Object input) {
                CompareConfiguration cc = new CompareConfiguration();
                cc.setLeftEditable(false);
                cc.setRightEditable(false);
                return CompareUI.findContentViewer(oldViewer, input, this, cc); 
            }
        };
        compareViewerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        showCompare = false;

        if (!showCompare) {
            horizontalSash.setMaximizedControl(verticalSash);
        } else {
            showCompareButton.setSelection(true);
        }

    }
    
    /**
     * Allow the user to chose to save the patch to the workspace or outside
     * of the workspace.
     */
    public void createControlDetail(Composite composite) {
        
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        
        createPreOrPostInfo(composite);
        
        GridLayout layout= new GridLayout();
        
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        
        Group groupSummary = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        groupSummary.setLayout(layout);
        groupSummary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        groupSummary.setText(RbSubclipseMessages.getString("PAGE_SUMMARY"));

        Label lblSummary = new Label(groupSummary, SWT.NONE);
        lblSummary.setText(RbSubclipseMessages.getString("PAGE_SUMMARY"));
        txtSummary = new Text(groupSummary, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtSummary.setLayoutData(gridData);
        
        Group groupAttribute = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 4;
        groupAttribute.setLayout(layout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        groupAttribute.setLayoutData( gridData );
        groupAttribute.setText(RbSubclipseMessages.getString("PAGE_ATTRIBUTES"));
        
        Label lblRepository = new Label(groupAttribute, SWT.NONE);
        lblRepository.setText(RbSubclipseMessages.getString("PAGE_REPOSITORIES"));

        Label lblRepositoryValue = new Label(groupAttribute, SWT.NONE);
        ModelRepository repository = this.getSelectedRepository();
        if( null == repository ){
            lblRepositoryValue.setText("");
        }else{
            lblRepositoryValue.setText(repository.getName());
        }
        
        if( rbConfig.isAllowOptionBugClosed() ){
            Label lblTmp = new Label(groupAttribute, SWT.NONE);
            lblTmp.setText("");
            lblTmp = new Label(groupAttribute, SWT.NONE);
            lblTmp.setText("");
        }
        
        Label lblGroup = new Label(groupAttribute, SWT.NONE);
        lblGroup.setText(RbSubclipseMessages.getString("PAGE_GROUPS"));
        txtGroup = new Text(groupAttribute, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtGroup.setLayoutData(gridData);
        txtGroupCompleteField = new AutoCompleteField(txtGroup, new TextContentAdapter(), new String[] {});
        txtGroupCompleteField.setProposals( reviewGroupNameList.toArray(new String[reviewGroupNameList.size()]) );
        txtGroup.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                List<String> proposalTmp = getNewListForProposal( reviewGroupNameList, txtGroup.getText() );
                txtGroupCompleteField.setProposals( proposalTmp.toArray(new String[proposalTmp.size()]) );
            }
        });
        
        Label lblBranch = new Label(groupAttribute, SWT.NONE);
        lblBranch.setText(RbSubclipseMessages.getString("PAGE_BRANCHES"));
        textBranch = new Text(groupAttribute, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        textBranch.setLayoutData(gridData);
        textBranch.setEnabled(false);
        
        Label lblPeople = new Label(groupAttribute, SWT.NONE);
        lblPeople.setText(RbSubclipseMessages.getString("PAGE_REVIEWPEOPLES"));
        txtPeople = new Text(groupAttribute, SWT.BORDER);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        txtPeople.setLayoutData(gridData);
        txtPeopleCompleteField = new AutoCompleteField(txtPeople, new TextContentAdapter(), new String[] {});
        txtPeopleCompleteField.setProposals( userNameList.toArray(new String[userNameList.size()]) );
        txtPeople.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                List<String> proposalTmp = getNewListForProposal( userNameList, txtPeople.getText() );
                txtPeopleCompleteField.setProposals( proposalTmp.toArray(new String[proposalTmp.size()]) );
            }
        });
        if( rbConfig.isAllowOptionBugClosed() ){
            Label lblBugsClosed = new Label(groupAttribute, SWT.NONE);
            lblBugsClosed.setText(RbSubclipseMessages.getString("PAGE_BUGCLOSED"));
            
            textBugsClosed = new Text(groupAttribute, SWT.BORDER);
            gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            gridData.widthHint= 0;
            gridData.heightHint= SWT.DEFAULT;
            gridData.horizontalSpan= 1;
            textBugsClosed.setLayoutData(gridData);
            textBugsClosed.addVerifyListener(new VerifyListener() {
                public void verifyText(VerifyEvent e) {
                    //检查输入的字符是否为数字0123456789，不在则返回-1
                    boolean b=("0123456789,".indexOf(e.text)>=0);
                    e.doit=b;//其含义是如果doit==true，则允许输入，否则不允许
                }
            });
        }
        
        Group groupRequestDescrible = new Group(composite, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 1;
        groupRequestDescrible.setLayout(layout);
        GridData gdGroupRequestDescrible = new GridData(GridData.FILL_HORIZONTAL);
        gdGroupRequestDescrible.horizontalSpan = 1;
        groupRequestDescrible.setLayoutData(gdGroupRequestDescrible);
        groupRequestDescrible.setText(RbSubclipseMessages.getString("PAGE_DESCRIPTION"));
        
        txtDescription = new Text(groupRequestDescrible, SWT.BORDER | SWT.MULTI);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= 50;
        gridData.horizontalSpan= 1;
        txtDescription.setLayoutData(gridData);
        
        comboDescriptionHistory = new Combo(groupRequestDescrible, SWT.READ_ONLY);
        //comboDescriptionHistory.setVisibleItemCount( RecentDescriptionBeanListManager.MAX_SIZE + 1 );
        comboDescriptionHistory.setVisibleItemCount( 11 );
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint= 0;
        gridData.heightHint= SWT.DEFAULT;
        gridData.horizontalSpan= 1;
        comboDescriptionHistory.setLayoutData(gridData);
        List<RecentDescriptionBean> recentDescriptionBeanList = RecentDescriptionBeansManager.getSingletonInstance().getRecentDescriptionBeanList();
        int recentDescriptionBeanIndex = 0;
        for( RecentDescriptionBean recentDescriptionBean : recentDescriptionBeanList ){
            comboDescriptionHistory.add( recentDescriptionBean.getDescriptionShortContent() );
            //comboDescriptionHistory.add( recentDescriptionBean.getDecriptionContent() );
            comboDescriptionHistory.setData(String.valueOf(recentDescriptionBeanIndex),recentDescriptionBean);
            recentDescriptionBeanIndex++ ;
        }
        comboDescriptionHistory.select(0);
        comboDescriptionHistory.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if( comboDescriptionHistory.getSelectionIndex() == 0 ){
                    return ;
                }
                String key=String.valueOf(comboDescriptionHistory.getSelectionIndex());
                RecentDescriptionBean recentDescriptionBeanSelected= (RecentDescriptionBean)comboDescriptionHistory.getData(key);
                if( null != recentDescriptionBeanSelected && null != recentDescriptionBeanSelected.getDecriptionContent()){
                    txtDescription.setText( recentDescriptionBeanSelected.getDecriptionContent() );
                }
            }
        });
        
        Group groupRequsetInfo = new Group(composite, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns = 7;
        groupRequsetInfo.setLayout(layout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        groupRequsetInfo.setLayoutData(gridData);
        groupRequsetInfo.setText(RbSubclipseMessages.getString("PAGE_UPDATE_REQUEST"));
        btnUpdateOrCommit = new Button(groupRequsetInfo, SWT.CHECK);
        btnUpdateOrCommit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnUpdateOrCommitChanged();
            }
        });
        btnUpdateOrCommit.setToolTipText(RbSubclipseMessages.getString("PAGE_UPDATE_REQUEST"));
        btnUpdateOrCommit.setText(RbSubclipseMessages.getString("PAGE_UPDATE_REQUEST"));
        Label lblRequestId = new Label(groupRequsetInfo, SWT.NONE);
        lblRequestId.setText(RbSubclipseMessages.getString("PAGE_UPDATE_REQUEST_ID"));
        
        textRequestId = new Text(groupRequsetInfo, SWT.BORDER);
        textRequestId.setTextLimit(9);
        gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = 167;
        textRequestId.setLayoutData(gridData);
        textRequestId.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                //检查输入的字符是否为数字0123456789，不在则返回-1
                boolean b=("0123456789".indexOf(e.text)>=0);
                e.doit=b;//其含义是如果doit==true，则允许输入，否则不允许
            }
        });
        textRequestId.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                //validatePage();
            }
        });
        
        //将本次提交保存草稿
        btnIsDraft = new Button(groupRequsetInfo, SWT.CHECK);
        btnIsDraft.setText(RbSubclipseMessages.getString("PAGE_SAVE_AS_DRAFT"));
        btnIsDraft.setVisible( rbConfig.isAllowDraft() );
        
    }
    
    /**
     * 代码比较和SQL审核功能相关
     */
    public IToolbarControlCreator getToolbarControlCreator(){
        final SVNPluginAction[] toolbarActions = SVNUIPlugin.getCommitDialogToolBarActions();
        IToolbarControlCreator toolbarControlCreator = new IToolbarControlCreator() {
            public void createToolbarControls(ToolBarManager toolbarManager) {
                
                toolbarManager.add(new Separator());
                toolbarManager.add(new ControlContribution("showCompare") {
                    protected Control createControl(Composite parent) {
                        showCompareButton = new Button(parent, SWT.TOGGLE | SWT.FLAT);
                        showCompareButton.setImage(SVNUIPlugin.getImage(ISVNUIConstants.IMG_SYNCPANE)); //$NON-NLS-1$
                        showCompareButton.setToolTipText(RbSubclipseMessages.getString("PAGE_CODE_COMPARE"));
                        showCompareButton.setSelection(showCompare);
                        showCompareButton.addSelectionListener(
                              new SelectionListener(){
                                  public void widgetSelected(SelectionEvent e) {
                                      showComparePane(!showCompare);
                                  }
                                  public void widgetDefaultSelected(SelectionEvent e) {
                                  }
                              }
                              );
                      return showCompareButton;
                    }
                  });
                
                // add any contributing actions from the extension point
                if (toolbarActions.length > 0) {
                    toolbarManager.add(new Separator());
                    for (int i = 0; i < toolbarActions.length; i++) {
                        SVNPluginAction action = toolbarActions[i];
                        toolbarManager.add(action);
                    }
                }
            }
            public int getControlCount() {
              return 2;
            }
          };
          return toolbarControlCreator;
    }
    
    /**
     * 代码比较功能相关
     */
    public void showComparePane(boolean showCompare) {
        this.showCompare = showCompare;
        if (showCompare) {
            horizontalSash.setMaximizedControl(null);
        } else {
            horizontalSash.setMaximizedControl(verticalSash);
        }
    }
    
    

    /**
     * btnPreOrPost改变之后触发的动作
     */
    public void btnUpdateOrCommitChanged(){
        if( btnUpdateOrCommit.getSelection()){
            textRequestId.setEnabled(true);
            txtDescription.setEnabled(true);
            txtSummary.setEnabled(false);
            txtGroup.setEnabled(false);
            txtPeople.setEnabled(false);
            if( null != textBugsClosed ){
                textBugsClosed.setEnabled(false);
            }
            if( null != textBranch ){
                textBranch.setEnabled(false);
            }
            if( null != changeNumText ){
                changeNumText.setEnabled(false);
            }
        }else{
            textRequestId.setEnabled(false);
            txtDescription.setEnabled(true);
            txtSummary.setEnabled(true);
            txtGroup.setEnabled(true);
            txtPeople.setEnabled(true);
            if( null != textBugsClosed ){
                textBugsClosed.setEnabled(true);
            }
            if( null != textBranch ){
                textBranch.setEnabled(true);
            }
            validateChangeNumberTextField();
        }
    }
    
    /**
     * 是否将本次提交保存为草稿
     * @return
     */
    public boolean isDraft(){
        if( null != btnIsDraft ){
            return btnIsDraft.getSelection();
        }else{
            return false;
        }
    }
    
    public boolean isUpdate(){
        if( null != btnUpdateOrCommit ){
            return btnUpdateOrCommit.getSelection();
        }else{
            return false;
        }
    }
    
    /**
     * 返回requestId
     */
    public String getRequestId(){
        return textRequestId.getText().trim();
    }
    
    /**
     * 返回选中的Repository
     * @return
     */
    private ModelRepository getSelectedRepository() {
        ModelRepository repository = null;
        String svnUrl = null;
        try {
            svnUrl = RbSVNUrlUtils.getSVNUrlForResouce(resourcesSelectedByUser[0]);
        } catch (ReviewboardException e) {
        }
        if( null != svnUrl && !svnUrl.isEmpty()
                && null != modelRepositoryList && !modelRepositoryList.isEmpty() ){
            for( ModelRepository repositoryTmp : modelRepositoryList ){
                if( null == repositoryTmp || null == repositoryTmp.getName() 
                        || repositoryTmp.getName().trim().isEmpty()
                        || null == repositoryTmp.getPath() 
                        || repositoryTmp.getPath().trim().isEmpty()){
                    continue;
                }
                if( svnUrl.trim().toLowerCase().startsWith(repositoryTmp.getPath().trim().toLowerCase())){
                    repository = repositoryTmp;
                    break;
                }
            }
        }
        return repository;
    }
    
    /**
     * 返回当前页面构造出来的Request
     * @return
     */
    public ModelReviewRequest getReviewRequest() throws ReviewboardException {
        ModelReviewRequest reviewRequest = null;
        if(this.isUpdate()){
            try {
                reviewRequest = reviewboardService.getReviewRequestThinkAboutDraft(
                        Integer.parseInt(textRequestId.getText().trim()), 
                        new NullProgressMonitor());
                if( null != reviewRequest ){
                    reviewRequest.setDescription(txtDescription.getText().trim());
                }
            } catch (NumberFormatException e) {
                throw new ReviewboardException(e);
            } catch (ReviewboardException e){
                throw e;
            }
        }else{
            reviewRequest = new ModelReviewRequest();
            ModelRepository selectedRepo = getSelectedRepository();  
            reviewRequest.setRepository(selectedRepo);       
            if (selectedRepo!=null && 
                selectedRepo.getTool()!=null&&
                selectedRepo.getTool().equalsIgnoreCase(CHANGE_NUMBER_ENABLE_REPOSITORY)) {
                if (null != changeNumText && changeNumText.getEnabled() && changeNumText.getText().trim().length() > 0) {
                    try {
                        reviewRequest.setChangeNumber(Integer.parseInt(changeNumText.getText().trim()));
                    } catch (NumberFormatException e) {
                    }
                }
            }
            
            reviewRequest.setDescription(txtDescription.getText());
            reviewRequest.setSummary(txtSummary.getText().trim());
            if( null != textBranch ){
                reviewRequest.setBranch(textBranch.getText().trim());
            }else{
                reviewRequest.setBranch( EMPTY_STRING );
            }
            if( null != textBugsClosed ){
                List<Integer> bugClosedList = new ArrayList<Integer>();
                for (String bugClosed : splitString(textBugsClosed.getText().trim())) {
                    try {
                        bugClosedList.add(Integer.parseInt(bugClosed.trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                reviewRequest.getBugsClosedList().clear();
                reviewRequest.getBugsClosedList().addAll(bugClosedList);
            }else{
                reviewRequest.getBugsClosedList().clear();
            }
            reviewRequest.setTargetPeopleList(peopleList);
            reviewRequest.setTargetGroupList(groupList);
        }
        return reviewRequest;
    }
    
    /**
     * 给界面设置默认值，便于用户更方便地使用
     */
    public void setDefaultValue(){
        RbConfig rbConfig = RbConfigReader.getRbConfig(null);
        if( null != rbConfig ){
            if( null != textBranch ){
                textBranch.setText( null == rbConfig.getBranch() ? "" : rbConfig.getBranch().trim() );
            }
            txtPeople.setText( null == rbConfig.getPeoples() ? "" : rbConfig.getPeoples().trim() );
            txtGroup.setText( null == rbConfig.getGroups() ? "" : rbConfig.getGroups().trim() );
        }
    }
    
    //以下是各种验证
    
    /**
     * 提交前的认证
     */
    public boolean validateTextField() {
        List<String> errorInfos = new ArrayList<String>();
        validateTextFieldSpecial(errorInfos);
        //如果是修改
        if(isUpdate()){
          //request ID 是必须的
            if( textRequestId.getText().trim().equals("") ){
                errorInfos.add(RbSubclipseMessages.getString("REQURIED_REQUEST_ID"));
            }
            try {
                Integer.parseInt(textRequestId.getText().trim());
            } catch (NumberFormatException e) {
                errorInfos.add(RbSubclipseMessages.getString("VALID_REQUEST_ID"));
            }
            
        }
        //如果是新增
        else{
            if( txtSummary.getText().trim().equals("") ){
                errorInfos.add(RbSubclipseMessages.getString("REQURIED_SUMMARY"));
            }
            if( txtPeople.getText().trim().equals("") ){
                errorInfos.add(RbSubclipseMessages.getString("REQURIED_REVIEWPEOPLES"));
            }else{
                List<String> peoples = splitString(txtPeople.getText().trim());
                //先清空数据
                peopleList.clear();
                if( null !=  modelUserList && !modelUserList.isEmpty() ){
                    for( String peopleName : peoples ){
                        if( peopleName.trim().equals("") )continue;
                        ModelUser userTmp = null;
                        for( ModelUser user : modelUserList){
                            if( user.getUsername().trim().equalsIgnoreCase(peopleName.trim()) ){
                                userTmp = user;
                                break;
                            }
                        }
                        if( null != userTmp ){
                            peopleList.add(userTmp);
                        }
                        else{
                            errorInfos.add(MessageFormat.format(
                                    RbSubclipseMessages.getString("VALID_REVIEWPEOPLES_0"), 
                                    new Object[]{ peopleName }));
                        }
                    }
                }
                if( peopleList.isEmpty() ){
                    errorInfos.add(RbSubclipseMessages.getString("VALID_REVIEWPEOPLES_1"));
                }
            }
            if( !txtGroup.getText().trim().equals("") ){
                List<String> groups = splitString(txtGroup.getText().trim());
                //先清空数据
                groupList.clear();
                if( null != modelReviewGroupList && !modelReviewGroupList.isEmpty() ){
                    for( String groupName : groups ){
                        if( groupName.trim().equals("") )continue;
                        ModelReviewGroup reviewGroupTmp = null;
                        for( ModelReviewGroup reviewGroup : modelReviewGroupList){
                            if( reviewGroup.getName().trim().equalsIgnoreCase(groupName.trim()) ){
                                reviewGroupTmp = reviewGroup;
                                break;
                            }
                        }
                        if( null != reviewGroupTmp ){
                            groupList.add(reviewGroupTmp);
                        }
                        else{
                            errorInfos.add(MessageFormat.format(RbSubclipseMessages.getString("VALID_GROUPS_0"), new Object[]{ groupName }));
                        }
                    }
                }
                if( groupList.isEmpty() ){
                    errorInfos.add(RbSubclipseMessages.getString("VALID_GROUPS_1"));
                }
            }
            if( null != textBugsClosed && !textBugsClosed.getText().trim().equals("") ){
                List<String> bugCloses = splitString(textBugsClosed.getText().trim());
                for( String bugClose : bugCloses ){
                    if( bugClose.trim().equals("") )continue;
                    try {
                        Integer.parseInt(bugClose.trim());
                    } catch (NumberFormatException e) {
                        errorInfos.add(MessageFormat.format(RbSubclipseMessages.getString("VALID_BUGCLOSED"), new Object[]{ bugClose.trim() }));
                    }
                }
            }
            if( null == this.getSelectedRepository() ){
                errorInfos.add(RbSubclipseMessages.getString("VALID_REPOSITORIES"));
            }
        }
        //新增和修改都需要验证
        if( txtDescription.getText().trim().equals("") ) {
            errorInfos.add(RbSubclipseMessages.getString("REQURIED_DESCRIPTION"));
        }
        if( errorInfos.isEmpty() ){
            this.addRecentDescriptionBean();
            return true;
        }
        this.openErrorInformationBox(errorInfos);
        return false;
    }
    
    /**
     * 打开错误提示信息
     * @param errorInfos
     */
    protected void openErrorInformationBox( List<String> errorInfos ){
        if( null == errorInfos || errorInfos.isEmpty() ){
            return ;
        }
        StringBuilder errorSb = new StringBuilder();
        int index = 0;
        for(String errorInfo : errorInfos){
            if( index != 0 ) errorSb.append(System.getProperty("line.separator"));
            errorSb.append(errorInfo);
            index ++ ;
        }
        MessageDialog.openError( null,
                RbSubclipseMessages.getString("ERROR_INFORMATION_LABLE"),
                errorSb.toString());
    }
    
    /**
     * changeNumber验证
     */
    protected void validateChangeNumberTextField(){
        if ( null != changeNumText ){
            changeNumText.setEnabled(false);
        }
        ModelRepository selectedRepo = getSelectedRepository();         
        if (null != changeNumText && selectedRepo != null && selectedRepo.getTool()!=null 
                && selectedRepo.getTool().equalsIgnoreCase(CHANGE_NUMBER_ENABLE_REPOSITORY)) {
            changeNumText.setEnabled(true);
        }
    }
    
    /**
     * 保存最近的Description
     */
    protected void addRecentDescriptionBean(){
        if( !txtDescription.getText().trim().isEmpty()){
            RecentDescriptionBeansManager.getSingletonInstance().addRecentDescription(txtDescription.getText(), null);
        }
    }

    /**
     * 截取出content的最后一个“，”或“、”或","或"；"或";"，并放到list的后面
     * @param list
     * @param content
     * @return
     */
    private List<String> getNewListForProposal(List<?> list, String content) {
        String contentTmp = content.replaceAll("，", ",").replaceAll("、", ",").replaceAll(";", ",").replaceAll("；", ",");
        int index = contentTmp.lastIndexOf(",");
        if( index != -1){
            content = content.substring(0, index+1);
        }else{
            content = "";
        }
        List<String> result = new ArrayList<String>();
        for (Object string : list) {
            result.add(content+string.toString());
        }
        return result;
    }
    
    private List<String> splitString(String string) {
        List<String> result = new ArrayList<String>();
        string = string.replaceAll("，", ",").replaceAll("、", ",").replaceAll(";", ",").replaceAll("；", ",");
        for (String item : string.split(",")) {
            result.add(item.trim());
        }
        return result;
    }

}
