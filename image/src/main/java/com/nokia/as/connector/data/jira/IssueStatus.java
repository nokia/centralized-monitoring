/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jira;

import javax.ws.rs.NotFoundException;

public enum IssueStatus {
    OPEN("open", 1, "Open", 1, 1d),
    TODO("todo", 10119, "To Do", 1, 2d),
    DRAFT("draft", 10219, "Draft", 1, 3d),

    INPROGRESS("inprogress", 3, "In Progress", 2, 50d),
    INREVIEW("inreview", 10280, "In Review", 2, 51d),
    UNDERREVIEW("underreview", 10041, "Under Review", 2, 52d),
    REJECTED("rejected", 10008, "Rejected", 2, 53d),
    PUBLISHED("published", 15629, "Published", 2, 54d),
    ACCEPTED("accepted", 10071, "Accepted", 2, 55d),
    NEEDSINFORMATION("needsinformation", 34124, "Needs Information", 2, 56d),
    INANALYSIS("inanalysis", 10063, "In Analysis", 2, 57d),
    ONHOLD("onhold", 10060, "On Hold", 2, 58d),
    NEW("new", 10044, "New", 2, 59d),
    REOPENED("reopened", 4, "Reopened", 2, 60d),

    DONE("done", 10046, "Done", 3, 100d),
    APPROVED("approved", 10007, "Approved", 3, 101d),
    CANCELLED("cancelled", 10019, "Cancelled", 3, 102d),
    CLOSED("closed", 6, "Closed", 3, 103d),
    RESOLVED("resolved", 5, "Resolved", 3, 104d);

    private final String text;
    private final Integer id;
    private final String name;
    private final Integer category;
    private final Double gaugeValue;

    IssueStatus(final String text,
                final Integer id,
                final String name,
                final Integer category,
                final Double gaugeValue) {
        this.text = text;
        this.id = id;
        this.name = name;
        this.category = category;
        this.gaugeValue = gaugeValue;
    }

    public String getValue() {
        return text;
    }

    public Double getGaugeValue() {
        return gaugeValue;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return text;
    }

    public static IssueStatus fromString(String text) throws NotFoundException {
        for (IssueStatus e : IssueStatus.values()) {
            if (e.text.equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new NotFoundException("No IssueStatus corresponding to " + text);
    }

    public static IssueStatus fromId(Integer id) throws NotFoundException {
        for (IssueStatus e : IssueStatus.values()) {
            if (e.id.equals(id)) {
                return e;
            }
        }
        throw new NotFoundException("No IssueStatus corresponding to " + id);
    }
}