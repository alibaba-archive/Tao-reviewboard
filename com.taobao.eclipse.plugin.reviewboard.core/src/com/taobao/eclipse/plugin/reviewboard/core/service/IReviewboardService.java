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
 * ��˵��:Interface for Review Board operations.
 * 
 * @author ���� 
 * @author Markus Knittig
 */
public interface IReviewboardService extends java.io.Serializable {
    
    /**
     * ����RepositoryData
     * @param force �Ƿ�ǿ��ˢ�¡������false����ô�����������Ѿ������¹��Ļ�����ˢ��
     * @param monitor
     */
    void updateRepositoryData(boolean force, IProgressMonitor monitor);
    
    /**
     * ��½��֤
     * @param monitor
     * @return
     */
    boolean validCredentials(String username, String password, IProgressMonitor monitor);

    /**
     * ��ѯ����������ReviewRequest
     * @param query
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    List<ModelReviewRequest> getReviewRequests(String query, IProgressMonitor monitor) throws ReviewboardException;

    /**
     * ����reviewRequestId����ReviewRequest
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequest getReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * ����reviewRequestId����ReviewRequest�����Ͳݸ������������һ������������ReviewRequest
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequest getReviewRequestThinkAboutDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * ����reviewRequestId����ReviewRequestDraft
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequestDraft getReviewRequestDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * ����reviewRequestId�鿴ReviewRequest�ǲ��ǲݸ�״̬������ǲݸ�״̬���򷵻�true�����򷵻�false
     * @param reviewRequestId
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    boolean isReviewRequestDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException;

    /**
     * ����reviewRequestId����Review
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
     * ����ReviewRequest Draft����Ϣ
     * @param reviewRequest
     * @param monitor
     * @throws ReviewboardException
     */
    void updateReviewRequest(ModelReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * ����ReviewRequest Draft����Ϣ(��������ֵ��Ԫ��)
     * @param reviewRequest
     * @param monitor
     * @throws ReviewboardException
     */
    void updateReviewRequestFilterNoValue(ModelReviewRequest reviewRequest, 
            IProgressMonitor monitor) throws ReviewboardException;
    
    /**
     * ����һ���հ׵�ReviewRequest
     * @param reviewRequest
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    ModelReviewRequest newReviewRequest(ModelReviewRequest reviewRequest, 
            IProgressMonitor monitor) throws ReviewboardException;

    /**
     * ����һ��Request�������󣬻��޸�������������ԣ����ύ�׸�diff�ļ�
     * @throws ReviewboardException
     */
    ModelReviewRequest newReviewRequestAll(ModelReviewRequest reviewRequestNew, String baseUrlDir, 
            File[] fileDiffs, IProgressMonitor monitor) throws ReviewboardException,OperationException;
    
    /**
     * �ϴ�diff(������ʽ)
     * @param review_request_id
     * @param diffContent
     * @param monitor
     * @return
     */
    void uploadDiff( int review_request_id, String baseUrlDir, String diffContent, 
            String charsetName, IProgressMonitor monitor )throws ReviewboardException,OperationException;
    
    /**
     * �ϴ�diff(�ļ���ʽ)
     * @param review_request_id
     * @param diffFile
     * @param monitor
     * @return
     */
    void uploadDiff( int review_request_id, String baseUrlDir, File fileDiff, 
            File parentDiffFile, IProgressMonitor monitor )throws ReviewboardException,OperationException;

    /**
     * �ϴ�diff(�ļ���ʽ)
     * @param review_request_id
     * @param diffFile
     * @param monitor
     * @return
     */
    void uploadDiff( int review_request_id, String baseUrlDir, File[] fileDiffs, 
            File parentDiffFile, IProgressMonitor monitor )throws ReviewboardException,OperationException;

   
}
