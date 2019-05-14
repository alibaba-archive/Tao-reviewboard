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

/**
 * ��˵��:��Ϊ��չ��Ľӿڡ�<BR/>
 * ͨ�����ַ�ʽ������ģ��(��Subclipse��CVS��GIT)�������¿���ReviewBoardע��ģ�飬�����Ի�õ�����IReviewboardClientRepository����ReviewBoard����������
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 *
 */
public interface IClientRepositoryAwair {

    /**
     * @param rBClientManager ע��ReviewboardClientManager
     */
    public void setReviewboardClientRepository( IReviewboardClientRepository rbClientRepository );
    
}
