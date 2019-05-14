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

import static com.taobao.eclipse.plugin.reviewboard.core.constant.ReviewBoardCoreConstants.CHARACTER_ENCODING;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.ParsingDetector;
import info.monitorenter.cpdetector.io.UnknownCharset;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * 类说明:计算文件或流的编码
 * 
 * 创建时间：2010-12-14
 */
public class GuessStreamEncoding {
    
    private static CodepageDetectorProxy codepageDetectorProxySingleton = null;
    
    private static Object objectSyn = new Object();
	
    private static CodepageDetectorProxy getCodepageDetectorProxy() {
        if( null == codepageDetectorProxySingleton ){
            synchronized(objectSyn) {  
              if( null == codepageDetectorProxySingleton ){
                  codepageDetectorProxySingleton = CodepageDetectorProxy.getInstance();
                  codepageDetectorProxySingleton.add(new ParsingDetector(false));    
                  codepageDetectorProxySingleton.add(info.monitorenter.cpdetector.io.JChardetFacade.getInstance());   
                  codepageDetectorProxySingleton.add(info.monitorenter.cpdetector.io.UnicodeDetector.getInstance());
              }
            }
        }
        return codepageDetectorProxySingleton;
    }

	/**
	 * 返回文件的编码格式
	 * @param file
	 * @return
	 */
	public static String getFileEncoding(File file) {
        Charset charset = null;     
        try {   
              charset = getCodepageDetectorProxy().detectCodepage(file.toURI().toURL());   
        } catch (Exception ex) {
            ex.printStackTrace();
        }   
        String charsetName = null;
        if( null == charset || charset instanceof UnknownCharset){
            charsetName = CHARACTER_ENCODING;
        }else{
            charsetName = charset.name();
        }
        //目前大部分代码，如果不是UTF-8，就一定是GBK
        if( !charsetName.trim().toUpperCase().equals(CHARACTER_ENCODING)){
            charsetName = "GBK";
        }
        return charsetName;
	}
	
    /**
     * 返回inputstream的编码
     * @param inputstream
     * @return
     */
    public static String getInputStreamEncoding(InputStream inputstream) {
        Charset charset = null;     
        try{
            charset = getCodepageDetectorProxy().detectCodepage(inputstream, 10000000);
        }catch (Exception ex) {
            ex.printStackTrace();
        }   
        String charsetName = null;
        if( null == charset || charset instanceof UnknownCharset ){
            charsetName = CHARACTER_ENCODING;
        }else{
            charsetName = charset.name();
        }
        //目前大部分代码，如果不是UTF-8，就一定是GBK
        if( !charsetName.trim().toUpperCase().equals(CHARACTER_ENCODING)){
            charsetName = "GBK";
        }
        return charsetName;
    }

}
