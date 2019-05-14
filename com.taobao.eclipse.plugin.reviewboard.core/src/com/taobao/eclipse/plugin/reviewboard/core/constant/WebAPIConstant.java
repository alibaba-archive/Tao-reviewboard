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

/**
 * ��˵��: Web API���õ�ַ
 * 
 * @author ����  ����ʱ�䣺2010-10-18
 */
public class WebAPIConstant {

    public static final String URI_LOGIN = "/api/users";

    public static final String URI_USERS = "/api/users/?start={0}&max-results={1}";

    public static final String URI_GROUPS = "/api/groups/?start={0}&max-results={1}";

    public static final String URI_REPOSITORIES = "/api/repositories/?start={0}&max-results={1}";

    public static final String URI_PUBLISH_REQUEST = "/api/review-requests/{0}/draft/";

    public static final String URI_NEW_REQUESTS = "/api/review-requests/";

    public static final String URI_REQUESTS = "/api/review-requests/";

    public static final String URI_DRAFT_BY_REQUESTID = "/api/review-requests/{0}/draft/";

    public static final String URI_REQUEST_BY_REQUESTID = "/api/review-requests/{0}/reviews/";

    public static final String URI_UPDATE_REQUEST_DRAFT = "/api/review-requests/{0}/draft/";

    public static final String URI_NEW_DIFF = "/api/review-requests/{0}/diffs/";

}
