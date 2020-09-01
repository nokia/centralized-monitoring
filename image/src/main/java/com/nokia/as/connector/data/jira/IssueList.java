/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jira;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class IssueList extends ArrayList<Issue> {

    @Expose
    private String jqlFilter;

    public IssueList(String jqlFilter) {
        this.jqlFilter = jqlFilter;
    }
}
