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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.RepositoryProvider;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.ISVNRemoteResource;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.LocalResourceStatus;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.taobao.eclipse.plugin.reviewboard.core.exception.ReviewboardException;
import com.taobao.eclipse.plugin.reviewboard.subclipse.RbSubclipseMessages;

/**
 * 类说明:SVN路径或Version相关的Util
 * 
 * @author 智清 
 * 创建时间：2010-11-8
 */
public final class RbSVNUrlUtils {

    private RbSVNUrlUtils() {
        super();
    }
    /**
     * 本插件读取SVNRevision均遵循该规范
     */
    public static SVNRevision[] formateSVNRevisionUnify(String start, String stop ){
        SVNRevision fromRevision;
        SVNRevision toRevision;
        if(start.trim().isEmpty()){
            fromRevision = SVNRevision.HEAD;
        }
        else {
            try {
                int fromRevisionInt = Integer.parseInt(start.trim());
                long fromRevisionLong = fromRevisionInt;
                fromRevision = new SVNRevision.Number(fromRevisionLong);
            } catch (NumberFormatException e) {
                fromRevision = SVNRevision.HEAD;
            }
        }
        if(stop.trim().isEmpty()){
            toRevision = SVNRevision.HEAD;
        }
        else {
            try {
                int toRevisionInt = Integer.parseInt(stop.trim());
                long toRevisionLong = toRevisionInt;
                toRevision = new SVNRevision.Number(toRevisionLong);
            } catch (NumberFormatException e) {
                toRevision = SVNRevision.HEAD;
            }
        }
        return new SVNRevision[]{ fromRevision, toRevision };
    }
    
    /**
     * 计算出project的SVN路径和有效的Resource的SVN路径
     * 对于Resource的SVN路径：考虑到某些resource是新增项，并不具备SVN属性，
     * 如果isAllowResourceFileNotSVN，此时将一直往父目录找，直到找到具备SVN元素的Resource；如果!isAllowResourceFileNotSVN，则抛出异常
     * 返回格式：new String[]{ baseSVNUrl,resourceUploadDiffBaseSVNUrl,resourceCreateDiffSVNUrl }，
     * 分别代表：1.project的SVN路径、2.有效的用于上传Diff文件的SVN基路径、3.有效的用于生成Diff文件的SVN基路径
     * @param resource
     * @param isAllowResourceFileNotSVN 当resource不具备SVN属性时，如果isAllowResourceFileNotSVN，此时将一直往父目录找，直到找到具备SVN元素的Resource；如果!isAllowResourceFileNotSVN，则抛出异常
     * @return
     * @throws ReviewboardException
     */
    public static String[] getBaseSVNUrlForDiffUpload(IResource resource , boolean isAllowResourceFileNotSVN) throws ReviewboardException{
        //project的SVN路径
        String baseSVNUrl = null;
        //有效的用于上传Diff文件的SVN基路径
        String resourceUploadDiffBaseSVNUrl = null;
        //有效的用于生成Diff文件的SVN基路径
        String resourceCreateDiffSVNUrl = null;
        
        if (null == resource) {
            throw new ReviewboardException(RbSubclipseMessages.getString("ERROR_NOSVNFILE"));
        }
        
        if (null == resource.getProject()) {
            throw new ReviewboardException("ERROR_NOT_IPROJECT");
        }

        //下面将找出project的baseSVNUrl。该baseSVNUrl在上传diff文件时会用到
        baseSVNUrl = getSVNUrlForProject( resource );
        
        //下面将找出resouce的url
        IResource resourceTmp = resource;
        
        while(true){
            try {
                resourceCreateDiffSVNUrl = getSVNUrlForResouce( resourceTmp );
            } catch (ReviewboardException e) {
                if( !isAllowResourceFileNotSVN ){
                    throw new ReviewboardException(MessageFormat.format(
                            RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES_2"),
                            new Object[]{ resourceTmp.getProjectRelativePath().toString() }));
                }
            }
            if( null != resourceCreateDiffSVNUrl 
                    && !resourceCreateDiffSVNUrl.trim().isEmpty() ){
                //如果resource是文件而不是目录，则resourceUploadDiffSVNUrl直接取父目录
                if( resourceTmp instanceof IFile ){
                    IResource resourceTmpParent = resourceTmp.getParent();
                    resourceUploadDiffBaseSVNUrl = getSVNUrlForResouce( resourceTmpParent );
                }else{
                    resourceUploadDiffBaseSVNUrl = resourceCreateDiffSVNUrl;
                }
                break;
            }else{
                resourceTmp = resourceTmp.getParent();
            }
        }
        
        return new String[]{ baseSVNUrl,resourceUploadDiffBaseSVNUrl,resourceCreateDiffSVNUrl };
    }
    
