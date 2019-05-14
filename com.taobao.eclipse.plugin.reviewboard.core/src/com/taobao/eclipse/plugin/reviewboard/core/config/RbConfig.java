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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARACTER_ENCODING;

import java.io.Serializable;

/**
 * 类说明:用户配置的参数（服务器信息及其它参数）
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class RbConfig implements Serializable {
    
	private static final long serialVersionUID = -5070924064118080135L;

	private String server;
    
    private String userId;
    
    private String password;
    
    private String branch;
    
    private String groups;
    
    private String peoples;
    
    private String repository;
    
    /**
     * 网络传输编码
     */
    private String charactorEncoding;
    
    /**
     * 生成diff的编码
     */
    private String charsetEncoding;
    
    private boolean allowBranchesStartVersionGet;
    
    private boolean allowAllStartVersionGet;
    
    private boolean allowCompareVersionForPreCommit;
    
    private boolean allowOptionalPageCommit;
    
    private boolean allowDraft;
    
    private boolean allowOptionBugClosed;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getPeoples() {
        return peoples;
    }

    public void setPeoples(String peoples) {
        this.peoples = peoples;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCharactorEncoding() {
        if( null == charactorEncoding || charactorEncoding.trim().isEmpty()){
            return CHARACTER_ENCODING;
        }
        else{
            return charactorEncoding;
        }
    }

    public void setCharactorEncoding(String charactorEncoding) {
        this.charactorEncoding = charactorEncoding;
    }

    public boolean isAllowBranchesStartVersionGet() {
        return allowBranchesStartVersionGet;
    }

    public void setAllowBranchesStartVersionGet(boolean allowBranchesStartVersionGet) {
        this.allowBranchesStartVersionGet = allowBranchesStartVersionGet;
    }

    public boolean isAllowAllStartVersionGet() {
        return allowAllStartVersionGet;
    }

    public void setAllowAllStartVersionGet(boolean allowAllStartVersionGet) {
        this.allowAllStartVersionGet = allowAllStartVersionGet;
    }

    public String getCharsetEncoding() {
        return charsetEncoding;
    }

    public void setCharsetEncoding(String charsetEncoding) {
        this.charsetEncoding = charsetEncoding;
    }

    public boolean isAllowCompareVersionForPreCommit() {
        return allowCompareVersionForPreCommit;
    }

    public void setAllowCompareVersionForPreCommit(boolean allowCompareVersionForPreCommit) {
        this.allowCompareVersionForPreCommit = allowCompareVersionForPreCommit;
    }

    public boolean isAllowOptionalPageCommit() {
        return allowOptionalPageCommit;
    }

    public void setAllowOptionalPageCommit(boolean allowOptionalPageCommit) {
        this.allowOptionalPageCommit = allowOptionalPageCommit;
    }

    public boolean isAllowOptionBugClosed() {
        return allowOptionBugClosed;
    }

    public void setAllowOptionBugClosed(boolean allowOptionBugClosed) {
        this.allowOptionBugClosed = allowOptionBugClosed;
    }

    public boolean isAllowDraft() {
        return allowDraft;
    }

    public void setAllowDraft(boolean allowDraft) {
        this.allowDraft = allowDraft;
    }
    
    
}
