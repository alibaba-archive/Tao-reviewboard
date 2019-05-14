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
package com.taobao.eclipse.plugin.reviewboard.subclipse.model;

import static com.taobao.eclipse.plugin.reviewboard.subclipse.model.RecentDescriptionBeans.RecentDescriptionBean.MAX_LENGTH_FOR_SHORT;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;

/**
 * 类说明:最近的Request描述列表
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public class RecentDescriptionBeans implements Serializable{
    
    private static final long serialVersionUID = 89298954265993412L;
    
    private final LinkedList<RecentDescriptionBean> recentDescriptionBeanList = new LinkedList<RecentDescriptionBean>();
    
    private void addRecentDescriptionBean( RecentDescriptionBean recentDescriptionBean, int maxSize ){
        if( recentDescriptionBeanList.size() >= maxSize ){
            recentDescriptionBeanList.removeFirst();
        }
        recentDescriptionBeanList.add( recentDescriptionBean );
    }
    
    public void addRecentDescription( String description, int maxSize ){
        RecentDescriptionBean recentDescriptionBean = new RecentDescriptionBean();
        //首先判断是否该描述已经存在
        boolean isAlreadyExist = false;
        for( RecentDescriptionBean recentDescriptionBeanTmp : recentDescriptionBeanList ){
            if( recentDescriptionBeanTmp.getDecriptionContent().trim().equalsIgnoreCase(description.trim()) ){
                isAlreadyExist = true;
                break;
            }
        }
        //如果不存在
        if( isAlreadyExist ){
            return ;
        }
        recentDescriptionBean.setDecriptionContent(description);
        //生成descriptionShortContent
        String descriptionShortContent = description;
        if( maxSize <= 0 || description.length() <= MAX_LENGTH_FOR_SHORT ){
            descriptionShortContent = description;
        }else{
            descriptionShortContent = description.substring(0, MAX_LENGTH_FOR_SHORT) +"...";
        }
        recentDescriptionBean.setDescriptionShortContent(descriptionShortContent);
        //设置日期
        Date nowtime = Calendar.getInstance().getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeString = timeFormat.format(nowtime);
        recentDescriptionBean.setCreateDate( timeString );
        this.addRecentDescriptionBean(recentDescriptionBean, maxSize);
    }
    
    public List<RecentDescriptionBean> getRecentDescriptionBeanList() {
        List<RecentDescriptionBean> rdbTmp = new ArrayList<RecentDescriptionBean>();
        RecentDescriptionBean recentDescriptionBeanFirst = new RecentDescriptionBean();
        recentDescriptionBeanFirst.setDescriptionShortContent(RbSubclipseMessages.getString("RECENT_DESCRI_LABLE"));
        recentDescriptionBeanFirst.setDecriptionContent(RbSubclipseMessages.getString("RECENT_DESCRI_LABLE"));
        rdbTmp.add( recentDescriptionBeanFirst );
        int rdbListSize = recentDescriptionBeanList.size();
        for( int index = rdbListSize - 1; index >=0 ; index-- ){
            rdbTmp.add( recentDescriptionBeanList.get( index ) );
        }
        return rdbTmp;
    }
    
    /**
     * 类说明:最近的Request描述
     * 
     * @author 智清 
     * 创建时间：2010-11-8
     */
    public static class RecentDescriptionBean implements Serializable{
        
        private static final long serialVersionUID = 892934288882942384L;
        
        /**
         * 保存到RecentDescriptionBean时，其描述简写截取多少个字符
         */
        public final static int MAX_LENGTH_FOR_SHORT = 50;

        /**
         * 本次Description的简要描述，用于在下拉列表中展示
         */
        private String descriptionShortContent;
        
        /**
         * 本次Description的全部内容
         */
        private String decriptionContent;
        
        /**
         * 创建日期
         */
        private String createDate;
        
        public String getDecriptionContent() {
            return decriptionContent;
        }

        public void setDecriptionContent(String decriptionContent) {
            this.decriptionContent = decriptionContent;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public String getDescriptionShortContent() {
            return descriptionShortContent;
        }

        public void setDescriptionShortContent(String descriptionShortContent) {
            this.descriptionShortContent = descriptionShortContent;
        }
        
        @Override
        public String toString() {
            if( null != decriptionContent ){
                return decriptionContent;
            }
            return super.toString();
        }

    }
       
}
