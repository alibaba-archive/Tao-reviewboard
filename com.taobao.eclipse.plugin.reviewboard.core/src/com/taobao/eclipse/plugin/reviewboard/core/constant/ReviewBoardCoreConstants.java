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
package com.taobao.eclipse.plugin.reviewboard.core.constant;

import com.taobao.eclipse.plugin.reviewboard.core.RbCoreMessages;

/**
 * 类说明:ReviewBoard常量
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ReviewBoardCoreConstants {
    
    /* ============================================================================ */
    /*  公用变量                                                                                                                                                                               */
    /* ============================================================================ */
    
    /** 默认是服务器的URL */
    public static final String SERVER_DEFAULT = "";
    
    /**分支标记*/
    public static final String TAG_BRANCHES = "/branches/";
    
    /**主干标记*/
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
     * 多长时间更新一次缓存数据ClinentData（用于定时器使用），单位：分钟
     * 
     */
    public static final long RELOAD_MINUTE_CACHE_CLIENTDATA = 20L;
    
    public static final String CHANGE_NUMBER_ENABLE_REPOSITORY = "Perforce";
    
    /* ============================================================================ */
    /*  diff相关                                                                                                                                                                              */
    /* ============================================================================ */
    
    /**临时文件夹名字（在用户的临时目录下，所有的临时文件都存放在该子文件夹里）*/
    public static final String FOLDER_TMP_DIFF = "diffFileTmp";

    /**diff文件后缀*/
    public static final String DIFF_SUFFIX = ".diff";
    
    /**生成diff内容时的头部描述*/
    public static final String ECLIPSE_PATCH_HEADER = "### Alibabab Eclipse Workspace For ReviewBoard Patch 2.0"; //$NON-NLS-1$
    
    /**生成diff内容时用到*/
    public static final String ECLIPSE_PROJECT_MARKER = "#P "; //$NON-NLS-1$
    
    public static final String EOL = System.getProperty("line.separator");
    
    public static final String SVN_HEAD = "HEAD";

    /**空字符串*/
    public static final String EMPTY_STRING = "";
    
}
