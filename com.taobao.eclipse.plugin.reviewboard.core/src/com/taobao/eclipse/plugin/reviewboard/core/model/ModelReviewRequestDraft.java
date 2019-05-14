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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.taobao.eclipse.plugin.reviewboard.core.util.JsonUtil;

/**
 * 类说明:ReviewRequestDraft
 *
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ModelReviewRequestDraft implements ModelConverter, Serializable {
    
    private static final long serialVersionUID = -2293865467891523007L;
    
    private int id;

    private String branch = "";
    
    private String description = "";
    
    private String summary = "";
    
    private String testingDone = "";
    
    private Date lastUpdated;
    
    private List<Integer> bugsClosedList = new ArrayList<Integer>();
    
    private List<ModelReviewGroup> targetGroupList = new ArrayList<ModelReviewGroup>();
    
    private List<ModelUser> targetPeopleList = new ArrayList<ModelUser>();
    
    public void convertToModel(JSONObject jsonObject) {
        try {
            id  = JsonUtil.getIntFromJsonObject(jsonObject, "id") ;
            branch = JsonUtil.getStringFromJsonObject(jsonObject, "branch");
            description = JsonUtil.getStringFromJsonObject(jsonObject, "description");
            summary = JsonUtil.getStringFromJsonObject(jsonObject, "summary");
            testingDone = JsonUtil.getStringFromJsonObject(jsonObject, "testing_done");
            branch = JsonUtil.getStringFromJsonObject(jsonObject, "branch");
            String lastUpdatedTmp = JsonUtil.getStringFromJsonObject(jsonObject, "last_updated");
            if( null != lastUpdatedTmp && !lastUpdatedTmp.trim().isEmpty() ){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                lastUpdated = dateFormat.parse( lastUpdatedTmp );
            }
            
            try {
                JSONArray bugsClosedJsonArray = jsonObject.getJSONArray("bugs_closed");
                if( null != bugsClosedJsonArray ){
                    bugsClosedList.clear();
                    int listSize = bugsClosedJsonArray.length();
                    try {
                        for (int index = 0; index < listSize; index++) {
                            String bugsClosedString = bugsClosedJsonArray.getString(index);
                            bugsClosedList.add(Integer.parseInt(bugsClosedString));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
            }
            
            try {
                JSONArray targetGroupJsonArray = jsonObject.getJSONArray("target_groups");
                if( null != targetGroupJsonArray ){
                    for(int i = 0; i< targetGroupJsonArray.length(); i++){
                        JSONObject targetGroupJsonObject = targetGroupJsonArray.getJSONObject(i);
                        
                        if(null != targetGroupJsonObject){
                            String titleObject = JsonUtil.getStringFromJsonObject(targetGroupJsonObject, "title");
                            ModelReviewGroup modelReviewGroup = new ModelReviewGroup(titleObject );
                            targetGroupList.add(modelReviewGroup);
                        }
                   }
                }
            } catch (Exception e) {
            }
            
            try {
                JSONArray targetPeopleJsonArray = jsonObject.getJSONArray("target_people");
                for(int i = 0; i< targetPeopleJsonArray.length(); i++){
                    JSONObject targetPeopleJsonObject = targetPeopleJsonArray.getJSONObject(i);
                    if(null != targetPeopleJsonObject){
                        String titleObject = JsonUtil.getStringFromJsonObject(targetPeopleJsonObject, "title");
                        ModelUser modelUser  = new ModelUser();
                        modelUser.setUsername(titleObject);
                        targetPeopleList.add(modelUser);
                    }
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTestingDone() {
        return testingDone;
    }

    public void setTestingDone(String testingDone) {
        this.testingDone = testingDone;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<Integer> getBugsClosedList() {
        return bugsClosedList;
    }

    public void setBugsClosedList(List<Integer> bugsClosedList) {
        this.bugsClosedList = bugsClosedList;
    }

    public List<ModelReviewGroup> getTargetGroupList() {
        return targetGroupList;
    }

    public void setTargetGroupList(List<ModelReviewGroup> targetGroupList) {
        this.targetGroupList = targetGroupList;
    }

    public List<ModelUser> getTargetPeopleList() {
        return targetPeopleList;
    }

    public void setTargetPeopleList(List<ModelUser> targetPeopleList) {
        this.targetPeopleList = targetPeopleList;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((branch == null) ? 0 : branch.hashCode());
        result = prime * result + ((bugsClosedList == null) ? 0 : bugsClosedList.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + id;
        result = prime * result + ((lastUpdated == null) ? 0 : lastUpdated.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
        result = prime * result + ((targetGroupList == null) ? 0 : targetGroupList.hashCode());
        result = prime * result + ((targetPeopleList == null) ? 0 : targetPeopleList.hashCode());
        result = prime * result + ((testingDone == null) ? 0 : testingDone.hashCode());
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
        ModelReviewRequestDraft other = (ModelReviewRequestDraft) obj;
        if (branch == null) {
            if (other.branch != null)
                return false;
        } else if (!branch.equals(other.branch))
            return false;
        if (bugsClosedList == null) {
            if (other.bugsClosedList != null)
                return false;
        } else if (!bugsClosedList.equals(other.bugsClosedList))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id != other.id)
            return false;
        if (lastUpdated == null) {
            if (other.lastUpdated != null)
                return false;
        } else if (!lastUpdated.equals(other.lastUpdated))
            return false;
        if (summary == null) {
            if (other.summary != null)
                return false;
        } else if (!summary.equals(other.summary))
            return false;
        if (targetGroupList == null) {
            if (other.targetGroupList != null)
                return false;
        } else if (!targetGroupList.equals(other.targetGroupList))
            return false;
        if (targetPeopleList == null) {
            if (other.targetPeopleList != null)
                return false;
        } else if (!targetPeopleList.equals(other.targetPeopleList))
            return false;
        if (testingDone == null) {
            if (other.testingDone != null)
                return false;
        } else if (!testingDone.equals(other.testingDone))
            return false;
        return true;
    }

}
