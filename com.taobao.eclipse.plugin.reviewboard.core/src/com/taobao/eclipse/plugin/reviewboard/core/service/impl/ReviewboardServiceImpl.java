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
package com.taobao.eclipse.plugin.reviewboard.core.service.impl;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.DIFF_SUFFIX;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EOL;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.FOLDER_TMP_DIFF;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.UPLOADTOSERVER_FAIL_FILE_CODE;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_DRAFT_BY_REQUESTID;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_GROUPS;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_NEW_DIFF;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_NEW_REQUESTS;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_PUBLISH_REQUEST;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_REPOSITORIES;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_REQUESTS;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_REQUEST_BY_REQUESTID;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_UPDATE_REQUEST_DRAFT;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_USERS;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;
import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.exception.OperationException;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.core.model.DiffUploadErrorBean;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelRepository;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReview;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewGroup;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequest;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewRequestDraft;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelUser;
import com.taobao.eclipse.plugin.reviewboard.core.service.IReviewboardService;
import com.taobao.eclipse.plugin.reviewboard.core.service.RestfulReviewboardReader;
import com.taobao.eclipse.plugin.reviewboard.core.service.ReviewboardHttpClient;
import com.taobao.eclipse.plugin.reviewboard.core.util.GuessStreamEncoding;
import com.taobao.eclipse.plugin.reviewboard.core.util.IOUtils;
import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;

/**
 * Description: IReviewboardService impl for API V2.0  
 * @author Markus Knittig
 * @author 银时 <a href="mailto:yinshi.nc@taobao.com">银时</a>  
 */
public class ReviewboardServiceImpl implements IReviewboardService {
    
    protected final RestfulReviewboardReader reviewboardReader;

    protected ReviewboardHttpClient httpClient;
    
    protected IReviewboardClient reviewboardClient;
    
    public ReviewboardServiceImpl( IReviewboardClient reviewboardClient ) {
        reviewboardReader = new RestfulReviewboardReader();
        this.httpClient = reviewboardClient.getHttpClient();
        this.reviewboardClient = reviewboardClient;
    }

    /**
     * Get all Repository on rb server.
     */
    private List<ModelRepository> getRepositories(IProgressMonitor monitor) throws ReviewboardException {
        List<ModelRepository> repositoryList = new ArrayList<ModelRepository>();
        int start = 0;
        int maxResult = 200;
        String repoistioryUri = MessageFormat.format(URI_REPOSITORIES, new Object[]{ String.valueOf(start), String.valueOf(maxResult) });
        String result = httpClient.executeGet(repoistioryUri, monitor);
        List<ModelRepository> repositoryListTemp =  reviewboardReader.readRepositories(result); 
        //In Api 2.0, There is a limit of 200 max-results, so need while to get all result.
        while(null != repositoryListTemp && !repositoryListTemp.isEmpty() ){
            repositoryList.addAll(repositoryListTemp );
            monitorWorked(monitor);
            start += maxResult;
            repoistioryUri = MessageFormat.format(URI_REPOSITORIES, new Object[]{ String.valueOf(start), String.valueOf(maxResult) });
            result = httpClient.executeGet(repoistioryUri, monitor);
            repositoryListTemp =  reviewboardReader.readRepositories(result);
        }
        return repositoryList;
    }

    /**
     * Get all user on rb server.
     */
    private List<ModelUser> getUsers(IProgressMonitor monitor) throws ReviewboardException {
        
        List<ModelUser> userList = new ArrayList<ModelUser>();
        
        int start = 0;
        int maxResult  = 200;
        
        String userUri = MessageFormat.format(URI_USERS, new Object[]{ String.valueOf(start), String.valueOf(maxResult) });
        String result = httpClient.executeGet(userUri, monitor);
        List<ModelUser> userListTemp = reviewboardReader.readUsers(result); 
      
        while(null != userListTemp && !userListTemp.isEmpty() ){
            userList.addAll(userListTemp );
            monitorWorked(monitor);
            start += maxResult;
            userUri = MessageFormat.format(URI_USERS, new Object[]{ String.valueOf(start), String.valueOf(maxResult) });
            result = httpClient.executeGet(userUri, monitor);
            userListTemp =   reviewboardReader.readUsers(result);
        }
        return userList;
    }
    
