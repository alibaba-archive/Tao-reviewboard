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
package com.taobao.eclipse.plugin.reviewboard.core.util;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.URL_PREFIX_HTTP;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.URL_PREFIX_HTTPS;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.taobao.eclipse.plugin.reviewboard.core.model.ModelRepository;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewGroup;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelUser;

/**
 *
 * @author Markus Knittig
 * @author 智清 
 */
public final class ReviewboardUtil {

    public static <T> String joinList(List<T> list) {
        StringBuilder result = new StringBuilder();

        String delimiter = "";
        for (T item : list) {
            result.append(delimiter);
            if( item instanceof ModelUser ){
                ModelUser modelTmp = (ModelUser)item;
                result.append(modelTmp.getUsername().toString().trim());
            }else if( item instanceof ModelRepository ){
                ModelRepository modelTmp = (ModelRepository)item;
                result.append(modelTmp.getName().toString().trim());
            }else if( item instanceof ModelReviewGroup ){
                ModelReviewGroup modelTmp = (ModelReviewGroup)item;
                result.append(modelTmp.getName().toString().trim());
            }else{
                result.append(item.toString().trim());
            }
            delimiter = ",";
        }

        return result.toString();
    }
    
    /**
     * Convert string to BASE64  encoding 
     * @param   normalStr
     * @return  string encoded with BASE64
     */
    public static String convertStr2BASE64(String normalStr) { 
        if (normalStr == null) 
        return null; 
        
        return (new sun.misc.BASE64Encoder() ).encode( normalStr.getBytes() ); 
    }
    
    /**
     * 标准化URL格式，从而使得用户的输入可以多样化，对使用者更加友好
     * @param url
     * @return
     */
    public static String norminateRepositoryUrl(String url) {
        if( null == url || url.trim().equals("") ) return "";
        url = url.trim();
        if (url.startsWith(URL_PREFIX_HTTPS) || url.startsWith(URL_PREFIX_HTTP)) {
            ;
        }else{
            url = URL_PREFIX_HTTP + url;
        }
        if( url.endsWith("/") ){
            url = url.substring(0, url.length()-1);
        }
        return url;
    }
    
    /**
     * 检查URL是否合法
     * @param url
     * @return
     */
    public static boolean isValidUrl(String url) {
        url = ReviewboardUtil.norminateRepositoryUrl(url);
        if ((url.startsWith(URL_PREFIX_HTTPS) || url.startsWith(URL_PREFIX_HTTP))
                && !url.endsWith("/")) {
            try {
                new URL(url);
                return true;
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        return false;
    }
    

}
