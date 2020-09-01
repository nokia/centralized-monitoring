/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.main.jetty;

import com.nokia.as.connector.ConnectionSettings;
import com.nokia.as.connector.HttpConnector;
import com.nokia.as.manager.MonitoringManager;
import com.nokia.as.util.FileUtil;
import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Jetty App runtime
 */
public class App implements Runnable {
    public static final String CONFIG_FILE = "resources/config/config.json";
    public static final String CONNECTION_CONFIG_FILE = "resources/config/connectionSettings.json";

    public static final String API_ROOT = "/api";

    private Server jettyServer;
    public static final ExecutorService executorService = Executors.newFixedThreadPool(15);
    public static ConnectionSettings connectionSettings;
    private static HttpConnector httpClient;
    public static MonitoringManager monitoringManager;

    public App() {
        connectionSettings = ConnectionSettings.build(FileUtil.readFile(CONNECTION_CONFIG_FILE));
        httpClient = connectionSettings.isProxyEnabled() ?
                new HttpConnector(executorService,
                        connectionSettings.getProxyIp(),
                        connectionSettings.getProxyPort()) :
                new HttpConnector(executorService);
        monitoringManager = new MonitoringManager(httpClient);
        createServer();
    }

    public void createServer() {
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server();
        jettyServer.setHandler(context);

        ServerConnector connector = new ServerConnector(jettyServer);
        connector.setPort(JettyConfig.HTTP_PORT);

        if (JettyConfig.ENABLE_SSL) {
            HttpConfiguration https = new HttpConfiguration();
            https.addCustomizer(new SecureRequestCustomizer());

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(JettyConfig.KEYSTORE_FILE);
            sslContextFactory.setKeyStorePassword(JettyConfig.KEYSTORE_PWD);
            sslContextFactory.setKeyManagerPassword(JettyConfig.KEYSTORE_MANAGER_PWD);

            ServerConnector sslConnector = new ServerConnector(jettyServer,
                    new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https));
            sslConnector.setPort(JettyConfig.HTTPS_PORT);

            jettyServer.setConnectors(new ServerConnector[]{connector, sslConnector});
        } else {
            jettyServer.setConnectors(new ServerConnector[]{connector});
        }


        context.addServlet(
                new ServletHolder(
                        new MetricsServlet(monitoringManager.getMetricRegistry().getCollectorRegistry())),
                API_ROOT + "/metrics");

        ResourceConfig resourceConfig = new ResourceConfig(EntryPoint.class);
        resourceConfig.register(MultiPartFeature.class);
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        jerseyServlet.setInitOrder(0);
        context.addServlet(jerseyServlet, "/*");

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.classnames",
                EntryPoint.class.getCanonicalName());
    }

    public void run() {
        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jettyServer.destroy();
        }
    }
}
