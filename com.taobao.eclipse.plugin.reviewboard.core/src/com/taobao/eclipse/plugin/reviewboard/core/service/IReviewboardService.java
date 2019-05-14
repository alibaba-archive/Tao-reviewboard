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
* If you have any question, please contact:千丫 <qianya@taobao.com>
* Authors:智清 <zhiqing.ht@taobao.com>；银时<yinshi.nc@taobao.com>
*
*/
package com.taobao.eclipse.plugin.reviewboard.core.service;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.taobao.eclipse.plugin.reviewboard.core.exception.OperationException;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReview;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequest;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequestDraft;

/**
 * 类说明:Interface for Review Board operations.
 * 
 * @author 智清 
 * @author Markus Knittig
 */
public interface IReviewboardService extends java.io.Serializable {
    
    /**
     * 更新RepositoryData
     * @param force 是否强制刷新。如果是false，那么当缓存数据已经被更新过的话，则不刷新
     * @param monitor
     */
    void updateRepositoryData(boolean force, IProgressMonitor monitor);
    
    /**
     * 登陆认证
     * @param monitor
     * @return
     */
    boolean validCredentials(String username, String password, IProgressMonitor monitor);

    /**
     * 查询符合条件的ReviewRequest
     * @param query
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    List<ModelReviewRequest> getReviewRequests(String query, IProgressMonitor monitor) throws ReviewboardException;

    /**
     * 根据reviewRequestId查找ReviewRequest
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequest getReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * 根据reviewRequestId查找ReviewRequest，并和草稿结合起来，组成一个完整的最后的ReviewRequest
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequest getReviewRequestThinkAboutDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * 根据reviewRequestId查找ReviewRequestDraft
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequestDraft getReviewRequestDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * 根据reviewRequestId查看ReviewRequest是不是草稿状态。如果是草稿状态，则返回true，否则返回false
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    boolean isReviewRequestDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;

    /**
     * 根据reviewRequestId返回Review
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    List<ModelReview> getReviews(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * Publish ReviewRequest
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    boolean publishReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;

    /**
     * 更新ReviewRequest Draft的信息
     * @param reviewRequest
     * @param monitor
     * @throws ReviewboardException
     */
    void updateReviewRequest(ModelReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * 更新ReviewRequest Draft的信息(仅更新有值的元素)
     * @param reviewRequest
     * @param monitor
     * @throws ReviewboardException
     */
    void updateReviewRequestFilterNoValue(ModelReviewRequest reviewRequest, 
            IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * 新增一个空白的ReviewRequest
     * @param reviewRequest
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequest newReviewRequest(ModelReviewRequest reviewRequest, 
            IProgressMonitor monitor) throws ReviewboardException;

    /**
     * 新增一个Request，新增后，会修改相关描述和属性，并提交首个diff文件
     * @throws ReviewboardException
     */
    ModelReviewRequest newReviewRequestAll(ModelReviewRequest reviewRequestNew, String baseUrlDir, 
            File[] fileDiffs, IProgressMonitor monitor) throws ReviewboardException,OperationException;
    
    /**
     * 上传diff(内容形式)
     * @param review_request_id
     * @param diffContent
     * @param monitor
     * @return
     */
    void uploadDiff( int review_request_id, String baseUrlDir, String diffContent, 
            String charsetName, IProgressMonitor monitor )throws ReviewboardException,OperationException;
    
    /**
     * 上传diff(文件形式)
     * @param review_request_id
     * @param diffFile
     * @param monitor
     * @return
     */
    void uploadDiff( int review_request_id, String baseUrlDir, File fileDiff, 
            File parentDiffFile, IProgressMonitor monitor )throws ReviewboardException,OperationException;

    /**
     * 上传diff(文件形式)
     * @param review_request_id
     * @param diffFile
     * @param monitor
     * @return
     */
    void uploadDiff( int review_request_id, String baseUrlDir, File[] fileDiffs, 
            File parentDiffFile, IProgressMonitor monitor )throws ReviewboardException,OperationException;

   
}
