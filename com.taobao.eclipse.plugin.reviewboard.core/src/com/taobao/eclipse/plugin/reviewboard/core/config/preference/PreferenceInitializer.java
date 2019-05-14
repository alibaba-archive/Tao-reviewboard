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
package com.taobao.eclipse.plugin.reviewboard.core.config.preference;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARSET_AUTO;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.SERVER_DEFAULT;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.taobao.eclipse.plugin.reviewboard.core.ReviewboardCorePlugin;

/**
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore pStore = ReviewboardCorePlugin.getDefault().getPreferenceStore();
        pStore.setDefault(ReviewBoardPreferencePage.P_SERVER, SERVER_DEFAULT ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_USERID, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_PASSWORD, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_BRANCH, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_GROUPS, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_PEOPLES, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_REPOSITORY, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(ReviewBoardPreferencePage.P_REPOSITORY, EMPTY_STRING ); //$NON-NLS-1$
        pStore.setDefault(AdvanceFuctionPreferencePage.P_ALL_STARTVERSION_GET, Boolean.toString(false) ); //$NON-NLS-1$
        pStore.setDefault(AdvanceFuctionPreferencePage.P_BRANCHS_STARTVERSION_GET, Boolean.toString(false) ); //$NON-NLS-1$
        pStore.setDefault(AdvancePagePreferencePage.P_COMPAREVESION_PRECOMMIT, Boolean.toString(false) ); //$NON-NLS-1$
        pStore.setDefault(AdvancePagePreferencePage.P_OPTIONAL_PAGE_COMMIT, Boolean.toString(false) ); //$NON-NLS-1$
        pStore.setDefault(AdvancePagePreferencePage.P_OPTIONAL_DRAFT, Boolean.toString(false) ); //$NON-NLS-1$
        pStore.setDefault(AdvancePagePreferencePage.P_OPTIONAL_PAGE_COMMIT, Boolean.toString(false) ); //$NON-NLS-1$
        pStore.setDefault(AdvanceFuctionPreferencePage.P_CHARSET, CHARSET_AUTO ); //$NON-NLS-1$
    }
    
}
