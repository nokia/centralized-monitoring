/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jira;

import com.nokia.as.manager.data.GaugeMetric;

public class User {

    private String csl;
    private Integer nbIssues;
    private GaugeMetric nbIssuesGaugeMetric;
    private Integer nbTodayIssues;
    private GaugeMetric nbTodayIssuesGaugeMetric;

    public User(String csl) {
        this.csl = csl;
        this.nbIssues = 0;
        this.nbTodayIssues = 0;
    }

    public String getCsl() {
        return csl;
    }

    public Integer getNbIssues() {
        return nbIssues;
    }

    public void setNbIssues(Integer nbIssues) {
        this.nbIssues = nbIssues;
    }

    public Integer getNbTodayIssues() {
        return nbTodayIssues;
    }

    public void setNbTodayIssues(Integer nbTodayIssues) {
        this.nbTodayIssues = nbTodayIssues;
    }

    public void incrementNbIssues() {
        nbIssues++;
    }

    public void incrementNbTodayIssues() {
        nbTodayIssues++;
    }

    public void resetNbIssues() {
        nbIssues = 0;
    }

    public void resetNbTodayIssues() {
        nbTodayIssues = 0;
    }

    public GaugeMetric getNbIssuesGaugeMetric() {
        return nbIssuesGaugeMetric;
    }

    public GaugeMetric getNbTodayIssuesGaugeMetric() {
        return nbTodayIssuesGaugeMetric;
    }

    public void setNbIssuesGauge(GaugeMetric nbIssuesGaugeMetric) {
        this.nbIssuesGaugeMetric = nbIssuesGaugeMetric;
    }

    public void setNbTodayIssuesGaugeMetric(GaugeMetric nbTodayIssuesGaugeMetric) {
        this.nbTodayIssuesGaugeMetric = nbTodayIssuesGaugeMetric;
    }
}
