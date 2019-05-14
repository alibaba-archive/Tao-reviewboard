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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taobao.eclipse.plugin.reviewboard.core.model.ModelConverter;

/**
 * 解析json util
 *
 * @author 智清
 * 创建时间：2010-08-03
 */
public class JsonUtil {

    public static String getStringFromJsonObject( JSONObject jsonObject, String key ) {
        try {
			return jsonObject.getString(key);
		} catch (JSONException e) {
			return EMPTY_STRING;
		}
    }

    public static boolean getBooleanFromJsonObject( JSONObject jsonObject, String key ) {
        try {
			return jsonObject.getBoolean(key);
		} catch (JSONException e) {
			return false;
		}
    }
    
    public static Integer getIntFromJsonObject( JSONObject jsonObject, String key ) throws JSONException{
        try {
            return jsonObject.getInt(key);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static long getLongFromJsonObject( JSONObject jsonObject, String key ) throws JSONException{
        try {
            return jsonObject.getLong(key);
        } catch (Exception e) {
            return 0l;
        }
    }
    
    public static <T extends ModelConverter> T parseModel(Class<T> modelClass, JSONObject jsonObject) {
        if( null == jsonObject ){
            return null;
        }
        T modelClassInstance = null;
        try {
            modelClassInstance = modelClass.newInstance();
        } catch (Exception e) {
        }
        if( null != modelClassInstance ){
            modelClassInstance.convertToModel(jsonObject);
        }
        return modelClassInstance;
    }

    public static <T extends ModelConverter> List<T> parseModelList(Class<T> entityClass, JSONArray jsonArray) {
        List<T> modelList = new ArrayList<T>();
        if( null == jsonArray ){
            return modelList;
        }
        try {
            int listSize = jsonArray.length();
            for (int index = 0; index < listSize; index++) {
                try {
                    T model = parseModel(entityClass, jsonArray.getJSONObject(index));
                    if( null != model ){
                        modelList.add(model);
                     }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return modelList;
    }
    
}
