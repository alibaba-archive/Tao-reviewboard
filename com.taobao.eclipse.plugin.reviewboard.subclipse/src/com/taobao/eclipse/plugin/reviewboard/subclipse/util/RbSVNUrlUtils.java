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
 * ��˵��:SVN·����Version��ص�Util
 * 
 * @author ���� 
 * ����ʱ�䣺2010-11-8
 */
public final class RbSVNUrlUtils {

    private RbSVNUrlUtils() {
        super();
    }
    /**
     * �������ȡSVNRevision����ѭ�ù淶
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
     * �����project��SVN·������Ч��Resource��SVN·��
     * ����Resource��SVN·�������ǵ�ĳЩresource������������߱�SVN���ԣ�
     * ���isAllowResourceFileNotSVN����ʱ��һֱ����Ŀ¼�ң�ֱ���ҵ��߱�SVNԪ�ص�Resource�����!isAllowResourceFileNotSVN�����׳��쳣
     * ���ظ�ʽ��new String[]{ baseSVNUrl,resourceUploadDiffBaseSVNUrl,resourceCreateDiffSVNUrl }��
     * �ֱ����1.project��SVN·����2.��Ч�������ϴ�Diff�ļ���SVN��·����3.��Ч����������Diff�ļ���SVN��·��
     * @param resource
     * @param isAllowResourceFileNotSVN ��resource���߱�SVN����ʱ�����isAllowResourceFileNotSVN����ʱ��һֱ����Ŀ¼�ң�ֱ���ҵ��߱�SVNԪ�ص�Resource�����!isAllowResourceFileNotSVN�����׳��쳣
     * @return
     * @throws ReviewboardException
     */
    public static String[] getBaseSVNUrlForDiffUpload(IResource resource , boolean isAllowResourceFileNotSVN) throws ReviewboardException{
        //project��SVN·��
        String baseSVNUrl = null;
        //��Ч�������ϴ�Diff�ļ���SVN��·��
        String resourceUploadDiffBaseSVNUrl = null;
        //��Ч����������Diff�ļ���SVN��·��
        String resourceCreateDiffSVNUrl = null;
        
        if (null == resource) {
            throw new ReviewboardException(RbSubclipseMessages.getString("ERROR_NOSVNFILE"));
        }
        
        if (null == resource.getProject()) {
            throw new ReviewboardException("ERROR_NOT_IPROJECT");
        }

        //���潫�ҳ�project��baseSVNUrl����baseSVNUrl���ϴ�diff�ļ�ʱ���õ�
        baseSVNUrl = getSVNUrlForProject( resource );
        
        //���潫�ҳ�resouce��url
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
                //���resource���ļ�������Ŀ¼����resourceUploadDiffSVNUrlֱ��ȡ��Ŀ¼
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
     * ��ȡResource��SVN·��
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static String getSVNUrlForResouce( IResource resource ) throws ReviewboardException{
        String svnUrl = null;
        
        //��ȡResource��SVN·��
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
     * ��ȡResource���ڵ�Project��SVN·��
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static String getSVNUrlForProject( IResource resource ) throws ReviewboardException{
        String svnUrl = null;
        
        //��ȡProject��SVN·��
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
     * ��ȡResource��SVN Repository Root Url
     * @param resource
     * @return
     * @throws ReviewboardException
     */
    public static String getRepositoryRootUrlForResource( IResource resource ) throws ReviewboardException{
        String repositoryRootUrl = null;
        
        //��ȡResource��SVN·��
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
     * �ж����е�project�Ƿ�߱����е�SVN Repository Root Url
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
     * �÷����������������ж����е���Դ�Ƿ������ɻ��֧��Դ��str�봫��"/trunk/"��"/branches/"
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
     * Resource�Ƿ����SVN����
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
     * ����SVN��ʷ
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
     * ��ȡResource��Base��Head�汾
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
     * ��ȡResource����Ͱ汾�汾
     * @param resource
     * @param maxChooseHead
     * @return
     */
    public static SVNRevision.Number getSVNRevisionMin( IResource resource  ){
        SVNRevision.Number svnRevisionNumber = new SVNRevision.Number( 1 );
        return reviseSVNRevision( resource, svnRevisionNumber, false, false, null );
    }
    
    /**
     * �����û�����İ汾�š�
     * ����ԭ��
     * 1.����û�����İ汾����ȷ�����ҵ���ʵ�汾�Ų�����
     * 2.����û�����İ汾�Ų���ȷ������ð汾�Ŵ�����߰汾���򷵻�BASE��HEAD�汾
     * 3.����û�����İ汾�Ų���ȷ������ð汾��С����߰汾���򷵻���Ͱ汾
     * 4.��ע����maxChooseHead=trueʱ����߰汾��HEADΪ׼��������BASEΪ׼
     * @param resource
     * @param svnRevisionNumber �����İ汾
     * @param svnRevisionStopNumber ���ڲο�����߰汾��������null���ò����Ĵ�����Ϊ���Ż�reviseSVNRevision�����ܣ�����ر�֤�ò�����ʵ��ֵ����ȷ��
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
            //��õ�������û�����Ϸ�
            return svnRevisionNumber;
            //return svnRemoteResource.getLastChangedRevision();
        } catch (Exception e) {
        }
        SVNRevision.Number svnRemoteResourceBaseNumber = getSVNRevisionBaseOrHead( resource, maxChooseHead );
        //�û�Ҫ��ȡ��߰汾����ֱ�ӷ�����߰汾
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
        //svnRevisionNumber������߰汾������svnRevisionNumber��Ӧ��svnRemoteResource�����ڣ������Ȼ���svnRevisionNumber�Ǳ���Ͱ汾�͵�
        if( svnRevisionNumber.getNumber() < svnRemoteResourceBaseNumber.getNumber() ){
            minConsultRevisionNumber = svnRevisionNumber;
        }
        else{
            minConsultRevisionNumber = null;
        }
        //�����۰���ң��Ҳ�����Ӧ��API������ֻ�����۰���ҷ�������Ӧ����Ҫ�и�API����ȡStart�汾�ŵġ�SVNRevision.STARTҲû����ȡ������
        return getSVNRevisionMinBySplitHalf( resource, localResource, minConsultRevisionNumber, maxConsultRevisionNumber );
    }
    
    /**
     * �����û�����İ汾�š�
     * ����ԭ��
     * 1.����û�����İ汾����ȷ�����ҵ���ʵ�汾�Ų�����
     * 2.����û�����İ汾�Ų���ȷ����chooseMax=true�򷵻�BASE��HEAD�汾
     * 3.����û�����İ汾�Ų���ȷ����chooseMax=false�򷵻���С�汾
     * 4.��ע����maxChooseHead=trueʱ����߰汾��HEADΪ׼��������BASEΪ׼
     * @param resource
     * @param svnRevisionNumber �����İ汾
     * @param svnRevisionStopNumber ���ڲο�����߰汾��������null���ò����Ĵ�����Ϊ���Ż�reviseSVNRevision�����ܣ�����ر�֤�ò�����ʵ��ֵ����ȷ��
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
            //��õ�������û�����Ϸ�
            return svnRevisionNumber;
            //return svnRemoteResource.getLastChangedRevision();
        } catch (Exception e) {
        }
        SVNRevision.Number svnRemoteResourceBaseNumber = getSVNRevisionBaseOrHead( resource, maxChooseHead );
        //�û�Ҫ��ȡ��߰汾����ֱ�ӷ�����߰汾
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
        //svnRevisionNumber������߰汾������svnRevisionNumber��Ӧ��svnRemoteResource�����ڣ������Ȼ���svnRevisionNumber�Ǳ���Ͱ汾�͵�
        if( svnRevisionNumber.getNumber() < svnRemoteResourceBaseNumber.getNumber() ){
            minConsultRevisionNumber = svnRevisionNumber;
        }
        else{
            minConsultRevisionNumber = null;
        }
        //�����۰���ң��Ҳ�����Ӧ��API������ֻ�����۰���ҷ�������Ӧ����Ҫ�и�API����ȡStart�汾�ŵġ�SVNRevision.STARTҲû����ȡ������
        return getSVNRevisionMinBySplitHalf( resource, localResource, minConsultRevisionNumber, maxConsultRevisionNumber );
    }

    /**
     * �۰���ҷ��ҳ�SVN����Ͱ汾���Ҳ�����Ӧ��API������ֻ�����۰���ҷ�������Ӧ����Ҫ�и�API����ȡStart�汾�ŵġ�SVNRevision.STARTҲû����ȡ������
     * @param localResource
     * @param svnMinRevisionNumber ���������
     * @param svnMaxRevisionNumber ��������
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
        
        //����һ���㷨:�� max - min <= ĳ��ֵʱ������һ�ַ���̽���£������ܼ���̽��������Ӷ��Ż����㷨������
        //������ļ�����ֱ�Ӷ�ȡ�޸���־ȡ�汾�ţ�������ļ��У�������־���ܻ�ǳ��࣬�����Ƚ��汾����С��threshold��Χ���ڣ���ȡ�汾��
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
