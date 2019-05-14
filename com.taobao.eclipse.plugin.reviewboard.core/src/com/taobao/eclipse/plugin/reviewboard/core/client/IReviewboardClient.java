/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
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
package com.taobao.eclipse.plugin.reviewboard.core.client;

import com.taobao.eclipse.plugin.reviewboard.core.service.ReviewboardHttpClient;

/**
 * ��˵��:Interface for Review Board operations.
 * 
 * @author Markus Knittig
 * @author ��ʱ
 */
public interface IReviewboardClient{
    
    /**
     * ����ReviewboardHttpClient
     * @return
     */
    ReviewboardHttpClient getHttpClient();
    
    /**
     * ���ر����ReviewboardClientData
     * @return
     */
    ReviewboardClientData getClientData();

    /**
     * ���أ����������Ƿ񱻸��¹�
     * @return
     */
    boolean hasRepositoryData();

    /**
     * ����location url���û��������롣�Żظ�ʽ:new String[]{locationUrl,userName,password};
     * @return
     */
    String[] getLocationUrlAndUserNameAndPassword();
    
    /**
     * ����Server url
     * @return
     */
    public String getServerUrl();

    public void setServerUrl(String serverUrl);

    public String getUsername();

    public void setUsername(String username);

    public String getPassword();
    
    public void setPassword(String password);
}
