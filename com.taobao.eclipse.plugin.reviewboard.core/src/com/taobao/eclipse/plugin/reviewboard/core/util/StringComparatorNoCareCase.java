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

import java.util.Comparator;

/**
 * 
 * 类说明: String比较（不关心大小写）
 * 
 * @author 智清 
 *
 */
public class StringComparatorNoCareCase implements Comparator<String> {

    public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }
}
