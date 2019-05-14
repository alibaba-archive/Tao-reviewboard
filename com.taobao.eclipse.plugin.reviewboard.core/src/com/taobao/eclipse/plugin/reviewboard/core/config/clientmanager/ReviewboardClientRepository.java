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
package com.taobao.eclipse.plugin.reviewboard.core.config.clientmanager;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EXTENSIONPOINT_CLIENT;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARACTER_ENCODING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.taobao.eclipse.plugin.reviewboard.core.client.IReviewboardClient;
import com.taobao.eclipse.plugin.reviewboard.core.client.ReviewboardClientData;
import com.taobao.eclipse.plugin.reviewboard.core.client.ReviewboardClientImpl;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfig;
import com.taobao.eclipse.plugin.reviewboard.core.config.RbConfigReader;
import com.taobao.eclipse.plugin.reviewboard.core.service.IReviewboardService;
import com.taobao.eclipse.plugin.reviewboard.core.service.impl.ReviewboardServiceImpl;
import com.taobao.eclipse.plugin.reviewboard.core.util.ReviewboardUtil;

/**
 * 类说明:IReviewboardClientRepository，Client和缓存数据仓库
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 *
 */
public class ReviewboardClientRepository implements IReviewboardClientRepository {

    private Map<String, IReviewboardClient> urlToClientMap = new HashMap<String, IReviewboardClient>();

    private static ReviewboardClientRepository rbClientRepositorySingleton = null;

    private final static Object objectSyn = new Object();
    
    private ReviewboardClientRepository() {
    	;
    }
    
    public static ReviewboardClientRepository getSingletonInstance() {
        if (null == rbClientRepositorySingleton) {
            synchronized (objectSyn) {
                if (null == rbClientRepositorySingleton) {
                    rbClientRepositorySingleton = new ReviewboardClientRepository(); 
                }
            }
        }
        return rbClientRepositorySingleton;
    }

    public List<IClientRepositoryAwair> clientRepositoryAwairRegiest(){
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSIONPOINT_CLIENT);
        IExtension[] extensions = extensionPoint.getExtensions();
        List<IClientRepositoryAwair> results = new ArrayList<IClientRepositoryAwair>();
        for (int i = 0; i < extensions.length; i++) {
            IConfigurationElement[] elements = extensions[i].getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                try {
                    Object detector = elements[j].createExecutableExtension("class"); //$NON-NLS-1$
                    if (detector instanceof IClientRepositoryAwair) {
                        IClientRepositoryAwair clientRepositoryAwair = (IClientRepositoryAwair)detector;
                        clientRepositoryAwair.setReviewboardClientRepository(this);
                        results.add(clientRepositoryAwair);
                    }
                } catch(CoreException e) {
                }
            }
        }
        return results;
    }
    
    /**
     * 返回IReviewboardClient
     * @param refreshForce
     * @return
     */
    public synchronized IReviewboardClient getClient(boolean refreshForce, IProgressMonitor monitor) {
        if( null == monitor ){
            monitor = new NullProgressMonitor();
        }
        RbConfig rbConfig = RbConfigReader.getServerConfig(null);
        if( null == rbConfig || null == rbConfig.getServer() ){
            return null;
        }
        IReviewboardClient reviewboardClient = this.urlToClientMap.get(rbConfig.getServer()); 
        boolean isClientDataNull = false;
        ReviewboardClientData clientData = null;
        if (null == reviewboardClient) {
            isClientDataNull = true;
            clientData = new ReviewboardClientData();
            reviewboardClient = new ReviewboardClientImpl(clientData, rbConfig.getServer(), 
                    rbConfig.getUserId(), rbConfig.getPassword(), CHARACTER_ENCODING );
        }
        clientData = reviewboardClient.getClientData();
        if( null == clientData ||
                null == clientData.getReviewGroupList() || clientData.getReviewGroupList().isEmpty() 
                || null == clientData.getRepositoryList() || clientData.getRepositoryList().isEmpty() 
                || null == clientData.getUserList() || clientData.getUserList().isEmpty() ){
            isClientDataNull = true;
        }
        //强制刷新RepositoryData
        if( isClientDataNull || refreshForce ){
            IReviewboardService reviewboardService = new ReviewboardServiceImpl( reviewboardClient );
            boolean loginSuccess = reviewboardService.validCredentials(rbConfig.getUserId(), rbConfig.getPassword(), null);
            if( loginSuccess ){
                reviewboardService.updateRepositoryData(true, monitor);
                this.urlToClientMap.put(rbConfig.getServer(), reviewboardClient);
            }
        }
        return reviewboardClient;
    }
    
    public synchronized IReviewboardClient getClient(boolean refreshForce, String serverUrl, 
            String userName, String password, String charactorEncoding) {
        serverUrl =  ReviewboardUtil.norminateRepositoryUrl( serverUrl.trim() ) ;
        IReviewboardClient reviewboardClient = this.urlToClientMap.get(serverUrl); 
        boolean isClientDataNull = false;
        ReviewboardClientData clientData = null;
        if (null == reviewboardClient) {
            isClientDataNull = true;
            clientData = new ReviewboardClientData();
            reviewboardClient = new ReviewboardClientImpl(clientData, serverUrl, userName, password, charactorEncoding);
        }
        clientData = reviewboardClient.getClientData();
        if( null == clientData.getReviewGroupList() || clientData.getReviewGroupList().isEmpty() 
                || null == clientData.getRepositoryList() || clientData.getRepositoryList().isEmpty() 
                || null == clientData.getUserList() || clientData.getUserList().isEmpty() ){
            isClientDataNull = true;
        }
        //强制刷新RepositoryData
        if( isClientDataNull || refreshForce ){
            IReviewboardService reviewboardService = new ReviewboardServiceImpl( reviewboardClient );
            boolean loginSuccess = reviewboardService.validCredentials(userName, password, null);
            if( loginSuccess ){
                this.urlToClientMap.put(serverUrl, reviewboardClient);
                reviewboardService.updateRepositoryData(true, new NullProgressMonitor());
            }
        }
        return reviewboardClient;
    }
    
    public IReviewboardClient getClientFromUrlToClientMap(){
        RbConfig rbConfig = RbConfigReader.getServerConfig(null);
        if( null == rbConfig || null == rbConfig.getServer()){
            return null;
        }
        IReviewboardClient reviewboardClient = this.urlToClientMap.get(rbConfig.getServer()); 
        return reviewboardClient;
    }

    public synchronized void resetCache(){
        try {
            IReviewboardClient reviewboardClient = getClientFromUrlToClientMap();
            if( null == reviewboardClient ){
                return ;
            }
            RbConfig rbConfig = RbConfigReader.getServerConfig(null);
            if( null == rbConfig || null == rbConfig.getServer()){
                return ;
            }
            //强制刷新RepositoryData
            IReviewboardService reviewboardService = new ReviewboardServiceImpl( reviewboardClient );
            boolean loginSuccess = reviewboardService.validCredentials(rbConfig.getUserId(), rbConfig.getPassword(), null);
            if( loginSuccess ){
                reviewboardService.updateRepositoryData(true, new NullProgressMonitor());
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * 类说明:自动更新缓存数据ClinentData的定时器
     * 
     * @author 智清 
     * 创建时间：2010-11-8
     */
    public static class ClientDataReloadTimeTask extends TimerTask {

        @Override
        public void run() {
            IReviewboardClientRepository rbClientRepository = ReviewboardClientRepository.getSingletonInstance();
            rbClientRepository.resetCache();
        }
        
    }
    
}
