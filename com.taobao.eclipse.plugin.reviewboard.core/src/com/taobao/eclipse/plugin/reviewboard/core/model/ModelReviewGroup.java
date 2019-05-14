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

import com.taobao.eclipse.plugin.reviewboard.core.util.JsonUtil;

/**
 * 类说明:ReviewGroup
 *
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ModelReviewGroup implements ModelConverter, Serializable {
    
    private static final long serialVersionUID = -2908759207891523007L;

    private int id;
    
    private String name;
    
    private String displayName;
    
    private String url;
    
    private String mailingList;
    
    public ModelReviewGroup() {
    }

    public ModelReviewGroup(String name) {
        this.name = name;
    }
    
    public void convertToModel(JSONObject jsonObject) {
        try {
            id  = JsonUtil.getIntFromJsonObject(jsonObject, "id") ;
            url = JsonUtil.getStringFromJsonObject(jsonObject, "url");
            name = JsonUtil.getStringFromJsonObject(jsonObject, "name");
            displayName = JsonUtil.getStringFromJsonObject(jsonObject, "display_name");
            mailingList = JsonUtil.getStringFromJsonObject(jsonObject, "mailing_list");
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMailingList() {
        return mailingList;
    }

    public void setMailingList(String mailingList) {
        this.mailingList = mailingList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + id;
        result = prime * result + ((mailingList == null) ? 0 : mailingList.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
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
        ModelReviewGroup other = (ModelReviewGroup) obj;
        if (displayName == null) {
            if (other.displayName != null)
                return false;
        } else if (!displayName.equals(other.displayName))
            return false;
        if (id != other.id)
            return false;
        if (mailingList == null) {
            if (other.mailingList != null)
                return false;
        } else if (!mailingList.equals(other.mailingList))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

}
