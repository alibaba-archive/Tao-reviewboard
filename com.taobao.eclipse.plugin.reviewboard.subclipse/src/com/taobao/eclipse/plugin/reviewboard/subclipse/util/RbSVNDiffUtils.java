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
package com.taobao.eclipse.plugin.reviewboard.subclipse.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.ISVNResource;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.taobao.eclipse.plugin.reviewboard.core.exception.DiffGenerateException;

/**
 * ��˵��:diff�����Util
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public final class RbSVNDiffUtils {

    private RbSVNDiffUtils() {
        super();
    }

    /**
     * ȡ��diffӰ��ķ�Χ
     * @Param localNewVersionFlag�������ǣ����localNewVersionFlag=true����ô��localResource != nullʱ��ȡ���ص����°汾
     */
    public static SVNDiffSummary[] diffSummarize(SVNUrl fromUrl, SVNRevision fromRevision, 
            SVNUrl toUrl, SVNRevision toRevision, 
            boolean localNewVersionFlag,ISVNResource localResource) throws DiffGenerateException {
        SVNDiffSummary[] sVNDiffSummarys = null;
        try {
            ISVNClientAdapter client = null;
            ISVNRepositoryLocation repository = SVNProviderPlugin.getPlugin().getRepository(fromUrl.toString());
            if (repository != null){
                client = repository.getSVNClient();
            }
            if( null == client ){
                return sVNDiffSummarys;
            }
            SVNRevision pegRevision = null;
            if ( localNewVersionFlag && fromUrl.toString().equals(toUrl.toString()) && localResource != null) {
                if (localResource.getResource() == null){
                    pegRevision = SVNRevision.HEAD;
                }
                else {
                    IResource resource = localResource.getResource();
                    ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
                    pegRevision = svnResource.getRevision();
                }
            }
            if (pegRevision == null){
                sVNDiffSummarys = client.diffSummarize(fromUrl, fromRevision, toUrl, toRevision, 40, false);
            }else{
                sVNDiffSummarys = client.diffSummarize(fromUrl, pegRevision, fromRevision, toRevision, 40, false);
            }
        }catch (SVNClientException e) {
            throw new DiffGenerateException(e);
        }catch ( SVNException e2 ) {
            throw new DiffGenerateException(e2);
        }catch ( Exception e3 ) {
            throw new DiffGenerateException(e3);
        }finally {
        }      
        return sVNDiffSummarys;
    }
    
    /**
     * ���϶���ļ���post-commit�����Ϲ���
     * ���ѡ���˶��Ŀ¼�Ͷ���ļ���
     * 1.���ĳ��Ŀ¼�͸�Ŀ¼�µ��ļ���������Ŀ¼�µ��ļ���ͬʱ��ѡ�У��������ļ���
     * 2.���ѡ����ĳ��Ŀ¼����ѡ���˸�Ŀ¼�µ����ɸ���Ŀ¼����ֱ��������Ŀ¼
     * �ܵ���˵���������д��ڸ��ӹ�ϵ�еĶ���
     * @param resource
     * @return
     */
    public static IResource[] integerateResourcesForSVNPostCommit(IResource[] resources){
        if( null == resources ){
            return null;
        }
        LinkedList<IResource> resouceLinkedList = new LinkedList<IResource>();
        Set<IResource> resouceSetReturn = new HashSet<IResource>();
        //��¼��Ҫɾ����Ԫ��
        Set<IResource> resouceSetDelete = new HashSet<IResource>();
        for(IResource resource : resources){
            resouceLinkedList.add(resource);
            resouceSetReturn.add(resource);
        }
        while( !resouceLinkedList.isEmpty() ){
            //ȡ����һ��Ԫ�أ�Ȼ�����̽���Ԫ�ش�LinkedListɾ����
            IResource resouceTmpFromList = resouceLinkedList.getFirst();
            IResource resouceTmp = resouceTmpFromList;
            resouceLinkedList.removeFirst();
            for( IResource resource : resouceLinkedList ){
                String resourceTmpPath = resouceTmp.getFullPath().toString();
                String resourcePath = resource.getFullPath().toString();
                if(resourceTmpPath.indexOf(resourcePath)!=-1){
                    resouceSetDelete.add(resouceTmp);
                }else if(resourcePath.indexOf(resourceTmpPath)!=-1){
                    resouceSetDelete.add(resource);
                }
            }
        }
        resouceSetReturn.removeAll(resouceSetDelete);
        IResource[] resourcesReturn = new IResource[resouceSetReturn.size()];
        resouceSetReturn.toArray(resourcesReturn);
        return resourcesReturn;
    }
    
}
