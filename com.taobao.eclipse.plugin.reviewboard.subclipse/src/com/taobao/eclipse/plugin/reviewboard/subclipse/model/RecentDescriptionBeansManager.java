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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.eclipse.core.runtime.IPath;

import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipsePlugin;
import com.taobao.eclipse.plugin.reviewboard.subclipse.model.RecentDescriptionBeans.RecentDescriptionBean;

/**
 * ��˵��:�����Request�����б�Manager
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public class RecentDescriptionBeansManager{
    
    private static Object objectSyn = new Object();
   
    private File cacheFile;
    
    public static final String CACHE_FILE_RBL = "rbBeanListCacheV20111009.txt";
   
    /**
     * Ĭ����ౣ������Ķ��ٸ��б�
     */
    public static final int MAX_SIZE = 15;
    
    private RecentDescriptionBeans recentDescriptionBeans = null;

    private static RecentDescriptionBeansManager recentDescriptionBeansManagerSinglton = null;
    
    /**
     * private����ֹ���ⲿ��ʵ����
     */
    private RecentDescriptionBeansManager() {
        try {
            IPath path = RbSubclipsePlugin.getPlugin().getPluginPath();
            IPath cacheFile = path.append(CACHE_FILE_RBL);
            this.cacheFile = cacheFile.toFile();
            readCache();
        } catch (Exception e) {
        }
        if( null == recentDescriptionBeans ){
            recentDescriptionBeans = new RecentDescriptionBeans();
        }
    }

    public static RecentDescriptionBeansManager getSingletonInstance(){
        //double check
        if( null == recentDescriptionBeansManagerSinglton ){
            synchronized(objectSyn) {  
              if( null == recentDescriptionBeansManagerSinglton ){
                  recentDescriptionBeansManagerSinglton = new RecentDescriptionBeansManager();
              }
            }
        }
        return recentDescriptionBeansManagerSinglton;
    }
    
    public List<RecentDescriptionBean> getRecentDescriptionBeanList() {
        return recentDescriptionBeans.getRecentDescriptionBeanList();
    }
    
    /**
     * ����һ��RecentDescriptionBean
     * @param recentDescriptionBean
     * @param MAX_SIZE ��ౣ����ٸ������д���null������nullʱ��ʹ��Ĭ�ϵ�MAX_SIZE
     */
    public void addRecentDescription( String description, Integer maxSize ){
        if( null != maxSize && maxSize > 0 ){
            recentDescriptionBeans.addRecentDescription(description, maxSize );
        }else{
            recentDescriptionBeans.addRecentDescription(description, MAX_SIZE );
        }
    }
    
    public void readCache() {
        if (cacheFile == null || !cacheFile.exists()) {
            return;
        }

        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(cacheFile));
            recentDescriptionBeans = (RecentDescriptionBeans)in.readObject();
        } catch (Throwable e) {
            // ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    public void writeCache() {
        if (cacheFile == null) {
            return;
        }
        if( cacheFile.exists() ){
            //���ⷢ���°汾ʱ�����ڰ汾��ͻ������Recent Description��Զ���޷�д�뵽cache
            try {
                cacheFile.delete();
            } catch (Exception e) {
                // ignore
            }
        }
        try {
            cacheFile.createNewFile();
        } catch (Exception e) {
            // ignore
        }
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(cacheFile));
            out.writeObject(recentDescriptionBeans);
            out.flush();
        } catch (Throwable e) {
            // ignore
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    } 
}
