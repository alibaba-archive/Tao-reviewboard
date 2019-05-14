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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.WebAPIConstant.URI_LOGIN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.protocol.Protocol;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;
import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.core.util.IOUtils;
import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.ENCODING_PUT;

/**
 * HTTP Client for calling the Review Board API. Handles {@link HttpClient} setup,
 * authentication and GET/POST requests.
 *
 * @author Markus Knittig
 * 
 */
public class ReviewboardHttpClient {
    
    private String serverUrl = "";
    
    private String username = "";
    
    private String password = "";

    private final HttpClient httpClient;
    
    private final boolean TEST_MODE;

    private String cookie = "";
    
    private final int CONNNECT_TIMEOUT = 60000;
    private final int SOCKET_TIMEOUT = 180000;
    private final long CONNECTION_TIMEOUT_INTERVAL = 30000L;

    public ReviewboardHttpClient( String serverUrl, 
            String username, String password, String characterEncoding,boolean selfSignedSSL) {
        
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        
        String application = System.getProperty("eclipse.application", "");
        if(application.length() > 0){
            TEST_MODE = application.endsWith("testapplication");
        } else {
            String commands = System.getProperty("eclipse.commands", "");
            TEST_MODE = commands.contains("testapplication\n");
        }
        
        this.httpClient = createAndInitHttpClient(characterEncoding, selfSignedSSL);
        
    }