    /**
     * Get a detail user on rb server by userHrefUrl
     */
    public ModelUser getUserByHrefUrl(String userHrefUrl, IProgressMonitor monitor) throws ReviewboardException {
        String result = httpClient.executeGet( userHrefUrl, monitor);
        List<ModelUser> userList = reviewboardReader.readUsers(result); 
        if(null != userList && !userList.isEmpty() ){
            return userList.get( 0 );
        }
        return null;
    }
    
    /**
     * Get all group on rb server.
     */
    private List<ModelReviewGroup> getReviewGroups(IProgressMonitor monitor) throws ReviewboardException {
        List<ModelReviewGroup> reviewGroupList = new ArrayList<ModelReviewGroup>();
        
        int start = 0;
        int maxResult  = 200;
        
        String groupUri = MessageFormat.format(URI_GROUPS, new Object[]{ String.valueOf(start), String.valueOf(maxResult) });
        String result = httpClient.executeGet(groupUri, monitor);
        if( null == result || result.trim().isEmpty() ){
            return reviewGroupList;
        }
        
        List<ModelReviewGroup> groupListTemp =  reviewboardReader.readGroups(result); 
      
        while(null != groupListTemp && !groupListTemp.isEmpty() ){
            reviewGroupList.addAll(groupListTemp );
            monitorWorked(monitor);
            start += maxResult;
            groupUri = MessageFormat.format(URI_GROUPS, new Object[]{ String.valueOf(start), String.valueOf(maxResult) });
            result = httpClient.executeGet(groupUri, monitor);
            groupListTemp =   reviewboardReader.readGroups(result);
        }
        return reviewGroupList;
    }
    
    /**
     * Get reviewRequests with query.
     */
    public List<ModelReviewRequest> getReviewRequests(String query, IProgressMonitor monitor) throws ReviewboardException {
        String uri = httpClient.executeGet(URI_REQUESTS + query, monitor);
        List<ModelReviewRequest> reviewRequestList = reviewboardReader.readReviewRequests(uri);
        return reviewRequestList;
    }
    
