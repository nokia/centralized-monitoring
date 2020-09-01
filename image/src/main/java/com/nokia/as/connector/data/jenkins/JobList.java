/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jenkins;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class JobList extends ArrayList<Job> {

    @Expose
    private String directoryPath;

    public JobList(String directoryPath) {
        this.directoryPath = directoryPath;
    }
}