    private HttpClient createAndInitHttpClient(String characterEncoding, boolean selfSignedSSL) {
        
        if (selfSignedSSL) {
            Protocol.registerProtocol("https",
                    new Protocol("https", new EasySSLProtocolSocketFactory(), 443));
        }
        
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setBooleanParameter("http.protocol.allow-circular-redirects", true);
        httpClient.getParams().setConnectionManagerTimeout(CONNECTION_TIMEOUT_INTERVAL);
        httpClient.getParams().setContentCharset(characterEncoding);
        httpClient.getHttpConnectionManager().getParams().setSoTimeout(SOCKET_TIMEOUT);
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(CONNNECT_TIMEOUT);
        
        if(TEST_MODE){
            httpClient.getHttpConnectionManager().getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 2);
        } 
        else{
            httpClient.getHttpConnectionManager().getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, 100);
            httpClient.getHttpConnectionManager().getParams().setMaxTotalConnections(1000);
        }
        
        return httpClient;
    }

    public void login(String username, String password, 
    		IProgressMonitor monitor) throws ReviewboardException {
        
        this.cookie = "";
        
        if( null == username || username.trim().isEmpty() || null == password || password.trim().isEmpty()  ){
            throw new ReviewboardException("username and password is required.");
        }
       
        RestfulReviewboardReader reviewboardReader = new RestfulReviewboardReader();
        
        HttpMethodBase loginRequest = null;
        loginRequest = new GetMethod(serverUrl + URI_LOGIN);
        String authorizationKey   = "Authorization";
        String authorizationValue = "Basic " + ReviewboardUtil.convertStr2BASE64(username + ":" + password);
        //Set a HTTP Headers    WWW-Authenticate
        loginRequest.setRequestHeader(authorizationKey, authorizationValue );
        
        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        
        try {
            monitor.beginTask("Authorization checking...", IProgressMonitor.UNKNOWN);
            this.httpClient.getState().clear();
            
            if (executeRequest(loginRequest, monitor) == HttpStatus.SC_OK) {
                String response = getResponseBodyAsString(loginRequest, monitor);
                if (reviewboardReader.isStatOK(response)) {
                    Header header = loginRequest.getResponseHeader("Set-Cookie");
                    if(null != header){
                        cookie = header.getValue();
                    }
                } else {
                    throw new ReviewboardException(reviewboardReader.getErrorMessage(response));
                }
            } else {
                throw new ReviewboardException(RbCoreMessages.getString("ERROR_SERVER_NOT_CONFIFIGURED_0"));
            }
        } finally {
            loginRequest.releaseConnection();
            monitor.done();
        }
    }

    private String getCookie(IProgressMonitor monitor) throws ReviewboardException {
        if (cookie.equals("")) {
            login(username, password, monitor);
        }
        return cookie;
    }

    private String stripSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.lastIndexOf("/"));
        }
        return url;
    }

    public String executeGet(String url, IProgressMonitor monitor) throws ReviewboardException {
        String integratedUrl = stripSlash(serverUrl) + url;
        GetMethod getRequest = new GetMethod(integratedUrl);
        getRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));

        return executeMethod(getRequest, monitor);
    }

    public String executePost(String url, IProgressMonitor monitor)  throws ReviewboardException {
        return executePost(url, new HashMap<String, String>(), monitor);
    }

    /**
     * 提交普通文本参数
     * @param url
     * @param parameters
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    public String executePost(String url, Map<String, String> parameters,
            IProgressMonitor monitor)  throws ReviewboardException {
        PostMethod postRequest = new PostMethod(stripSlash(serverUrl) + url);
        postRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));

        for (String key : parameters.keySet()) {
            postRequest.setParameter(key, parameters.get(key));
        }

        return executeMethod(postRequest, monitor);
    }

    /**
     * Submit request with PUT method.
     * @param url
     * @param parameters
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    public String executePut(String url, Map<String, String> parameters, Map<String, File> fileParameters,
            IProgressMonitor monitor)  throws ReviewboardException {
        PutMethod putRequest = new PutMethod(stripSlash(serverUrl) + url);
        putRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));
        
        if( null == fileParameters || fileParameters.isEmpty() ){
            return this.executePut(url, parameters, monitor);
        }
        
        //设置param    
        Part[] params = null;
        if( null == parameters ){
            params = new Part[fileParameters.size()]; 
        }
        else{
            params = new Part[parameters.size() + fileParameters.size()]; 
            int index = 0;
            for(String parametersKey : parameters.keySet()){
                StringPart stringPart = new StringPart(parametersKey, parameters.get(parametersKey), ENCODING_PUT);  
                params[index++] = stringPart;   
            }
        }
        int index = 0;
        try {
            for(String fileKey : fileParameters.keySet()){
                FilePart filePart = new FilePart(fileKey, fileParameters.get(fileKey));   
                params[ (null == parameters ? 0 : parameters.size()) + index++ ] = filePart;   
            }
        } catch (Exception e) {
            throw new ReviewboardException(e);
        }
        
        MultipartRequestEntity putRequestEntity = new MultipartRequestEntity(params, putRequest.getParams());   

        putRequest.setRequestEntity(putRequestEntity);  

        return executeMethod(putRequest, monitor);
    }

    /**
     * Submit request with PUT method.
     * @param url
     * @param parameters
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    public String executePut(String url, Map<String, String> parameters,
            IProgressMonitor monitor)  throws ReviewboardException {
        PutMethod putRequest = new PutMethod(stripSlash(serverUrl) + url);
        putRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));
        
        //设置param    
        Part[] params = null;
        params = new Part[parameters.size()]; 
        int index = 0;
        for(String parametersKey : parameters.keySet()){
            StringPart stringPart = new StringPart(parametersKey, parameters.get(parametersKey), ENCODING_PUT);  
            params[index++] = stringPart;   
        }
        
        MultipartRequestEntity putRequestEntity = new MultipartRequestEntity(params, putRequest.getParams());   

        putRequest.setRequestEntity(putRequestEntity);  

        return executeMethod(putRequest, monitor);
    }
    
    /**
     * 上传普通文本参数和文件参数。
     * @param url
     * @param parameters
     * @param fileParameters
     * @param monitor
     * @return
     * @throws ReviewboardException
     */
    public String executePost(String url, Map<String, String> parameters, Map<String, File> fileParameters,
            IProgressMonitor monitor)  throws ReviewboardException {
        PostMethod postRequest = new PostMethod(stripSlash(serverUrl) + url);
        postRequest.getParams().setParameter("Set-Cookie", getCookie(monitor));

        if( null == fileParameters || fileParameters.isEmpty() ){
            return this.executePost(url, parameters, monitor);
        }
        
        //设置param    
        Part[] params = null;
        if( null == parameters ){
            params = new Part[fileParameters.size()]; 
        }
        else{
            params = new Part[parameters.size() + fileParameters.size()]; 
            int index = 0;
            for(String parametersKey : parameters.keySet()){
                StringPart stringPart = new StringPart(parametersKey,parameters.get(parametersKey));  
                params[index++] = stringPart;   
            }
        }
        int index = 0;
        try {
            for(String fileKey : fileParameters.keySet()){
                FilePart filePart = new FilePart(fileKey, fileParameters.get(fileKey));   
                params[ (null == parameters ? 0 : parameters.size()) + index++ ] = filePart;   
            }
        } catch (Exception e) {
            throw new ReviewboardException(e);
        }
        
        MultipartRequestEntity post = new MultipartRequestEntity(params, postRequest.getParams());   

        postRequest.setRequestEntity(post);  

        return executeMethod(postRequest, monitor);
    }

    private String executeMethod(HttpMethodBase request, IProgressMonitor monitor) throws ReviewboardException {

        if(monitor == null){
            monitor = new NullProgressMonitor();
        }
        
        try {
            monitor.beginTask("Executing request", IProgressMonitor.UNKNOWN);
            int resultCode = executeRequest(request, monitor);
            if (resultCode == HttpStatus.SC_OK || resultCode == HttpStatus.SC_CREATED ) {  
                return getResponseBodyAsString(request, monitor);
            }else {   
                String error = "HttpClient excuteMethod Error HttpCode=" + resultCode;    
                throw new ReviewboardException(error);   
            }  
        } finally {
            request.releaseConnection();
            monitor.done();
        }
        
    }

    private int executeRequest(HttpMethodBase request, IProgressMonitor monitor) {
        try {
            return httpClient.executeMethod(request); 
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getResponseBodyAsString(HttpMethodBase request, IProgressMonitor monitor) {
        try {
            InputStream is = request.getResponseBodyAsStream(); 
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
