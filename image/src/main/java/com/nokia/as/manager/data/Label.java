/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.manager.data;

public class Label {

    private String name;
    private String value;
    private boolean mutable;

    public Label(String name, String value, boolean mutable) {
        this.name = name;
        this.value = value;
        this.mutable = mutable;
    }

    public Label(String name, String value) {
        this(name, value, false);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isMutable() {
        return mutable;
    }
}
