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
package com.taobao.eclipse.plugin.reviewboard.core.exception;

/**
 * 类说明:Reviewboard应用级异常。例如，请求上传diff，而服务器端返回错误
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class OperationException extends Exception {

    public OperationException() {
        super();
    }

    public OperationException(String message, Exception exception) {
        super(message, exception);
    }
    
    public OperationException( String message ) {
        super(message);
    }

    public OperationException( Exception exception) {
        super( exception );
    }

}
