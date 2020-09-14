/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector;

import javax.ws.rs.NotFoundException;

public enum ConnectorType {
    JENKINS("jenkins"),
    GERRIT("gerrit"),
    GITLAB("gitlab"),
    GITHUB("github"),
    CONFLUENCE("confluence"),
    JIRA("jira"),
    ARTIFACTORY("artifactory"),
    OPENSTACK("openstack");

    private final String text;

    ConnectorType(final String text) {
        this.text = text;
    }

    public String getValue() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static ConnectorType fromString(String text) throws NotFoundException {
        for (ConnectorType e : ConnectorType.values()) {
            if (e.text.equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new NotFoundException("No ConnectorType corresponding to " + text);
    }
}