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
package com.taobao.eclipse.plugin.reviewboard.core.config;

import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvanceFuctionPreferencePage.P_ALL_STARTVERSION_GET;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvanceFuctionPreferencePage.P_BRANCHS_STARTVERSION_GET;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvanceFuctionPreferencePage.P_CHARSET;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvancePagePreferencePage.P_COMPAREVESION_PRECOMMIT;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvancePagePreferencePage.P_OPTIONAL_BUGCLOSED;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvancePagePreferencePage.P_OPTIONAL_DRAFT;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.AdvancePagePreferencePage.P_OPTIONAL_PAGE_COMMIT;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_BRANCH;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_GROUPS;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_PASSWORD;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_PEOPLES;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_REPOSITORY;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_SERVER;
import static com.taobao.eclipse.plugin.reviewboard.core.config.preference.ReviewBoardPreferencePage.P_USERID;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;

import org.eclipse.jface.preference.IPreferenceStore;

import com.taobao.eclipse.plugin.reviewboard.core.ReviewboardCorePlugin;
import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;

/**
 * 类说明:读取服务器信息
 * 
 * @author 智清 
 * 创建时间：2010-12-29
 *
 */
public class RbConfigReader {
    
    /**
     * 从首选项中获取配置信息
     * @param propertiesBean
     * @return
     */
    public static RbConfig getRbConfig( RbConfig rbConfig ){
        if( null == rbConfig )
            rbConfig = new RbConfig();
        String groups = null;
        String peoples = null;
        String repository = null;
        String branch = null;
        String charsetEncoding = null;
        
        IPreferenceStore preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        branch = preferenceStore.getString(P_BRANCH );
        groups = preferenceStore.getString(P_GROUPS );
        peoples = preferenceStore.getString(P_PEOPLES );
        repository = preferenceStore.getString(P_REPOSITORY );
        charsetEncoding = preferenceStore.getString(P_CHARSET );
        
        String allowBranchesStartVersionGetStr = null;
        String allowAllStartVersionGetStr = null;
        try {
        	allowAllStartVersionGetStr = preferenceStore.getString(P_ALL_STARTVERSION_GET );
        	allowBranchesStartVersionGetStr = preferenceStore.getString(P_BRANCHS_STARTVERSION_GET );
        } catch (Exception e) {
        }
        boolean allowBranchesStartVersionGet = false;
        boolean allowAllStartVersionGet = false;
        if ( allowBranchesStartVersionGetStr != null && !allowBranchesStartVersionGetStr.trim().isEmpty()) {
        	allowBranchesStartVersionGet = Boolean.valueOf(allowBranchesStartVersionGetStr).booleanValue();
        }
        if ( allowAllStartVersionGetStr != null && !allowAllStartVersionGetStr.trim().isEmpty()) {
        	allowAllStartVersionGet = Boolean.valueOf(allowAllStartVersionGetStr).booleanValue();
        }
        
        String allowCompareVersionForPreCommitStr = null;
        String allowOptionalPageCommitStr = null;
        String allowOptionBugClosedStr = null;
        String allowDraftStr = null;
        try{
        	allowCompareVersionForPreCommitStr = preferenceStore.getString(P_COMPAREVESION_PRECOMMIT );
        	allowOptionalPageCommitStr = preferenceStore.getString(P_OPTIONAL_PAGE_COMMIT );
        	allowOptionBugClosedStr = preferenceStore.getString(P_OPTIONAL_BUGCLOSED );
        	allowDraftStr = preferenceStore.getString(P_OPTIONAL_DRAFT );
        } catch (Exception e) {
        }
        boolean allowCompareVersionForPreCommit = false;
        boolean allowOptionalPageCommit = false;
        boolean allowOptionBugClosed = false;
        boolean allowDraft = false;
        if ( allowCompareVersionForPreCommitStr != null && !allowCompareVersionForPreCommitStr.trim().isEmpty()) {
        	allowCompareVersionForPreCommit = Boolean.valueOf(allowCompareVersionForPreCommitStr).booleanValue();
        }
        if ( allowOptionalPageCommitStr != null && !allowOptionalPageCommitStr.trim().isEmpty()) {
        	allowOptionalPageCommit = Boolean.valueOf(allowOptionalPageCommitStr).booleanValue();
        }
        if ( allowOptionBugClosedStr != null && !allowOptionBugClosedStr.trim().isEmpty()) {
        	allowOptionBugClosed = Boolean.valueOf(allowOptionBugClosedStr).booleanValue();
        }
        if ( allowDraftStr != null && !allowDraftStr.trim().isEmpty()) {
            allowDraft = Boolean.valueOf(allowDraftStr).booleanValue();
        }
        
        rbConfig.setBranch( null == branch ? EMPTY_STRING : branch.trim());
        rbConfig.setGroups( null == groups ? EMPTY_STRING : groups.trim());
        rbConfig.setPeoples( null == peoples ? EMPTY_STRING : peoples.trim());
        rbConfig.setRepository( null == repository ? EMPTY_STRING : repository.trim());
        rbConfig.setAllowAllStartVersionGet(allowAllStartVersionGet);
        rbConfig.setAllowBranchesStartVersionGet(allowBranchesStartVersionGet);
        rbConfig.setAllowCompareVersionForPreCommit(allowCompareVersionForPreCommit);
        rbConfig.setAllowOptionalPageCommit(allowOptionalPageCommit);
        rbConfig.setAllowOptionBugClosed(allowOptionBugClosed);
        rbConfig.setAllowDraft(allowDraft);
        rbConfig.setCharsetEncoding(charsetEncoding);
        
        return rbConfig;
    }
    
    /**
     * 从首选项中获取服务器信息
     * @param rbConfig
     * @return
     */
    public static RbConfig getServerConfig( RbConfig rbConfig ){
        if( null == rbConfig )
            rbConfig = new RbConfig();
        String server = null;
        String userId = null;
        String password = null;
        
        //从首选项中读取
        IPreferenceStore preferenceStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        server = preferenceStore.getString(P_SERVER);
        userId = preferenceStore.getString(P_USERID );
        password = preferenceStore.getString(P_PASSWORD );
        if( null != server && !server.trim().isEmpty() ){
            String serverUrlTmp = (null == server ? EMPTY_STRING : server.trim());
            serverUrlTmp =  ReviewboardUtil.norminateRepositoryUrl( serverUrlTmp ) ;
            rbConfig.setServer(serverUrlTmp);
            rbConfig.setUserId( null == userId ? EMPTY_STRING : userId.trim());
            rbConfig.setPassword( null == password ? EMPTY_STRING : password.trim());
            if( null != server && !server.trim().isEmpty() ){
                server =  ReviewboardUtil.norminateRepositoryUrl( server.trim() ) ;
                rbConfig.setServer( server );
            }
            return rbConfig;
        }
        //如果首选项中还是没有读到，则返回null
        return rbConfig;
    }
    
}
