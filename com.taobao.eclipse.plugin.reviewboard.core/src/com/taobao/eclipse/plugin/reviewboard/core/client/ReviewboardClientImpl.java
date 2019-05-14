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
package com.taobao.eclipse.plugin.reviewboard.core.client;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARACTER_ENCODING;

import com.taobao.eclipse.plugin.reviewboard.core.service.ReviewboardHttpClient;

/**
 * RESTful implementation of {@link IReviewboardClient}.
 * 
 * @author Markus Knittig
 */
public class ReviewboardClientImpl implements IReviewboardClient {
    
    private String serverUrl = "";
    
    private String username = "";
    
    private String password = "";
    
    private String characterEncoding = "";

    private ReviewboardClientData clientData;

    private ReviewboardHttpClient httpClient;
    
    public ReviewboardClientImpl( ReviewboardClientData clientData, String serverUrl, 
               String username, String password, String characterEncoding) {
        this.clientData = clientData;
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        if( null == characterEncoding || characterEncoding.trim().isEmpty() ){
            characterEncoding = CHARACTER_ENCODING;
        }
        this.setCharacterEncoding(characterEncoding);
        boolean isSSL = false;

        httpClient = new ReviewboardHttpClient( serverUrl, username, password, characterEncoding, isSSL );
    }

    public ReviewboardClientData getClientData() {
        return clientData;
    }
    
    public String[] getLocationUrlAndUserNameAndPassword(){
        return new String[]{ serverUrl, username, password };
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public ReviewboardHttpClient getHttpClient() {
        return httpClient;
    }

    public boolean hasRepositoryData() {
        return (clientData.lastupdate != 0);
    }

}
