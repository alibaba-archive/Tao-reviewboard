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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;

import com.taobao.eclipse.plugin.reviewboard.subclipse.util.RbSVNDiffUtils;

/**
 * 类说明:postcommit文件选择
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ResourceSelectionTreePostCommit extends Composite {
    
    private class ResourceComparator implements Comparator<IResource> {
        public int compare(IResource obj0, IResource obj1) {
            IResource resource0 = (IResource)obj0;
            IResource resource1 = (IResource)obj1;
            return resource0.getFullPath().toOSString().compareTo(resource1.getFullPath().toOSString());
        }           
    }

    private ResourceComparator comparator = new ResourceComparator();

    private Table table;
    private List<IResource> resourceList;
    
    private CheckboxTableViewer tableViewer = null;
    
    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public ResourceSelectionTreePostCommit(Composite parent, int style, String label, 
            IResource[] resources, IToolbarControlCreator toolbarControlCreator) {
        super(parent, style);
        setLayout(new GridLayout(2, false));
        setLayoutData(new GridData(GridData.FILL_BOTH));

        ViewForm viewerPane = new ViewForm(this, SWT.BORDER | SWT.FLAT);
        viewerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        CLabel toolbarLabel = new CLabel(viewerPane, SWT.NONE) {
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return super.computeSize(wHint, Math.max(24, hHint), changed);
            }
        };
        if (label != null) {
            toolbarLabel.setText(label);
        }
        viewerPane.setTopLeft(toolbarLabel);

        int buttonGroupColumns = 1;
        if (toolbarControlCreator != null) {
            buttonGroupColumns = buttonGroupColumns + toolbarControlCreator.getControlCount();
        }
        ToolBar toolbar = new ToolBar(viewerPane, SWT.FLAT);
        viewerPane.setTopCenter(toolbar);

        ToolBarManager toolbarManager = new ToolBarManager(toolbar);

        if (toolbarControlCreator != null) {
            toolbarControlCreator.createToolbarControls(toolbarManager);
            toolbarManager.add(new Separator());
        }

        toolbarManager.update(true);

        resourceList = new ArrayList<IResource>();

        if (resources != null) {
            Arrays.sort(resources, comparator);
            for (int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];
                try {
                    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                    if (svnResource.getStatus().isManaged()) {
                        resourceList.add(resource);
                    }
                } catch (SVNException e) {
                }
            }
        }

        tableViewer = CheckboxTableViewer.newCheckList(viewerPane, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 125;
        tableViewer.getControl().setLayoutData(gd);
        table = tableViewer.getTable();

        final TableColumn newColumnTableColumn = new TableColumn(table, SWT.NONE);
        newColumnTableColumn.setWidth(740);
        newColumnTableColumn.setText("Resources for Generate Diff of Post-commit");

        table.setHeaderVisible(true);// 显示表头
        table.setLinesVisible(true);// 显示表格线
        TableLayout tLayOut = new TableLayout();// 专用于表格的布局
        table.setLayout(tLayOut);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        tLayOut.addColumnData(new ColumnWeightData(740));// ID列宽

        tableViewer.setContentProvider(new TableViewerContentProvider());
        tableViewer.setLabelProvider(new ResourceTableViewerLabelProvider());
        tableViewer.setInput(resourceList);

        viewerPane.setContent(table);

    }
    
    /**
     * 增加或删除过滤器
     * @param allowSqlReviewFilter
     * @param viewerFilter
     */
    public void handleFilter(boolean allowSqlReviewFilter, ViewerFilter viewerFilter){
        if(allowSqlReviewFilter){
            this.tableViewer.addFilter(viewerFilter);
        }else{
            this.tableViewer.removeFilter(viewerFilter);
        }
        this.tableViewer.refresh();
        this.tableViewer.setAllChecked(true);
    }

    public IResource[] getSelectedResource(){
        IResource[] resources = null;
        List<IResource> resourceList = getSelectedResourceList();
        if( null != resourceList && !resourceList.isEmpty() ){
            resources = resourceList.toArray( new IResource[resourceList.size()] );
        }
        return resources;
    }
    
    public List<IResource> getSelectedResourceList(){
        List<IResource> resourceList = new ArrayList<IResource>();
        //取得打勾的记录
        Object[] checkObjs = tableViewer.getCheckedElements();
        if( null == checkObjs || checkObjs.length==0){
            //MessageDialog.openError(parent.getShell(), "提示", "请先选择资源文件");
            return null;
        }else{
            for( Object obj : checkObjs ){
                IResource resouce = (IResource)obj;
                //确认resourceList已经不包含重复值
                boolean isExsit = false;
                for( IResource resourceTmp : resourceList){
                    if( resourceTmp.getLocation().toString().equals(resouce.getLocation().toString())){
                        isExsit = true;
                        break;
                    }
                }
                if( !isExsit ){
                    resourceList.add(resouce);
                }
            }
        }
        return resourceList;
    }
    
    public IResource[] getResourceForPostCommit(){
        IResource[] resources = this.getSelectedResource();
        if( null == resources || resources.length == 0 ){
            return null;
        }
        return RbSVNDiffUtils.integerateResourcesForSVNPostCommit(resources);
    }
    
    public CheckboxTableViewer getTableViewer(){
        return tableViewer;
    }

}
