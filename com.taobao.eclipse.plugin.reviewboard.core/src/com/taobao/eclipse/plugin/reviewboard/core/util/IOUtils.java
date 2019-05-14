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
 * 类说明: 字符操作的Util
 * 
 * @author 智清 
 * 创建时间：2010-10-18
 */
public class IOUtils {


    /**
     * 读取URL内容，字符串形式返回
     * @param url
     * @param 编码
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
     * 读取文件内容，字符串形式返回
     * @param file
     * @param 编码
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
     * 读取文件内容，字符串形式返回
     * @param filePath 文件路径
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
     * 根据文件内容新生成文件
     * @param fileContent 文件内容
     * @param parentFolder 文件夹路径
     * @param filePath 建立的子路径
     * @param charset 文件编码
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
     * 根据文件内容新生成文件
     * @param fileContent 文件内容
     * @param parentFolder 文件夹路径
     * @param filePath 建立的子路径
     * @param charset 文件编码
     * @return
     */
    public static File saveFile(String fileFullPath, String fileContent, String charset){
        File file = new File( fileFullPath );
        saveFile(file, fileContent, charset);
        return file;
    }
    
    /**
     * 根据文件内容新生成文件
     * @param fileContent 文件内容
     * @param parentFolder 文件夹路径
     * @param filePath 建立的子路径
     * @param charset 文件编码
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
     * 用于将流转换为字符串
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
     * 在用户的临时目录下新建一个临时文件
     * @param tmpFolderName 临时文件目录下的哪个子目录
     * @param tmpFileSuffix 临时文件后缀
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
     * @return 随机的2位数字
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
