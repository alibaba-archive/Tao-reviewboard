/*******************************************************************************
 * Copyright (c) 2004 - 2009 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylyn project committers, Atlassian, Sven Krzyzak
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2009 Markus Knittig
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *     Markus Knittig - adapted Trac, Redmine & Atlassian implementations for
 *                      Review Board
 *******************************************************************************/
package com.taobao.eclipse.plugin.reviewboard.core.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.taobao.eclipse.plugin.reviewboard.core.model.ModelRepository;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelReviewGroup;
import com.taobao.eclipse.plugin.reviewboard.core.model.ModelUser;

/**
 *
 * @author Markus Knittig
 * @author ÖÇÇå
 */
public class ReviewboardClientData implements Serializable{

    private static final long serialVersionUID = 3937173234234910552L;

    private final List<ModelUser> userList = new ArrayList<ModelUser>();
    
    private final List<ModelReviewGroup> reviewGroupList = new ArrayList<ModelReviewGroup>();
    
    private final List<ModelRepository> repositoryList = new ArrayList<ModelRepository>();

    public long lastupdate = 0;

    public List<ModelUser> getUserList() {
        return userList;
    }

    public List<ModelReviewGroup> getReviewGroupList() {
        return reviewGroupList;
    }

    public List<ModelRepository> getRepositoryList() {
        return repositoryList;
    }
}
