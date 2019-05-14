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

import java.util.List;

import org.json.JSONObject;

import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.core.model.DiffUploadErrorBean;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelRepository;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReview;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewGroup;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequest;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequestDraft;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelUser;
import com.taobao.eclipse.plugin.reviewboard.core.util.JsonUtil;

/**
 * Class for converting Review Board API call responses (JSON format) to Java objects.
 *
 * @author Markus Knittig
 * @author 智清 
 */
public class RestfulReviewboardReader {

    public boolean isStatOK(String source) throws ReviewboardException {
        try {
            JSONObject jsonStat = new JSONObject(source);
            return jsonStat.getString("stat").equals("ok");
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public String getErrorMessage(String source) throws ReviewboardException {
        try {
            JSONObject jsonStat = new JSONObject(source);
            if (jsonStat.getString("stat").equals("fail")) {
                JSONObject jsonError = jsonStat.getJSONObject("err");
                return jsonError.getString("msg") + " (Errorcode: " +
                        jsonError.getString("code") + ")!";
            } else {
                throw new IllegalStateException("Request didn't fail!");
            }
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public String getErrorCode(String source) throws ReviewboardException {
        try {
            JSONObject jsonStat = new JSONObject(source);
            if (jsonStat.getString("stat").equals("fail")) {
                JSONObject jsonError = jsonStat.getJSONObject("err");
                return jsonError.getString("code");
            } else {
                throw new IllegalStateException("Request didn't fail!");
            }
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public DiffUploadErrorBean getDiffUploadErrorBean(String source) throws ReviewboardException {
        DiffUploadErrorBean diffUploadErrorBean = new DiffUploadErrorBean();
        try {
            JSONObject jsonStat = new JSONObject(source);
            if (jsonStat.getString("stat").equals("fail")) {
                JSONObject jsonError = jsonStat.getJSONObject("err");
                String code = jsonError.getString("code");
                String msg = jsonError.getString("msg");
                if( null == code || code.trim().isEmpty()){
                    throw new IllegalStateException("Upload diff's error code is not 207!");
                }
                diffUploadErrorBean.setCode(code);
                diffUploadErrorBean.setFile(jsonStat.getString("file"));
                diffUploadErrorBean.setMsg(msg);
                diffUploadErrorBean.setRevision(jsonStat.getString("revision"));
            } else {
                throw new IllegalStateException("Request didn't fail!");
            }
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
        return diffUploadErrorBean;
    }

    public List<ModelUser> readUsers(String source) throws ReviewboardException {
        try {
            JSONObject jsonUsers = new JSONObject(source);
            return JsonUtil.parseModelList(ModelUser.class, jsonUsers.getJSONArray("users") );
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ModelReviewGroup> readGroups(String source) throws ReviewboardException {
        try {
            JSONObject jsonGroups = new JSONObject(source);
            return JsonUtil.parseModelList(ModelReviewGroup.class, jsonGroups.getJSONArray("groups"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    /**
     * 从JSON数据中读取出ReviewRequest集合.
     */
    public List<ModelReviewRequest> readReviewRequests(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequests = new JSONObject(source);
            List<ModelReviewRequest> reviewRequests = JsonUtil.parseModelList(ModelReviewRequest.class,
                    jsonReviewRequests.getJSONArray("review_requests"));
            return reviewRequests;
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public List<ModelReviewRequest> readReviewRequests2(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequests = new JSONObject(source);
            List<ModelReviewRequest> reviewRequests = JsonUtil.parseModelList(ModelReviewRequest.class,
                    jsonReviewRequests.getJSONArray("review_requests"));
            return reviewRequests;
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ModelRepository> readRepositories(String source) throws ReviewboardException {
        try {
            JSONObject jsonRepositories = new JSONObject(source);
            return JsonUtil.parseModelList(ModelRepository.class, jsonRepositories.getJSONArray("repositories"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public ModelReviewRequest readReviewRequest(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return JsonUtil.parseModel(ModelReviewRequest.class, jsonReviewRequest.getJSONObject("review_request"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
    public ModelReviewRequestDraft readReviewRequestDraft(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return JsonUtil.parseModel(ModelReviewRequestDraft.class, jsonReviewRequest.getJSONObject("draft"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ModelReview> readReviews(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return JsonUtil.parseModelList(ModelReview.class, jsonReviewRequest.getJSONArray("reviews"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }

    public List<ModelReview> readReplies(String source) throws ReviewboardException {
        try {
            JSONObject jsonReviewRequest = new JSONObject(source);
            return JsonUtil.parseModelList(ModelReview.class, jsonReviewRequest.getJSONArray("replies"));
        } catch (Exception e) {
            throw new ReviewboardException(e.getMessage(), e);
        }
    }
    
}
