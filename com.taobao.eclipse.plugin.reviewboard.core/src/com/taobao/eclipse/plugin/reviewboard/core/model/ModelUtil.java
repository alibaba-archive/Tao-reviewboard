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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.taobao.eclipse.plugin.reviewboard.core.util.StringComparatorNoCareCase;

/**
 * 类说明:ModelUtil
 *
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class ModelUtil {

    public static List<String> getModelUserNameList(List<ModelUser> modelUserList){
        StringComparatorNoCareCase stringComparatorNoCareCase = new StringComparatorNoCareCase();
        List<String> nameList = new ArrayList<String>();
        for (ModelUser modelUser : modelUserList) {
            if( null == modelUser.getUsername() || modelUser.getUsername().trim().isEmpty() ){
                continue;
            }
            nameList.add(modelUser.getUsername());
        }
        Collections.sort(nameList, stringComparatorNoCareCase);
        return nameList;
    }

    public static List<String> getModelRepositoryNameList(List<ModelRepository> modelRepositoryList){
        StringComparatorNoCareCase stringComparatorNoCareCase = new StringComparatorNoCareCase();
        List<String> nameList = new ArrayList<String>();
        for (ModelRepository modelRepository : modelRepositoryList) {
            if( null == modelRepository.getName() || modelRepository.getName().trim().isEmpty() ){
                continue;
            }
            nameList.add(modelRepository.getName());
        }
        Collections.sort(nameList, stringComparatorNoCareCase);
        return nameList;
    }

    public static List<String> getModelReviewGroupNameList(List<ModelReviewGroup> modelReviewGroupList){
        StringComparatorNoCareCase stringComparatorNoCareCase = new StringComparatorNoCareCase();
        List<String> nameList = new ArrayList<String>();
        for (ModelReviewGroup modelReviewGroup : modelReviewGroupList) {
            if( null == modelReviewGroup.getName() || modelReviewGroup.getName().trim().isEmpty() ){
                continue;
            }
            nameList.add(modelReviewGroup.getName());
        }
        Collections.sort(nameList, stringComparatorNoCareCase);
        return nameList;
    }
    
}