    /**
     * Get reviewRequests By reviewRequestId.
     */
    public ModelReviewRequest getReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        String resultContent = httpClient.executeGet(URI_REQUESTS + reviewRequestId + "/", monitor);
        if(reviewboardReader.isStatOK(resultContent)){
            return reviewboardReader.readReviewRequest(resultContent);
        }else{
            return null;
        }
    }
    
    /**
     * Updates the status of the review request.
     */
    public boolean publishReviewRequest(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException{
        
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("public", "1");
        
        String publicUri = MessageFormat.format(URI_PUBLISH_REQUEST, new Object[]{ String.valueOf(reviewRequestId) });
        String resultContent = httpClient.executePut(publicUri, parameters, null, monitor);
        boolean result = reviewboardReader.isStatOK(resultContent);
        return result;
    }

    /**
     * New a review request.
     */
    public ModelReviewRequest newReviewRequest(ModelReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException {
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("repository", String.valueOf(reviewRequest.getRepository().getId()));
        if (reviewRequest.getChangeNumber() != null) {
            parameters.put("changenum", String.valueOf(reviewRequest.getChangeNumber()));
        }
        
        String result = httpClient.executePost( URI_NEW_REQUESTS, parameters, monitor);

        ModelReviewRequest newReviewRequest = reviewboardReader.readReviewRequest(result);
        reviewRequest.setId(newReviewRequest.getId());
        reviewRequest.setTimeAdded(newReviewRequest.getTimeAdded());
        reviewRequest.setLastUpdated(newReviewRequest.getLastUpdated());
        reviewRequest.setSubmitter(newReviewRequest.getSubmitter());
        
        return reviewRequest;
    }
    
    /**
     * Returns the current draft of a review request.
     */
    public ModelReviewRequestDraft getReviewRequestDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException{
        ModelReviewRequestDraft reviewRequestDraft = null;
        String uri = MessageFormat.format(URI_DRAFT_BY_REQUESTID, new Object[]{ String.valueOf(reviewRequestId) });
        String resultContent = httpClient.executeGet(uri, monitor);
        if(!reviewboardReader.isStatOK(resultContent)){
            return reviewRequestDraft;
        }
        reviewRequestDraft = reviewboardReader.readReviewRequestDraft(resultContent);
        return reviewRequestDraft;
    }
    
    public boolean isReviewRequestDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException{
        String uri = MessageFormat.format(URI_DRAFT_BY_REQUESTID, new Object[]{ String.valueOf(reviewRequestId) });
        String resultContent = null;
        try {
            //Since in API 2.0, if not a draft, it will throw a 404 exception. So here must catch it and return false;
            resultContent = httpClient.executeGet(uri, monitor);
        } catch (Exception e) {
            return false;
        }
        return reviewboardReader.isStatOK( resultContent );
    }

    public ModelReviewRequest getReviewRequestThinkAboutDraft(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException{
        ModelReviewRequest reviewRequest = null;
        try {
            reviewRequest = this.getReviewRequest(reviewRequestId, monitor);
        } catch (Exception e) {
            //无法获取review request.
        }
        ModelReviewRequestDraft reviewRequestDraft = null;
        try {
            reviewRequestDraft = this.getReviewRequestDraft(reviewRequestId, monitor);
        } catch (Throwable e) {
            // 没有拿到draff.
        }
        if( null == reviewRequestDraft ){
            return reviewRequest;
        }
        reviewRequest.setBranch(reviewRequestDraft.getBranch());
        reviewRequest.setSummary(reviewRequestDraft.getSummary());
        reviewRequest.setLastUpdated(reviewRequestDraft.getLastUpdated());
        reviewRequest.setBugsClosedList(reviewRequestDraft.getBugsClosedList());
        reviewRequest.setDescription(reviewRequestDraft.getDescription());
        reviewRequest.setTestingDone(reviewRequestDraft.getTestingDone());
        reviewRequest.setTargetGroupList(reviewRequestDraft.getTargetGroupList());
        reviewRequest.setTargetPeopleList(reviewRequestDraft.getTargetPeopleList());
        return reviewRequest;
    }
    
    /**
     * Get a a particular review.
     */
    public List<ModelReview> getReviews(int reviewRequestId, IProgressMonitor monitor) throws ReviewboardException {
        String uri = MessageFormat.format(URI_REQUEST_BY_REQUESTID, new Object[]{ String.valueOf(reviewRequestId) });
        List<ModelReview> result = reviewboardReader.readReviews(httpClient.executeGet(uri, monitor));
        return result;
    }
    
    /**  
     * Updates a draft of a review request.
     */
    public void updateReviewRequestFilterNoValue(ModelReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException{
        
        Map<String, String> parameters = new HashMap<String, String>();
        if( null != reviewRequest.getSummary() && !reviewRequest.getSummary().trim().equals("")){
            parameters.put("summary", reviewRequest.getSummary() );
        }
        if( null != reviewRequest.getDescription() && !reviewRequest.getDescription().trim().equals("")){
            parameters.put("description", reviewRequest.getDescription() );
        }
        if( null != reviewRequest.getTestingDone() && !reviewRequest.getTestingDone().trim().equals("")){
            parameters.put("testing_done", reviewRequest.getTestingDone() );
        }
        if( null != reviewRequest.getBranch() && !reviewRequest.getBranch().trim().equals("")){
            parameters.put("branch", reviewRequest.getBranch());
        }
        if( null != reviewRequest.getBugsClosedList() && !reviewRequest.getBugsClosedList().isEmpty() ){
            parameters.put("bugs_closed", ReviewboardUtil.joinList(reviewRequest.getBugsClosedList()));
        }
        if( null != reviewRequest.getTargetGroupList() && !reviewRequest.getTargetGroupList().isEmpty() ){
            parameters.put("target_groups", ReviewboardUtil.joinList(reviewRequest.getTargetGroupList()));
        }
        if( null != reviewRequest.getTargetPeopleList() && !reviewRequest.getTargetPeopleList().isEmpty() ){
            parameters.put("target_people", ReviewboardUtil.joinList(reviewRequest.getTargetPeopleList()));
        }
        if( null != reviewRequest.getChangeDescription()){
            parameters.put("changedescription", reviewRequest.getChangeDescription() );
        }
        
        String uri = MessageFormat.format(URI_UPDATE_REQUEST_DRAFT, new Object[]{ String.valueOf(reviewRequest.getId()) });
        httpClient.executePut( uri, parameters, null, monitor);
    }
    
    public void updateReviewRequest(ModelReviewRequest reviewRequest, IProgressMonitor monitor) throws ReviewboardException {

        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("summary", null == reviewRequest.getSummary() ? "" : reviewRequest.getSummary() );
        parameters.put("description", null == reviewRequest.getDescription() ? "" : reviewRequest.getDescription());
        parameters.put("testing_done", null == reviewRequest.getTestingDone() ? "" : reviewRequest.getTestingDone());
        parameters.put("branch", null == reviewRequest.getBranch() ? "" : reviewRequest.getBranch());
        if( null != reviewRequest.getBugsClosedList()){
            parameters.put("bugs_closed", ReviewboardUtil.joinList(reviewRequest.getBugsClosedList()));
        }
        if( null != reviewRequest.getTargetGroupList()){
            parameters.put("target_groups", ReviewboardUtil.joinList(reviewRequest.getTargetGroupList()));
        }
        if( null != reviewRequest.getTargetPeopleList()){
            parameters.put("target_people", ReviewboardUtil.joinList(reviewRequest.getTargetPeopleList()));
        }
        parameters.put("changedescription", null == reviewRequest.getChangeDescription() ? "" : reviewRequest.getChangeDescription());
        
        String uri = MessageFormat.format(URI_UPDATE_REQUEST_DRAFT, new Object[]{ String.valueOf(reviewRequest.getId()) });
        httpClient.executePut( uri, parameters, null, monitor);
        
    }

    /** 
     * Update the local RepositoryData.
     * 
     */
    public void updateRepositoryData(boolean force, IProgressMonitor monitor) {
        if (reviewboardClient.hasRepositoryData() && !force) {
            return;
        }

        try {
            monitor.subTask(RbCoreMessages.getString("PROGRESS_READING_GROUPS") + reviewboardClient.getServerUrl());
            reviewboardClient.getClientData().getReviewGroupList().clear();
            reviewboardClient.getClientData().getReviewGroupList().addAll(getReviewGroups(monitor));
            monitorWorked(monitor);

            monitor.subTask(RbCoreMessages.getString("PROGRESS_READING_USERS") + reviewboardClient.getServerUrl());
            reviewboardClient.getClientData().getUserList().clear();
            reviewboardClient.getClientData().getUserList().addAll(getUsers(monitor));
            monitorWorked(monitor);

            monitor.subTask(RbCoreMessages.getString("PROGRESS_READING_REPOSITORIES") + reviewboardClient.getServerUrl());
            reviewboardClient.getClientData().getRepositoryList().clear();
            reviewboardClient.getClientData().getRepositoryList().addAll(getRepositories(monitor));
            monitorWorked(monitor);

            reviewboardClient.getClientData().lastupdate = new Date().getTime();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void monitorWorked(IProgressMonitor monitor) {
        monitor.worked(1);
        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    public boolean validCredentials(String username, String password, IProgressMonitor monitor) {
        try {
            if( null == username || username.trim().isEmpty()
                    || null == password || password.trim().isEmpty() ){
                return false;
            }
            httpClient.login(username, password, monitor);
            //如果登录成功，则记录用户名和密码
            reviewboardClient.setUsername(username);
            reviewboardClient.setPassword(password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Creates a new diff by parsing an uploaded diff file.
     */
    public void uploadDiff( int reviewRequestId, String baseUrlDir, File fileDiff, File parentDiffFile, IProgressMonitor monitor )throws ReviewboardException,OperationException{
        boolean isSuccess = false;
        String resultContent = null;
        Map<String, String> parameters = new HashMap<String, String>();
        Map<String, File> fileParameters = new HashMap<String, File>();
        
        //Set Params to submit
        if( null != fileDiff && fileDiff.exists() ){
            fileParameters.put("path", fileDiff);
        }
        if( null != parentDiffFile && parentDiffFile.exists() ){
            fileParameters.put("parent_diff_path", parentDiffFile);
        }
        if( null != baseUrlDir && !baseUrlDir.trim().isEmpty()){
            parameters.put("basedir", baseUrlDir );
        }
        
        try {
            String uri = MessageFormat.format(URI_NEW_DIFF, new Object[]{ String.valueOf(reviewRequestId) });
            resultContent = httpClient.executePost( uri, parameters, fileParameters, monitor);
            isSuccess = reviewboardReader.isStatOK(resultContent);
        } catch (Exception e) {
            throw new ReviewboardException(e);
        }finally{
            if( null != fileDiff && fileDiff.exists() ){
                try {
                    fileDiff.delete();
                } catch (Exception e) {
                }
            }
        }
        if(!isSuccess){
            if(!reviewboardReader.getErrorCode(resultContent).equals( UPLOADTOSERVER_FAIL_FILE_CODE )){
                throw new OperationException("Error When Uploading Diff for review_request_id = "+ reviewRequestId 
                        +", error information is:" + reviewboardReader.getErrorMessage(resultContent) 
                        +",the resultContent from the server is:"+resultContent);
            }else{
                DiffUploadErrorBean diffUploadErrorBean = reviewboardReader.getDiffUploadErrorBean(resultContent);
                StringBuilder errorInfoSB = new StringBuilder();
                errorInfoSB.append("review_request_id = "+ reviewRequestId +"的Request在上传diff时，出现错误，错误原因一般是因为下面之一：");
                errorInfoSB.append(System.getProperty("line.separator"));
                errorInfoSB.append("(1)请仔细检查本地的SVN版本和文件，以及上传时指定的Base Directory");
                errorInfoSB.append(System.getProperty("line.separator"));
                errorInfoSB.append("(2)您不具备该目录的SVN权限；或者您在ReviewBoard服务器尚未配置该目录；");
                errorInfoSB.append(System.getProperty("line.separator"));
                errorInfoSB.append("服务器端返回的错误详情：ReviewBoard服务器找不到SVN文件："
                        + diffUploadErrorBean.getFile()+"，revision="+diffUploadErrorBean.getRevision());
                throw new OperationException(errorInfoSB.toString());
            }
        }
    }
    
    public void uploadDiff( int review_request_id, String baseUrlDir, File[] fileDiffs, File parentDiffFile, 
            IProgressMonitor monitor )throws ReviewboardException,OperationException{
        if( null == fileDiffs || fileDiffs.length == 0 ){
            return ;
        }
        //暂无解决方案解决多个不同的文件是不同编码的情况，下面还是将这些文件合并。取最后一个文件的编码
        File diffFileTmp = null;
        try {
            diffFileTmp = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
            StringBuilder diffContentSBTmp = new StringBuilder();
            String charsetName = null;
            for( File fileDiff : fileDiffs ){
                if( null == fileDiff || !fileDiff.exists() ){
                    continue;
                }
                charsetName = GuessStreamEncoding.getFileEncoding(fileDiff);
                String diffContentTmp = IOUtils.getContentFromFile( fileDiff, charsetName );
                diffContentSBTmp.append( diffContentTmp );
                diffContentSBTmp.append( EOL );
                try {
                    fileDiff.delete();
                } catch (Exception e) {
                }
            }
            IOUtils.saveFile(diffFileTmp, diffContentSBTmp.toString(), charsetName);
            this.uploadDiff(review_request_id, baseUrlDir, diffFileTmp, parentDiffFile, monitor);
        } catch (ReviewboardException e) {
            throw new ReviewboardException(e);
        }finally{
            if( null != diffFileTmp && diffFileTmp.exists() ){
                try {
                    diffFileTmp.delete();
                } catch (Exception e) {
                }
            }
        }
    }
    
    public void uploadDiff( int review_request_id, String baseUrlDir, String diffContent,
            String charsetName, IProgressMonitor monitor )throws ReviewboardException,OperationException{
        if( null == diffContent || diffContent.trim().isEmpty()){
            return ;
        }
        File diffFileTmp = null;
        try {
            diffFileTmp = IOUtils.generateTmpFile( FOLDER_TMP_DIFF, DIFF_SUFFIX);
            IOUtils.saveFile(diffFileTmp, diffContent, charsetName);
            this.uploadDiff(review_request_id, baseUrlDir, diffFileTmp, null, monitor);
        } catch (ReviewboardException e) {
            throw new ReviewboardException(e);
        }finally{
            if( null != diffFileTmp && diffFileTmp.exists() ){
                try {
                    diffFileTmp.delete();
                } catch (Exception e) {
                }
            }
        }
    }
    
    public ModelReviewRequest newReviewRequestAll(ModelReviewRequest reviewRequestNew, String baseUrlDir, 
            File[] fileDiffs, IProgressMonitor monitor) throws ReviewboardException,OperationException{
        //先创建一个空白的Request
        ModelReviewRequest reviewRequest = this.newReviewRequest(reviewRequestNew, monitor);
        //然后再修改Request的内容
        this.updateReviewRequestFilterNoValue(reviewRequest, monitor);
        
        //再上传diff文件
        if( null != fileDiffs && fileDiffs.length > 0 ){
            this.uploadDiff(reviewRequest.getId(), baseUrlDir, fileDiffs, null, monitor);
        }
        return reviewRequest;
    }
    
}