    /**
     * 获取Resource的SVN路径
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static String getSVNUrlForResouce( IResource resource ) throws ReviewboardException{
        String svnUrl = null;
        
        //获取Resource的SVN路径
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        if ( null == svnResource ) {
            throw new ReviewboardException(MessageFormat.format(
                    RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                    new Object[]{ resource.getLocation().toString() }));
        } else {
            LocalResourceStatus status = null;
            try {
                status = svnResource.getStatus();
            } catch (SVNException e) {
            }
            if( null != status ){
                svnUrl = status.getUrlString() != null ? status.getUrlString() : EMPTY_STRING;
            }
            if ( null == status || !status.isManaged() || null == svnUrl || svnUrl.trim().isEmpty()) {
                throw new ReviewboardException(MessageFormat.format(
                        RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                        new Object[]{ resource.getLocation().toString() }));
            }
        }
        
        return svnUrl;
    }
    
    /**
     * 获取Resource所在的Project的SVN路径
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static String getSVNUrlForProject( IResource resource ) throws ReviewboardException{
        String svnUrl = null;
        
        //获取Project的SVN路径
        SVNTeamProvider svnProviderBase = (SVNTeamProvider) RepositoryProvider.getProvider(resource.getProject(), 
                SVNProviderPlugin.getTypeId());
        if (null == svnProviderBase) {
            throw new ReviewboardException(
                    MessageFormat.format(RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                    new Object[]{ resource.getProject().getName() }));
        }
        ISVNLocalResource svnResourceBase = SVNWorkspaceRoot.getSVNResourceFor(resource.getProject());
        if ( null == svnResourceBase) {
            throw new ReviewboardException(
                    MessageFormat.format(RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                    new Object[]{ resource.getProject().getName() }));
        }
        try {
            LocalResourceStatus statusBase = svnResourceBase.getStatus();
            if( null != statusBase ){
                svnUrl = statusBase.getUrlString() != null ? statusBase.getUrlString() : EMPTY_STRING;
            }
            if ( null == statusBase || !statusBase.isManaged() || null == svnUrl || svnUrl.trim().isEmpty()) {
                throw new ReviewboardException(
                        MessageFormat.format(RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                        new Object[]{ resource.getProject().getName() }));
            }
        } catch (Exception e) {
            throw new ReviewboardException("getSVNUrlForResouce(IResource resource , boolean isGetSVNUrlForProject), error.", e);
        }
        
        return svnUrl;
    }
    
    /**
     * 获取Resource的SVN Repository Root Url
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static String getRepositoryRootUrlForResource( IResource resource ) throws ReviewboardException{
        String repositoryRootUrl = null;
        
        //获取Resource的SVN路径
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        if ( null == svnResource ) {
            throw new ReviewboardException(MessageFormat.format(RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                    new Object[]{ resource.getLocation().toString() }));
        } else {
            LocalResourceStatus status = null;
            try {
                status = svnResource.getStatus();
            } catch (SVNException e) {
            }
            if( null != status ){
                try {
                    repositoryRootUrl = status.getRepository().getRootFolder().getUrl().toString();
                } catch (Exception e) {
                    repositoryRootUrl = null;
                }
            }
            if ( null == status || !status.isManaged() 
                    || null == repositoryRootUrl 
                    || repositoryRootUrl.trim().isEmpty()) {
                throw new ReviewboardException(
                        MessageFormat.format(RbSubclipseMessages.getString("ERROR_NOSVNPROPERTIES"),
                        new Object[]{ resource.getLocation().toString() }));
            }
        }
        
        return repositoryRootUrl;
    }
    
    /**
     * 判断所有的project是否具备共有的SVN Repository Root Url
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static boolean isProjectWithSameSVNRepository( Set<IProject> projectSet ) throws ReviewboardException{
        String repositoryRoot = null;
        if( null != projectSet && !projectSet.isEmpty() ){
            for( IProject projectTmp : projectSet ){
                if( null == repositoryRoot 
                        || repositoryRoot.trim().isEmpty()){
                    repositoryRoot = getRepositoryRootUrlForResource( projectTmp );
                }
                String repositoryRootTmp = getRepositoryRootUrlForResource( projectTmp );
                if( !repositoryRootTmp.equalsIgnoreCase(repositoryRoot)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 该方法的作用是用于判断所有的资源是否都是主干或分支资源。str请传入"/trunk/"或"/branches/"
     * @param projectSet
     * @param str
     * @return
     * @throws ReviewboardException
     */
    public static boolean isSvnUrlAllContainStr( Set<IProject> projectSet, String str ) throws ReviewboardException{
        for( IProject projectTmp : projectSet ){
            String svnUrlString = getSVNUrlForProject( projectTmp );
            if( svnUrlString.indexOf(str) == -1 ){
                return false;
            }
        }
        return true;
    }

