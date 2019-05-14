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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EOL;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * ��˵��:diff�Ĳ�����Util�����ڲ��Diff���ݡ�
 * 
 * @author ���� 
 * ����ʱ�䣺2011-4-12
 */
public final class RbSVNDiffSplitUtils {

    private RbSVNDiffSplitUtils() {
        super();
    }
    
    /**
     * ��һ��diff�ı����ղ�ͬ�ļ����в��
     * 
     * @param diffContentAll ����������
     * @param pathList �ļ�·�����ļ���
     * @return key���ļ�·�����ļ�����value:diff����
     */
    public static Map<String,String> splitDiff(String diffContentAll, List<String> pathList){
        if(diffContentAll == null || diffContentAll.isEmpty() 
                || pathList == null || pathList.isEmpty()){
           return null;
        }
        Map<String,String> targetMap = new LinkedHashMap<String,String>();
        if( pathList.size() == 1 ){
            targetMap.put(pathList.get(0), diffContentAll);
            return targetMap;
        }
        
        String tempString = diffContentAll;
        Map<Integer,String> diffPathLocationMap = new TreeMap<Integer,String>();
        for(String key : pathList){
           int startLocation = tempString.indexOf("Index: " + key);
           if( startLocation == -1 ){
               continue;
           }
           diffPathLocationMap.put(startLocation, "Index: " + key);
        }
        
        Set<Integer> locationSet = diffPathLocationMap.keySet();
        int preStartLocation = -1;
        int nextStartLocation = -1;
        Iterator<Integer> locationIt = locationSet.iterator();
        while( locationIt.hasNext() ){
            Integer currentLocactionTmp = (Integer)locationIt.next();
            if( preStartLocation == -1 ){
                preStartLocation = currentLocactionTmp;
                nextStartLocation = currentLocactionTmp;
            }else{
                nextStartLocation = currentLocactionTmp;
                targetMap.put(diffPathLocationMap.get(preStartLocation), 
                        diffContentAll.substring(preStartLocation, nextStartLocation));
            }
            
            if( locationIt.hasNext() ){
                preStartLocation = nextStartLocation;
            }else{//�Ѿ������һ����
                targetMap.put(diffPathLocationMap.get(nextStartLocation), 
                        diffContentAll.substring(nextStartLocation, tempString.length()));
            }
        }
        
        return targetMap;
    }

    /**
     * ���˳��ļ�������Ҫ���diff
     * @param diffPathToContentMap
     * @param careCaseUpperOrLower �Ƿ��ע��Сд
     * @return
     */
    public static void filterDiff( Map<String,String> diffPathToContentMap, 
            String pathFilterMatch, boolean careCaseUpperOrLower ){
        
        if(  null == diffPathToContentMap || diffPathToContentMap.isEmpty() ){
            return ;
        }
        Set<String> diffPathSet = diffPathToContentMap.keySet();
        Set<String> diffPathSetDeleter = new HashSet<String>();
        for(String diffPathTmp : diffPathSet){
            boolean isMatched = true;
            try {
                String[] diffPaths = diffPathTmp.split("/");
                if( diffPaths.length == 0 ){
                    continue;
                }
                String fileName = diffPaths[diffPaths.length - 1];
                if( careCaseUpperOrLower ){
                    isMatched = fileName.matches(pathFilterMatch);
                }else{
                    isMatched = fileName.toLowerCase().matches(pathFilterMatch.toLowerCase());
                }
            } catch (Exception e) {
            }
            if( !isMatched ){
                diffPathSetDeleter.add(diffPathTmp);
            }
        }
        for(String diffPathTmp : diffPathSetDeleter){
            diffPathToContentMap.remove(diffPathTmp);
        }
        
        return ;
    }
    
    /**
     * ��������diff����
     * @param diffPathToContentMap
     * @return
     */
    public static String integrateDiff( Map<String,String> diffPathToContentMap ){
        StringBuilder diffContentSB = new StringBuilder();
        
        if(  null == diffPathToContentMap || diffPathToContentMap.isEmpty() ){
            return diffContentSB.toString();
        }
        Set<String> diffPathSet = diffPathToContentMap.keySet();
        for(String diffPathTmp : diffPathSet){
            String diffContentTmp = diffPathToContentMap.get(diffPathTmp);
            if( null != diffContentTmp ){
                diffContentSB.append( diffContentTmp );
                if( !diffContentTmp.endsWith( EOL )){
                    diffContentSB.append( EOL );
                }
            }
        }
        
        return diffContentSB.toString();
    }
    
}
