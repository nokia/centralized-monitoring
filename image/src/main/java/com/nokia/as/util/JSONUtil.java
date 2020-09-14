/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JSONUtil {
    private static final Logger logger = LoggerFactory.getLogger(JSONUtil.class);

    public static boolean isValidJSON(String s) {
        try {
            new JSONObject(s);
        } catch (JSONException ex) {
            try {
                new JSONArray(s);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkKeys(Map<String, Class> keys, JSONObject jsonObj, String jsonObjId, Boolean verbose) {
        String id = jsonObjId;
        if (id == null) {
            id = jsonObj.has("id") ? jsonObj.get("id").toString() : jsonObj.toString();
        }

        for (Object o : keys.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            String key = (String) pair.getKey();
            Class type = (Class) pair.getValue();

            if (!jsonObj.has(key)) {
                if (verbose) {
                    logger.error("JSON check keys error: the json object " + id + " isn't valid: " +
                            key + " field is missing");
                }
                return false;
            } else if (!jsonObj.get(key).getClass().equals(type)) {
                if (verbose) {
                    String[] splittedType = type.toString().split("\\.");
                    logger.error("JSON check keys error: the connector " + id + " isn't valid: " +
                            key + " field type must be " + splittedType[splittedType.length - 1]);
                }
                return false;
            }
        }
        return true;
    }

    public static boolean checkKeys(Map<String, Class> keys, JSONObject jsonObj, String jsonObjId) {
        return checkKeys(keys, jsonObj, jsonObjId, true);
    }

    public static boolean checkKeys(Map<String, Class> keys, JSONObject jsonObj) {
        return checkKeys(keys, jsonObj, null, true);
    }

    public static JSONObject cleanCredentialsJsonConfig(JSONObject config) {
        JSONObject cleanedJsonConfig = new JSONObject(config.toString());

        JSONArray cleanedJsonConfigConnectorGroups = cleanedJsonConfig.getJSONArray("connectorGroups");

        for (int i = 0; i < cleanedJsonConfigConnectorGroups.length(); i++) {
            JSONArray connectors = cleanedJsonConfigConnectorGroups.getJSONObject(i)
                    .getJSONArray("connectors");
            for (int j = 0; j < connectors.length(); j++) {
                JSONObject connector = connectors.getJSONObject(j);
                if (connector.has("authentication")) {
                    JSONObject authentication = connector.getJSONObject("authentication");
                    authentication.put("login", "*****");
                    authentication.put("token", "*****");
                }
            }
        }

        return cleanedJsonConfig;
    }

}
