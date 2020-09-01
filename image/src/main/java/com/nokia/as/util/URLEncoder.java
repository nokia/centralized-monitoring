/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.util;

public class URLEncoder {

    public static String encode(String input) {
        return encode(input, " %$&+,:;@<>#%()\"");
    }

    public static String encodeParam(String input) {
        return encode(input, " %$&+,/:;=?@<>#%()\"");
    }

    public static String encode(String input, String charsToCheck) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch, charsToCheck)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch, String charsToCheck) {
        if (ch > 128)
            return true;
        return charsToCheck.indexOf(ch) >= 0;
    }
}
