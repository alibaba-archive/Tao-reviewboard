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
package com.taobao.eclipse.plugin.reviewboard.core.model;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * ת��JsonObjectΪmodel�Ľӿ�
 *
 * @author ����
 * ����ʱ�䣺2010-08-03
 */
public interface ModelConverter extends Serializable {

    /**
     * ��jsonObjectת��Ϊmodel
     * @param jsonObject
     */
    void convertToModel(JSONObject jsonObject);

}
