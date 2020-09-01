/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector.data.jenkins;

import javax.ws.rs.NotFoundException;

public enum BuildStatus {
    SUCCESS("success", 10d),
    INPROGRESS("inprogress", 9d),
    UNSTABLE("unstable", 5d),
    ABORTED("aborted", 3d),
    FAILURE("failure", 2d),
    DISABLED("disabled", 1d),
    NOTBUILT("notbuilt", 0d);

    private final String text;
    private final Double gaugeValue;

    BuildStatus(final String text, final Double gaugeValue) {
        this.text = text;
        this.gaugeValue = gaugeValue;
    }

    public String getValue() {
        return text;
    }

    public Double getGaugeValue() {
        return gaugeValue;
    }

    @Override
    public String toString() {
        return text;
    }

    public static BuildStatus fromString(String text) throws NotFoundException {
        for (BuildStatus e : BuildStatus.values()) {
            if (e.text.equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new NotFoundException("No BuildStatus corresponding to " + text);
    }
}