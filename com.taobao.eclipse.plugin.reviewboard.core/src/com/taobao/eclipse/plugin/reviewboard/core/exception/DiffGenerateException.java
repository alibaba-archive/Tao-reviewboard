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
package com.taobao.eclipse.plugin.reviewboard.core.exception;

/**
 * ��˵��:Reviewboard����Diffʱ���쳣
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
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
