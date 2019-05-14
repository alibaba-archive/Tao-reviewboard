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
package com.taobao.eclipse.plugin.reviewboard.core.util;

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.BUFFER_SIZE_DEFAULT;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EMPTY_STRING;
import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.EOL;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

/**
 * ��˵��: �ַ�������Util
 * 
 * @author ���� 
 * ����ʱ�䣺2010-10-18
 */
public class IOUtils {


    /**
     * ��ȡURL���ݣ��ַ�����ʽ����
     * @param url
     * @param ����
     * @return
     */
    public static String getContentFromURL( URL url, String charset ){
        BufferedReader br = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            if(charset == null){
                br = new BufferedReader(new InputStreamReader(stream));
            }else{
                br = new BufferedReader(new InputStreamReader(stream,charset));
            }
            
            String line = null;
            StringBuilder sb  = new StringBuilder();
            while((line= br.readLine()) != null){
                sb.append( line );
                sb.append( EOL );
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    
     /**
     * ��ȡ�ļ����ݣ��ַ�����ʽ����
     * @param file
     * @param ����
     * @return
     */
    public static String getContentFromFile( File file, String charset ){
        BufferedReader br = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            if(charset == null){
                br = new BufferedReader(new InputStreamReader(stream));
            }else{
                br = new BufferedReader(new InputStreamReader(stream,charset));
            }
            
            String line = null;
            StringBuilder sb  = new StringBuilder();
            while((line= br.readLine()) != null){
                sb.append( line );
                sb.append( EOL );
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    /**
     * ��ȡ�ļ����ݣ��ַ�����ʽ����
     * @param filePath �ļ�·��
     * @return
     */
    public static String getContentFromFile(String filePath){
        File file = new File(filePath);
        if( !file.exists() ){
            return EMPTY_STRING;
        }
        OutputStream os = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE_DEFAULT];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer,0,length);                    
            }
        } catch(Exception e){
            ;
        } finally{
            try {
                is.close();
            } catch (Exception e) {
            }
            try {
                os.close();
            } catch (Exception e) {
            }
        }
        final ByteArrayOutputStream baos = (ByteArrayOutputStream)os;
        String fileContent = EMPTY_STRING;
        if(baos.size() != 0) {
            fileContent = baos.toString();
        }
        try {
            if( null != baos ){
                baos.close();
            }
        } catch (Exception e) {
        }
        return fileContent;
    }
    
    /**
     * �����ļ������������ļ�
     * @param fileContent �ļ�����
     * @param parentFolder �ļ���·��
     * @param filePath ��������·��
     * @param charset �ļ�����
     * @return
     */
    public static File saveFile(File parentFolder, String filePath, String fileContent, String charset){
        File file = new File( parentFolder, filePath );
        try {
            return saveFile( fileContent, file.getCanonicalPath(), charset );
        } catch (Exception e) {
            return saveFile( fileContent, file.getAbsolutePath(), charset );
        }
    }
    
    /**
     * �����ļ������������ļ�
     * @param fileContent �ļ�����
     * @param parentFolder �ļ���·��
     * @param filePath ��������·��
     * @param charset �ļ�����
     * @return
     */
    public static File saveFile(String fileFullPath, String fileContent, String charset){
        File file = new File( fileFullPath );
        saveFile(file, fileContent, charset);
        return file;
    }
    
    /**
     * �����ļ������������ļ�
     * @param fileContent �ļ�����
     * @param parentFolder �ļ���·��
     * @param filePath ��������·��
     * @param charset �ļ�����
     * @return
     */
    public static void saveFile(File file, String fileContent, String charset){
        PrintWriter pw = null;
        try {
            if(!file.exists()){
                File parent = file.getParentFile();
                if( !parent.exists() ){
                    parent.mkdirs();
                }
            }else{
                file.delete();
            }
            file.createNewFile();
            if (charset == null) {
                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file))), false);
            } else {
                pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file), charset)), false);
            }
            pw.print(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(pw != null){
                pw.close();
            }
        }
    }
    
    /**
     * ���ڽ���ת��Ϊ�ַ���
     * @param is
     * @return
     */
    public static String toString( InputStream  is ){
        OutputStream os = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[30000];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer,0,length);                    
            }
            final ByteArrayOutputStream baos = (ByteArrayOutputStream)os;
            if(baos.size() != 0) {
                return baos.toString();
            }
        } catch (IOException e) {
        }finally {
            try {
                os.close();
            } catch (IOException e) {
            }
        }
        return null;
    }
    
    /**
     * ���û�����ʱĿ¼���½�һ����ʱ�ļ�
     * @param tmpFolderName ��ʱ�ļ�Ŀ¼�µ��ĸ���Ŀ¼
     * @param tmpFileSuffix ��ʱ�ļ���׺
     * @return
     */
    public static File generateTmpFile( String tmpFolderName, String tmpFileSuffix ){
        if( !tmpFileSuffix.startsWith(".") ){
            tmpFileSuffix = "." + tmpFileSuffix;
        }
        String dirTmp = System.getProperty("java.io.tmpdir");
        File parentFolder = new File( dirTmp, tmpFolderName );
        if( !parentFolder.exists() ){
            parentFolder.mkdirs();
        }
        String tempDiffFileName = System.currentTimeMillis()+ getRandomNumber() + tmpFileSuffix;
        File fileDiff = new File( parentFolder, tempDiffFileName );
        return fileDiff;
    }
    
    /**
     * @return �����2λ����
     */
    private static int getRandomNumber(){
        int a = -1;
        int b = -1;
        java.util.Random rand=new java.util.Random();
        while( a <= 0)
            a= ( rand.nextInt()) % 9;
        while(b <= 0)
            b= ( rand.nextInt())% 9;
        String str = String.valueOf(a) + String.valueOf(b);
        return Integer.parseInt(str);
    }
}
