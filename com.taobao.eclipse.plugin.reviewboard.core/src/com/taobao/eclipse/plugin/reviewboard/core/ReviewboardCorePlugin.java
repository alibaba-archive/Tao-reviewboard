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
package com.taobao.eclipse.plugin.reviewboard.core;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.RELOAD_MINUTE_CACHE_CLIENTDATA;

import java.util.Timer;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.ReviewboardClientRepository;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.ReviewboardClientRepository.ClientDataReloadTimeTask;

/**
 * 类说明:Bundle-Activator
 *
 * @author 智清
 * 创建时间：2011-08-15
 */
public class ReviewboardCorePlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.taobao.eclipse.plugin.reviewboard.core";

	private static ReviewboardCorePlugin plugin;

	public static ReviewboardCorePlugin getDefault() {
		return plugin;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ReviewboardClientRepository rbClientRepositorySingleton = ReviewboardClientRepository.getSingletonInstance();
        rbClientRepositorySingleton.clientRepositoryAwairRegiest();
        try {
            // 启动定时器，用于定时加载cache
            ClientDataReloadTimeTask clientDataReloadTimeTask = new ClientDataReloadTimeTask();
            Timer fileConfigReloadTime = new Timer(true);
            // 指定分钟后首次执行操作，每隔指定时间触发一次
            fileConfigReloadTime.schedule(clientDataReloadTimeTask,
                    RELOAD_MINUTE_CACHE_CLIENTDATA * 60 * 1000, RELOAD_MINUTE_CACHE_CLIENTDATA * 60 * 1000);
        } catch (Exception e) {
        }
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}
