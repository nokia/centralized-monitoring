/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.main;

import com.nokia.as.main.jetty.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void startJettyServer() {
        App jettyServer = new App();
        Thread thread = new Thread(jettyServer);
        thread.start();
    }

    public static void main(String[] args) {
        try {
            logger.info("START");

            startJettyServer();

            App.monitoringManager.init();
            App.monitoringManager.run();

        } catch (Exception e) {
            logger.error("Exception in C-Monitoring tool build\n" + e);
        }

    }

}