    /**
     * Resource是否具有SVN属性
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static boolean isResourceHasSVNProperty( IResource resource ) throws ReviewboardException{
        String svnUrl = null;
        
        ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        if ( null == svnResource ) {
            return false;
        } else {
            LocalResourceStatus status = null;
            try {
                status = svnResource.getStatus();
            } catch (SVNException e) {
            }
            if( null != status ){
                svnUrl = status.getUrlString() != null ? status.getUrlString() : EMPTY_STRING;
            }
            if ( null == status || !status.isManaged() || null == svnUrl || svnUrl.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 返回SVN历史
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static ISVNLogMessage[] getPriorSVNRevision( SVNUrl url, SVNRevision revisionStart, 
            SVNRevision revisionEnd ) throws ReviewboardException{
        ISVNLogMessage[] svnLogMessages = null;
        ISVNClientAdapter client = null;
        try {
            ISVNRepositoryLocation repository = SVNProviderPlugin.getPlugin().getRepository(url.toString());
            if (repository != null){
                client = repository.getSVNClient();
            }
            if( null == client ){
                return svnLogMessages;
            }
            svnLogMessages = client.getLogMessages(url, revisionStart, revisionEnd);
        } catch (SVNException e) {
            throw new ReviewboardException(e);
        } catch (SVNClientException e) {
            throw new ReviewboardException(e);
        }
        return svnLogMessages;
    }

    /**
     * 获取Resource的Base或Head版本
     * @param resource
     * @param maxChooseHead
     * @return
     */
    public static SVNRevision.Number getSVNRevisionBaseOrHead( IResource resource, boolean maxChooseHead ){
        final ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        ISVNRemoteResource svnRemoteResourceBase = null;
        try {
            if(maxChooseHead){
                svnRemoteResourceBase = localResource.getRemoteResource( SVNRevision.HEAD );
            }else{
                svnRemoteResourceBase = localResource.getRemoteResource( SVNRevision.BASE );
            }
        } catch (Exception e) {
            return null;
        }
        SVNRevision.Number svnRemoteResourceBaseNumber = svnRemoteResourceBase.getLastChangedRevision();
        return svnRemoteResourceBaseNumber;
    }
    
    /**
     * 获取Resource的最低版本版本
     * @param resource
     * @param maxChooseHead
     * @return
     */
    public static SVNRevision.Number getSVNRevisionMin( IResource resource  ){
        SVNRevision.Number svnRevisionNumber = new SVNRevision.Number( 1 );
        return reviseSVNRevision( resource, svnRevisionNumber, false, false, null );
    }
    
