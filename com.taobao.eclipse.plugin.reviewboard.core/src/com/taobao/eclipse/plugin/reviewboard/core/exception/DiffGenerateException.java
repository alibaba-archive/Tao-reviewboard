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
 * 类说明:Reviewboard生成Diff时的异常
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class DiffGenerateException extends Exception {

    public DiffGenerateException() {
        super();
    }

    public DiffGenerateException(String message, Exception exception) {
        super(message, exception);
    }
    
    public DiffGenerateException( String message ) {
        super(message);
    }

    public DiffGenerateException( Exception exception) {
        super( exception );
    }

}
