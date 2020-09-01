/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector;

import javax.ws.rs.NotFoundException;

public enum ConnectorStatus {
    UP("up", 5d),
    WARNING("warning", 6d),
    READY("ready", 10d),
    ERROR("error", 1d),
    DOWN("down", 0d);

    private final String text;
    private final Double gaugeValue;

    ConnectorStatus(final String text, final Double gaugeValue) {
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

    public static ConnectorStatus fromString(String text) throws NotFoundException {
        for (ConnectorStatus e : ConnectorStatus.values()) {
            if (e.text.equalsIgnoreCase(text)) {
                return e;
            }
        }
        throw new NotFoundException("No ConnectorStatus corresponding to " + text);
    }
}