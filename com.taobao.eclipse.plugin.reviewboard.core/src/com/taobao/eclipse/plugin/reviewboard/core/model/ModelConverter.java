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
package com.taobao.eclipse.plugin.reviewboard.core.model;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * 转换JsonObject为model的接口
 *
 * @author 智清
 * 创建时间：2010-08-03
 */
public interface ModelConverter extends Serializable {

    /**
     * 将jsonObject转换为model
     * @param jsonObject
     */
    void convertToModel(JSONObject jsonObject);

}
