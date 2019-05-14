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
package com.taobao.eclipse.plugin.reviewboard.subclipse.clientmanager;

import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IClientRepositoryAwair;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IReviewboardClientRepository;

/**
 * ��˵��:ȡ��ReviewboardClientRepository
 * 
 * @author ���� 
 * ����ʱ�䣺2011-01-21
 *
 */
public class SVNClientRepositoryAwair implements IClientRepositoryAwair {
    
    private static IReviewboardClientRepository reviewboardClientRepository = null;

	public void setReviewboardClientRepository(IReviewboardClientRepository rbClientRepository) {
        reviewboardClientRepository = rbClientRepository;
	}

    public static IReviewboardClientRepository getReviewboardClientRepository() {
        return reviewboardClientRepository;
    }
	
}
