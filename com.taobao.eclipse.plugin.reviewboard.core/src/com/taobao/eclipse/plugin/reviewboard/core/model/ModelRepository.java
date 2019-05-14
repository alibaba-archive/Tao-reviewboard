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
 * 类说明:repositories
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ModelRepository implements ModelConverter, Serializable {
    
    private static final long serialVersionUID = -229303920234573007L;
    
    private int id;
    
    private String name;
    
    private String tool;
    
    private String path;
    
    public void convertToModel(JSONObject jsonObject) {
        try {
            id  = JsonUtil.getIntFromJsonObject(jsonObject, "id") ;
            name = JsonUtil.getStringFromJsonObject(jsonObject, "name");
            tool = JsonUtil.getStringFromJsonObject(jsonObject, "tool");
            path = JsonUtil.getStringFromJsonObject(jsonObject, "path");
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

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((tool == null) ? 0 : tool.hashCode());
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
        ModelRepository other = (ModelRepository) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        if (tool == null) {
            if (other.tool != null)
                return false;
        } else if (!tool.equals(other.tool))
            return false;
        return true;
    }
}
