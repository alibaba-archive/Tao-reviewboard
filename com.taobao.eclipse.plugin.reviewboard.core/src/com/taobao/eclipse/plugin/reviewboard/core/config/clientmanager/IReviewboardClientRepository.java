/*
* (C) 2007-2011 Alibaba Group Holding Limited
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
*
* If you have any question, please contact:ǧѾ <qianya@taobao.com>
* Authors:���� <zhiqing.ht@taobao.com>����ʱ<yinshi.nc@taobao.com>
*
*/
package com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;

/**
 * ��˵��:IReviewboardClientRepository��Client��ClientData����
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 *
 */
public interface IReviewboardClientRepository {
    
    /**
     * ���ݷ�������ַ��ȡIReviewboardClient
     * @param refreshForce �Ƿ�ǿ��ˢ�»���
     */
    public IReviewboardClient getClient(boolean refreshForce, IProgressMonitor monitor);
    
    /**
     * ���ݷ�������ַ��ȡIReviewboardClient
     * @param serverUrl
     */
    public IReviewboardClient getClient(boolean refreshForce, String serverUrl, String userName, String password, String charactorEncoding) ;
        
    /**
     * ��ȡIReviewboardClient
     */
    public IReviewboardClient getClientFromUrlToClientMap();
    
    /**
     * ���ñ��ػ���
     */
    public void resetCache();
    
    public List<IClientRepositoryAwair> clientRepositoryAwairRegiest();
    
}
