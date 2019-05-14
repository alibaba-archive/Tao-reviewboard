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
 * 类说明:ReviewRequest
 *
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ModelReviewRequest implements ModelConverter, Serializable {
    
    private static final long serialVersionUID = -229579075634523007L;

    private int id;
    
    private boolean isPublicReviewRequest;
    
    private String branch = "";
    
    private Integer changeNumber;
    
    private String summary = "";
    
    private String description = "";
    
    private String testingDone = "";
    
    private Date timeAdded;
    
    private Date lastUpdated;
    
    private ReviewRequestStatus status = ReviewRequestStatus.PENDING;
    
    private ModelUser submitter;
    
    private ModelRepository repository;
    
    private List<Integer> bugsClosedList = new ArrayList<Integer>();
    
    private List<ModelReviewGroup> targetGroupList = new ArrayList<ModelReviewGroup>();
    
    private List<ModelUser> targetPeopleList = new ArrayList<ModelUser>();
    
    private String changeDescription;

    public void convertToModel(JSONObject jsonObject) {
        
        try {
            id  = JsonUtil.getIntFromJsonObject(jsonObject, "id") ;
            isPublicReviewRequest = JsonUtil.getBooleanFromJsonObject(jsonObject, "public");
            try {
                String statusStr = JsonUtil.getStringFromJsonObject(jsonObject, "status");
                if( null != statusStr && !statusStr.isEmpty() ){
                    status = ReviewRequestStatus.parseStatus(statusStr);
                }
            } catch (Exception e) {
            }
            summary = JsonUtil.getStringFromJsonObject(jsonObject, "summary");
            description = JsonUtil.getStringFromJsonObject(jsonObject, "description");
            testingDone = JsonUtil.getStringFromJsonObject(jsonObject, "testing_done");
            branch = JsonUtil.getStringFromJsonObject(jsonObject, "branch");
            changeNumber = JsonUtil.getIntFromJsonObject(jsonObject, "changenum");
            
            String timeAddedTmp = JsonUtil.getStringFromJsonObject(jsonObject, "time_added");
            if( null != timeAddedTmp && !timeAddedTmp.trim().isEmpty() ){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeAdded = dateFormat.parse( timeAddedTmp );
            }
            
            String lastUpdatedTmp = JsonUtil.getStringFromJsonObject(jsonObject, "last_updated");
            if( null != lastUpdatedTmp && !lastUpdatedTmp.trim().isEmpty() ){
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                lastUpdated = dateFormat.parse( lastUpdatedTmp );
            }
            
            try {
                JSONObject linksJsonObject = jsonObject.getJSONObject("links");

                if(null != linksJsonObject){
                    try {
                        JSONObject submitterJsonObject = linksJsonObject.getJSONObject("submitter");
                        if( null != submitterJsonObject ){
                            String titleObject = JsonUtil.getStringFromJsonObject(submitterJsonObject, "title");
                            if(null != titleObject){
                                submitter = new ModelUser();
                                submitter.setUsername(titleObject);
                            }else{
                                submitter = JsonUtil.parseModel(ModelUser.class, submitterJsonObject);
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        JSONObject repositoryJsonObject = linksJsonObject.getJSONObject("repository");
                        if( null != repositoryJsonObject ){
                            String titleObject = JsonUtil.getStringFromJsonObject(repositoryJsonObject, "title");
                            if(null != titleObject){
                                repository = new ModelRepository();
                                repository.setName(titleObject);
                            }else{
                                repository = JsonUtil.parseModel(ModelRepository.class, repositoryJsonObject);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e1) {
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
                if( null != targetPeopleJsonArray ){
                    for(int i = 0; i< targetPeopleJsonArray.length(); i++){
                        JSONObject targetPeopleJsonObject = targetPeopleJsonArray.getJSONObject(i);
                        if(null != targetPeopleJsonObject){
                            String titleObject = JsonUtil.getStringFromJsonObject(targetPeopleJsonObject, "title");
                            ModelUser modelUser  = new ModelUser();
                            modelUser.setUsername(titleObject);
                            targetPeopleList.add(modelUser);
                        }
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

    public ModelUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(ModelUser submitter) {
        this.submitter = submitter;
    }

    public Date getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Date timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public ReviewRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewRequestStatus status) {
        this.status = status;
    }
    
    public Integer getChangeNumber() {
        return changeNumber;
    }

    public String getChangeNumberText() {
        if (changeNumber == null) {
            return "None";
        }
        return String.valueOf(changeNumber);
    }

    public void setChangeNumber(Integer changeNumber) {
        this.changeNumber = changeNumber;
    }

    public ModelRepository getRepository() {
        return repository;
    }

    public void setRepository(ModelRepository repository) {
        this.repository = repository;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTestingDone() {
        return testingDone;
    }

    public void setTestingDone(String testingDone) {
        this.testingDone = testingDone;
    }

    public List<Integer> getBugsClosedList() {
        return bugsClosedList;
    }

    public void setBugsClosedList(List<Integer> bugsClosedList) {
        this.bugsClosedList = bugsClosedList;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isPublicReviewRequest() {
        return isPublicReviewRequest;
    }

    public void setPublicReviewRequest(boolean isPublicReviewRequest) {
        this.isPublicReviewRequest = isPublicReviewRequest;
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

    public String getChangeDescription() {
        return changeDescription;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((branch == null) ? 0 : branch.hashCode());
        result = prime * result + ((bugsClosedList == null) ? 0 : bugsClosedList.hashCode());
        result = prime * result + ((changeDescription == null) ? 0 : changeDescription.hashCode());
        result = prime * result + ((changeNumber == null) ? 0 : changeNumber.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + id;
        result = prime * result + (isPublicReviewRequest ? 1231 : 1237);
        result = prime * result + ((lastUpdated == null) ? 0 : lastUpdated.hashCode());
        result = prime * result + ((repository == null) ? 0 : repository.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((submitter == null) ? 0 : submitter.hashCode());
        result = prime * result + ((summary == null) ? 0 : summary.hashCode());
        result = prime * result + ((targetGroupList == null) ? 0 : targetGroupList.hashCode());
        result = prime * result + ((targetPeopleList == null) ? 0 : targetPeopleList.hashCode());
        result = prime * result + ((testingDone == null) ? 0 : testingDone.hashCode());
        result = prime * result + ((timeAdded == null) ? 0 : timeAdded.hashCode());
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
        ModelReviewRequest other = (ModelReviewRequest) obj;
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
        if (changeDescription == null) {
            if (other.changeDescription != null)
                return false;
        } else if (!changeDescription.equals(other.changeDescription))
            return false;
        if (changeNumber == null) {
            if (other.changeNumber != null)
                return false;
        } else if (!changeNumber.equals(other.changeNumber))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id != other.id)
            return false;
        if (isPublicReviewRequest != other.isPublicReviewRequest)
            return false;
        if (lastUpdated == null) {
            if (other.lastUpdated != null)
                return false;
        } else if (!lastUpdated.equals(other.lastUpdated))
            return false;
        if (repository == null) {
            if (other.repository != null)
                return false;
        } else if (!repository.equals(other.repository))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (submitter == null) {
            if (other.submitter != null)
                return false;
        } else if (!submitter.equals(other.submitter))
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
        if (timeAdded == null) {
            if (other.timeAdded != null)
                return false;
        } else if (!timeAdded.equals(other.timeAdded))
            return false;
        return true;
    }   

}
