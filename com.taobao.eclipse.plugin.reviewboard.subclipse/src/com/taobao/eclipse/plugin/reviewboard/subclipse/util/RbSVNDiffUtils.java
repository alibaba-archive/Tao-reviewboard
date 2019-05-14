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
 * 类说明:diff的相关Util
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public final class RbSVNDiffUtils {

    private RbSVNDiffUtils() {
        super();
    }

    /**
     * 取得diff影响的范围
     * @Param localNewVersionFlag的作用是：如果localNewVersionFlag=true，那么当localResource != null时，取本地的最新版本
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
     * 整合多个文件的post-commit。整合规则：
     * 如果选中了多个目录和多个文件：
     * 1.如果某个目录和该目录下的文件（包括子目录下的文件）同时被选中，则抛弃文件。
     * 2.如果选中了某个目录，又选中了该目录下的若干个子目录，则直接抛弃子目录
     * 总的来说，抛弃所有存在父子关系中的儿子
     * @param resource
     * @return
     */
    public static IResource[] integerateResourcesForSVNPostCommit(IResource[] resources){
        if( null == resources ){
            return null;
        }
        LinkedList<IResource> resouceLinkedList = new LinkedList<IResource>();
        Set<IResource> resouceSetReturn = new HashSet<IResource>();
        //记录需要删除的元素
        Set<IResource> resouceSetDelete = new HashSet<IResource>();
        for(IResource resource : resources){
            resouceLinkedList.add(resource);
            resouceSetReturn.add(resource);
        }
        while( !resouceLinkedList.isEmpty() ){
            //取出第一个元素，然后立刻将该元素从LinkedList删掉。
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
