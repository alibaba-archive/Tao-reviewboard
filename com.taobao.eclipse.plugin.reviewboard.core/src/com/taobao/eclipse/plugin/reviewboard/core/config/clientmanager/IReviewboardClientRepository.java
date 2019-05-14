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
package com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;

/**
 * 类说明:IReviewboardClientRepository，Client和ClientData缓存
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 *
 */
public interface IReviewboardClientRepository {
    
    /**
     * 根据服务器地址获取IReviewboardClient
     * @param refreshForce 是否强制刷新缓存
     */
    public IReviewboardClient getClient(boolean refreshForce, IProgressMonitor monitor);
    
    /**
     * 根据服务器地址获取IReviewboardClient
     * @param serverUrl
     */
    public IReviewboardClient getClient(boolean refreshForce, String serverUrl, String userName, String password, String charactorEncoding) ;
        
    /**
     * 获取IReviewboardClient
     */
    public IReviewboardClient getClientFromUrlToClientMap();
    
    /**
     * 重置本地缓存
     */
    public void resetCache();
    
    public List<IClientRepositoryAwair> clientRepositoryAwairRegiest();
    
}
