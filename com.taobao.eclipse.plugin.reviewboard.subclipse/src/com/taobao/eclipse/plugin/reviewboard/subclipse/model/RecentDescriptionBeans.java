/*
* (C) 2007-2011 Alibaba Group Holding Limited
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2 as
* published by the Free Software Foundation.
*
*
* If you have any question, please contact:ǧѾ <qianya@taobao.com>
* Authors:���� <zhiqing.ht@taobao.com>����ʱ<yinshi.nc@taobao.com>
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
 * ��˵��:�����Request�����б�
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
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
        //�����ж��Ƿ�������Ѿ�����
        boolean isAlreadyExist = false;
        for( RecentDescriptionBean recentDescriptionBeanTmp : recentDescriptionBeanList ){
            if( recentDescriptionBeanTmp.getDecriptionContent().trim().equalsIgnoreCase(description.trim()) ){
                isAlreadyExist = true;
                break;
            }
        }
        //���������
        if( isAlreadyExist ){
            return ;
        }
        recentDescriptionBean.setDecriptionContent(description);
        //����descriptionShortContent
        String descriptionShortContent = description;
        if( maxSize <= 0 || description.length() <= MAX_LENGTH_FOR_SHORT ){
            descriptionShortContent = description;
        }else{
            descriptionShortContent = description.substring(0, MAX_LENGTH_FOR_SHORT) +"...";
        }
        recentDescriptionBean.setDescriptionShortContent(descriptionShortContent);
        //��������
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
     * ��˵��:�����Request����
     * 
     * @author ���� 
     * ����ʱ�䣺2010-11-8
     */
    public static class RecentDescriptionBean implements Serializable{
        
        private static final long serialVersionUID = 892934288882942384L;
        
        /**
         * ���浽RecentDescriptionBeanʱ����������д��ȡ���ٸ��ַ�
         */
        public final static int MAX_LENGTH_FOR_SHORT = 50;

        /**
         * ����Description�ļ�Ҫ�����������������б���չʾ
         */
        private String descriptionShortContent;
        
        /**
         * ����Description��ȫ������
         */
        private String decriptionContent;
        
        /**
         * ��������
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