    /**
     * 修正用户输入的版本号。
     * 修正原则：
     * 1.如果用户输入的版本号正确，则找到真实版本号并返回
     * 2.如果用户输入的版本号不正确，如果该版本号大于最高版本，则返回BASE或HEAD版本
     * 3.如果用户输入的版本号不正确，如果该版本号小于最高版本，则返回最低版本
     * 4.备注：当maxChooseHead=true时，最高版本以HEAD为准；否则以BASE为准
     * @param resource
     * @param svnRevisionNumber 待检查的版本
     * @param svnRevisionStopNumber 用于参考的最高版本，允许传入null。该参数的传入是为了优化reviseSVNRevision的性能；请务必保证该参数的实际值的正确性
     * @return
     * @throws ReviewboardException
     */
    public static SVNRevision.Number reviseSVNRevisionAdaptByMaxOrMin( IResource resource, 
            SVNRevision.Number svnRevisionNumber, boolean maxChooseHead, SVNRevision.Number svnRevisionStopNumber ){
        if( null == resource ){
            return svnRevisionNumber;
        }
        final ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        try {
            localResource.getRemoteResource( svnRevisionNumber );
            //最好的情况，用户输入合法
            return svnRevisionNumber;
            //return svnRemoteResource.getLastChangedRevision();
        } catch (Exception e) {
        }
        SVNRevision.Number svnRemoteResourceBaseNumber = getSVNRevisionBaseOrHead( resource, maxChooseHead );
        //用户要求取最高版本，则直接返回最高版本
        if( svnRevisionNumber.getNumber() > svnRemoteResourceBaseNumber.getNumber() ){
            return svnRemoteResourceBaseNumber;
        }
        SVNRevision.Number maxConsultRevisionNumber = null;
        if( null != svnRevisionStopNumber ){
            maxConsultRevisionNumber = svnRevisionStopNumber;
        }else{
            maxConsultRevisionNumber = svnRemoteResourceBaseNumber;
        }
        SVNRevision.Number minConsultRevisionNumber = null;
        //svnRevisionNumber低于最高版本，由于svnRevisionNumber对应的svnRemoteResource不存在，则很显然这个svnRevisionNumber是比最低版本低的
        if( svnRevisionNumber.getNumber() < svnRemoteResourceBaseNumber.getNumber() ){
            minConsultRevisionNumber = svnRevisionNumber;
        }
        else{
            minConsultRevisionNumber = null;
        }
        //否则，折半查找（找不到相应的API，所以只能用折半查找法；这里应该是要有个API来获取Start版本号的。SVNRevision.START也没法获取到。）
        return getSVNRevisionMinBySplitHalf( resource, localResource, minConsultRevisionNumber, maxConsultRevisionNumber );
    }
    
    /**
     * 修正用户输入的版本号。
     * 修正原则：
     * 1.如果用户输入的版本号正确，则找到真实版本号并返回
     * 2.如果用户输入的版本号不正确，且chooseMax=true则返回BASE或HEAD版本
     * 3.如果用户输入的版本号不正确，且chooseMax=false则返回最小版本
     * 4.备注：当maxChooseHead=true时，最高版本以HEAD为准；否则以BASE为准
     * @param resource
     * @param svnRevisionNumber 待检查的版本
     * @param svnRevisionStopNumber 用于参考的最高版本，允许传入null。该参数的传入是为了优化reviseSVNRevision的性能；请务必保证该参数的实际值的正确性
     * @return
     * @throws ReviewboardException
     */
    public static SVNRevision.Number reviseSVNRevision( IResource resource, 
            SVNRevision.Number svnRevisionNumber, boolean chooseMax, boolean maxChooseHead, SVNRevision.Number svnRevisionStopNumber ){
        if( null == resource ){
            return svnRevisionNumber;
        }
        final ISVNLocalResource localResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
        try {
            localResource.getRemoteResource( svnRevisionNumber );
            //最好的情况，用户输入合法
            return svnRevisionNumber;
            //return svnRemoteResource.getLastChangedRevision();
        } catch (Exception e) {
        }
        SVNRevision.Number svnRemoteResourceBaseNumber = getSVNRevisionBaseOrHead( resource, maxChooseHead );
        //用户要求取最高版本，则直接返回最高版本
        if( chooseMax ){
            return svnRemoteResourceBaseNumber;
        }
        SVNRevision.Number maxConsultRevisionNumber = null;
        if( null != svnRevisionStopNumber ){
            maxConsultRevisionNumber = svnRevisionStopNumber;
        }else{
            maxConsultRevisionNumber = svnRemoteResourceBaseNumber;
        }
        SVNRevision.Number minConsultRevisionNumber = null;
        //svnRevisionNumber低于最高版本，由于svnRevisionNumber对应的svnRemoteResource不存在，则很显然这个svnRevisionNumber是比最低版本低的
        if( svnRevisionNumber.getNumber() < svnRemoteResourceBaseNumber.getNumber() ){
            minConsultRevisionNumber = svnRevisionNumber;
        }
        else{
            minConsultRevisionNumber = null;
        }
        //否则，折半查找（找不到相应的API，所以只能用折半查找法；这里应该是要有个API来获取Start版本号的。SVNRevision.START也没法获取到。）
        return getSVNRevisionMinBySplitHalf( resource, localResource, minConsultRevisionNumber, maxConsultRevisionNumber );
    }

