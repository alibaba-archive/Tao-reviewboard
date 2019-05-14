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

/**
 * 类说明:作为扩展点的接口。<BR/>
 * 通过这种方式，其它模块(如Subclipse、CVS、GIT)无需重新开发ReviewBoard注册模块，即可以获得单例的IReviewboardClientRepository，与ReviewBoard服务器交互
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 *
 */
public interface IClientRepositoryAwair {

    /**
     * @param rBClientManager 注入ReviewboardClientManager
     */
    public void setReviewboardClientRepository( IReviewboardClientRepository rbClientRepository );
    
}
