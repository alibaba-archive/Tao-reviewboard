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
package com.taobao.eclipse.plugin.reviewboard.core.constant;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;

/**
 * ��˵��:ReviewBoard����
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class ReviewBoardCoreConstants {
    
    /* ============================================================================ */
    /*  ���ñ���                                                                                                                                                                               */
    /* ============================================================================ */
    
    /** Ĭ���Ƿ�������URL */
    public static final String SERVER_DEFAULT = "";
    
    /**��֧���*/
    public static final String TAG_BRANCHES = "/branches/";
    
    /**���ɱ��*/
    public static final String TAG_TRUNK = "/trunk/";

    public static final String REVIEW_REQUEST_URL = "/r/";
    
    public static final String UPLOADTOSERVER_FAIL_FILE_CODE = "207";
    
    public static final String CHARSET_AUTO = RbCoreMessages.getString("SETTING_ADV_DIFF_ENCODE_AUTO");
    
    public static final String CHARSET_GBK = "GBK";
    
    public static final String CHARSET_UTF_8 = "UTF-8";
    
    public static final String CHARACTER_ENCODING = "UTF-8";
    
    public static final String ENCODING_PUT = "UTF-8";

    public static final String CHARACTER_ENCODING_PREFIX = "UTF-";
    
    public static final String URL_PREFIX_HTTPS = "https://";
    
    public static final String URL_PREFIX_HTTP = "http://";
    
    public static final int BUFFER_SIZE_DEFAULT = 30000;
    
    public static final String EXTENSIONPOINT_CLIENT = "com.taobao.eclipse.plugin.reviewboard.core.clientRepositoryAwair"; //$NON-NLS-1$
    
    /**
     * �೤ʱ�����һ�λ�������ClinentData�����ڶ�ʱ��ʹ�ã�����λ������
     * 
     */
    public static final long RELOAD_MINUTE_CACHE_CLIENTDATA = 20L;
    
    public static final String CHANGE_NUMBER_ENABLE_REPOSITORY = "Perforce";
    
    /* ============================================================================ */
    /*  diff���                                                                                                                                                                              */
    /* ============================================================================ */
    
    /**��ʱ�ļ������֣����û�����ʱĿ¼�£����е���ʱ�ļ�������ڸ����ļ����*/
    public static final String FOLDER_TMP_DIFF = "diffFileTmp";

    /**diff�ļ���׺*/
    public static final String DIFF_SUFFIX = ".diff";
    
    /**����diff����ʱ��ͷ������*/
    public static final String ECLIPSE_PATCH_HEADER = "### Alibabab Eclipse Workspace For ReviewBoard Patch 2.0"; //$NON-NLS-1$
    
    /**����diff����ʱ�õ�*/
    public static final String ECLIPSE_PROJECT_MARKER = "#P "; //$NON-NLS-1$
    
    public static final String EOL = System.getProperty("line.separator");
    
    public static final String SVN_HEAD = "HEAD";

    /**���ַ���*/
    public static final String EMPTY_STRING = "";
    
}