    /**
     * 折半查找法找出SVN的最低版本（找不到相应的API，所以只能用折半查找法；这里应该是要有个API来获取Start版本号的。SVNRevision.START也没法获取到。）
     * @param localResource
     * @param svnMinRevisionNumber 此项允许空
     * @param svnMaxRevisionNumber 此项不允许空
     * @return
     */
    private static SVNRevision.Number getSVNRevisionMinBySplitHalf( IResource resource, 
            ISVNLocalResource localResource, SVNRevision.Number svnMinRevisionNumber, SVNRevision.Number svnMaxRevisionNumber ){
        long max = svnMaxRevisionNumber.getNumber();
        long min = 0;
        if( null == svnMinRevisionNumber ){
            svnMinRevisionNumber = new SVNRevision.Number( 1 );
        }
        min = svnMinRevisionNumber.getNumber();
        if( min == max ){
            return svnMaxRevisionNumber;
        }
        long threshold = 50L;
        
        //补充一段算法:当 max - min <= 某个值时，用另一种方法探测下，尽可能减少探测次数，从而优化该算法的性能
        //如果是文件，则直接读取修改日志取版本号；如果是文件夹，由于日志可能会非常多，所以先将版本号缩小到threshold范围以内，再取版本号
        if( !(resource instanceof IFile) ){
            while( max - min > threshold ){
                long middle = ( max + min )/2;
                SVNRevision.Number svnMiddleRevisionNumber = new SVNRevision.Number(middle);
                try {
                    localResource.getRemoteResource( svnMiddleRevisionNumber );
                    //svnMaxRevisionNumber = svnRemoteMiddle.getLastChangedRevision();
                    svnMaxRevisionNumber = svnMiddleRevisionNumber;
                } catch (Exception e) {
                    svnMinRevisionNumber = svnMiddleRevisionNumber;
                }
                max = svnMaxRevisionNumber.getNumber();
                min = svnMinRevisionNumber.getNumber();
                if( min == max ){
                    return svnMaxRevisionNumber;
                }
            }
        }

        if( max == min || max - min == 1L ){
            try {
                localResource.getRemoteResource( svnMinRevisionNumber );
                return svnMinRevisionNumber;
                //return svnMinMiddleResource.getLastChangedRevision();
            } catch (Exception e) {
            }
            return svnMaxRevisionNumber;
        }
        try {
            String svnUrlString = getSVNUrlForResouce( resource );
            SVNUrl svnUrl = new SVNUrl(svnUrlString);
            ISVNLogMessage[] svnLogMessages = getPriorSVNRevision( svnUrl, svnMinRevisionNumber, svnMaxRevisionNumber );
            
            if( null != svnLogMessages && svnLogMessages.length > 0 ){
                for( ISVNLogMessage svnLogMessage : svnLogMessages ){
                    SVNRevision.Number svnRevisionNumber = svnLogMessage.getRevision();
                    try {
                        localResource.getRemoteResource( svnRevisionNumber );
                        return svnRevisionNumber;
                        //return svnRemoteResourceTmp.getLastChangedRevision();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (MalformedURLException e) {
        } catch (ReviewboardException e) {
        }
        return svnMaxRevisionNumber;
    }

}
