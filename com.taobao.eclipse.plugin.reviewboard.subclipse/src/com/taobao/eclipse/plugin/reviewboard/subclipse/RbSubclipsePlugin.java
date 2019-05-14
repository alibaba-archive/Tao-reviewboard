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
package com.taobao.eclipse.plugin.reviewboard.subclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.taobao.eclipse.plugin.reviewboard.core.ReviewboardCorePlugin;
import com.taobao.eclipse.plugin.reviewboard.subclipse.model.RecentDescriptionBeansManager;

/**
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class RbSubclipsePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "com.taobao.eclipse.plugin.reviewboard.subclipse";
    
    /**
     * The singleton plug-in instance
     */
    private static RbSubclipsePlugin plugin;
    
    public RbSubclipsePlugin(){
        super();
        plugin = this;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Platform.getPlugin(ReviewboardCorePlugin.PLUGIN_ID);
        RecentDescriptionBeansManager.getSingletonInstance();
    }
    

    @Override
    public void stop(BundleContext context) throws Exception {
        RecentDescriptionBeansManager.getSingletonInstance().writeCache();
        super.stop(context);
    }
    
    public static RbSubclipsePlugin getPlugin() {
        return plugin;
    }
    
    public IPath getPluginPath() {
        IPath stateLocation = Platform.getStateLocation(getBundle());
        return stateLocation;
    }

}
