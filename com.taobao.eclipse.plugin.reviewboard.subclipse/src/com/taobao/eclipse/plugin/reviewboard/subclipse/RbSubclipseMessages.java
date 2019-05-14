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
package com.taobao.eclipse.plugin.reviewboard.subclipse;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 *
 * @author 智清
 * 创建时间：2011-08-15
 */
public class RbSubclipseMessages {

	private static final String BUNDLE_NAME = ".rbMessage";

	private static ResourceBundle[] resources = null;

	private RbSubclipseMessages() {
	}

	public static String getString(String key) {
		init();
		for (int i = 0; i < resources.length; i++) {
			try {
				if (resources[i] != null){
					return resources[i].getString(key);
				}
			} catch (MissingResourceException e) {
			}
		}

		return '!' + key + '!';
	}

	private static synchronized void init() {
		if (resources == null) {
			Bundle mainPlugin = RbSubclipsePlugin.getPlugin().getBundle();
			Bundle[] fragments = Platform.getFragments(mainPlugin);

			if (fragments == null) {
				fragments = new Bundle[0];
			}

			resources = new ResourceBundle[fragments.length + 1];

			resources[0] = ResourceBundle.getBundle(mainPlugin.getSymbolicName() + BUNDLE_NAME);

			for (int i = 0; i < fragments.length; i++) {
				try {
					resources[i + 1] = ResourceBundle.getBundle(fragments[i].getSymbolicName() + BUNDLE_NAME);
				} catch (Exception ignored) {
					// ignore it
				}
			}
		}
	}

}
