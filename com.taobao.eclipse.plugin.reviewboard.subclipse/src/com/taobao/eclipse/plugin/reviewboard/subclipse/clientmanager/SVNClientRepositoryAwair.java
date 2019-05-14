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
package com.taobao.eclipse.plugin.reviewboard.subclipse.clientmanager;

import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IClientRepositoryAwair;
import com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager.IReviewboardClientRepository;

/**
 * 类说明:取得ReviewboardClientRepository
 * 
 * @author 智清 
 * 创建时间：2011-01-21
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
