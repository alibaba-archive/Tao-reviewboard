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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.taobao.eclipse.plugin.reviewboard.core.util.JsonUtil;

/**
 * 类说明:Review
 *
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ModelReview implements ModelConverter, Serializable {
    
    private static final long serialVersionUID = -2293039207891523007L;

    private int id;
    
    private String bodyTop;
    
    private String bodyBottom;
    
    private int shipIt;
    
    private Date timestamp;
    
    private ModelUser user;
    
    private boolean isPublic;

    public void convertToModel(JSONObject jsonObject) {
        try {
            id  = JsonUtil.getIntFromJsonObject(jsonObject, "id") ;
            bodyTop = JsonUtil.getStringFromJsonObject(jsonObject, "body_top");
            bodyBottom = JsonUtil.getStringFromJsonObject(jsonObject, "body_bottom");
            shipIt = JsonUtil.getIntFromJsonObject(jsonObject, "ship_it");
            String timestampTmp = JsonUtil.getStringFromJsonObject(jsonObject, "timestamp");
            if( null != timestampTmp && !timestampTmp.trim().isEmpty() ){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timestamp = dateFormat.parse( timestampTmp );
            }
            isPublic = JsonUtil.getBooleanFromJsonObject(jsonObject, "public");
            try {
                JSONObject userJsonObject = jsonObject.getJSONObject("user");
                if( null != userJsonObject ){
                    user = JsonUtil.parseModel(ModelUser.class, userJsonObject);
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBodyTop() {
        return bodyTop;
    }

    public void setBodyTop(String bodyTop) {
        this.bodyTop = bodyTop;
    }

    public String getBodyBottom() {
        return bodyBottom;
    }

    public void setBodyBottom(String bodyBottom) {
        this.bodyBottom = bodyBottom;
    }

    public int getShipIt() {
        return shipIt;
    }

    public void setShipIt(int shipIt) {
        this.shipIt = shipIt;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public ModelUser getUser() {
        return user;
    }

    public void setUser(ModelUser user) {
        this.user = user;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bodyBottom == null) ? 0 : bodyBottom.hashCode());
        result = prime * result + ((bodyTop == null) ? 0 : bodyTop.hashCode());
        result = prime * result + id;
        result = prime * result + (isPublic ? 1231 : 1237);
        result = prime * result + shipIt;
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelReview other = (ModelReview) obj;
        if (bodyBottom == null) {
            if (other.bodyBottom != null)
                return false;
        } else if (!bodyBottom.equals(other.bodyBottom))
            return false;
        if (bodyTop == null) {
            if (other.bodyTop != null)
                return false;
        } else if (!bodyTop.equals(other.bodyTop))
            return false;
        if (id != other.id)
            return false;
        if (isPublic != other.isPublic)
            return false;
        if (shipIt != other.shipIt)
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

}
