/**
 * Copyright 2020 Nokia
 * Licensed under the BSD 3-Clause Clear License.
 * SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package com.nokia.as.connector;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

import com.nokia.as.manager.MonitoringManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpConnector {
    private static final Logger logger = LoggerFactory.getLogger(HttpConnector.class);

    private static final int DEFAULT_REQUEST_TIMEOUT = 120;

    private HttpClient httpClient;
    private ExecutorService executorService;
    private SSLContext sslContext;
    private int timeout;

    public HttpConnector(ExecutorService executorService) {
        this(DEFAULT_REQUEST_TIMEOUT, executorService);
    }

    public HttpConnector(ExecutorService executorService, String proxyIp, int proxyPort) {
        this(DEFAULT_REQUEST_TIMEOUT, executorService, proxyIp, proxyPort);
    }

    public HttpConnector(int timeout, ExecutorService executorService, String proxyIp, int proxyPort) {
        this.timeout = timeout;
        this.executorService = executorService;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            httpClient = HttpClient.newBuilder()
                    .executor(this.executorService)
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .sslContext(sslContext)
                    .proxy(ProxySelector.of(
                            new InetSocketAddress(proxyIp, proxyPort)))
                    .build();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SSL Error: NoSuchAlgorithmException");
        } catch (KeyManagementException e) {
            logger.error("SSL Error: KeyManagementException");
        }
    }

    public HttpConnector(int timeout, ExecutorService executorService) {
        this.timeout = timeout;
        this.executorService = executorService;

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            httpClient = HttpClient.newBuilder()
                    .executor(this.executorService)
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .sslContext(sslContext)
                    .build();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SSL Error: NoSuchAlgorithmException");
        } catch (KeyManagementException e) {
            logger.error("SSL Error: KeyManagementException");
        }
    }

    public static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

    public HttpResponse<String> get(String url, Integer timeout) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeout))
                //.setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();

        HttpResponse<String> response;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // System.out.println(response.statusCode());
            // System.out.println(response.body());

            return response;
        } catch (IOException e) {
            logger.error("HTTP Client Error: IO Exception " + url);
        } catch (InterruptedException e) {
            logger.error("HTTP Client Error: Interrupted Exception " + url);
        }

        return null;
    }

    public HttpResponse<String> getAsync(URI uri, Integer timeout) {


        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                // .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();

        try {
            CompletableFuture<HttpResponse<String>> response =
                    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            return response.get(timeout, TimeUnit.SECONDS);
            // return response.thenApply(HttpResponse::body).get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("HTTP Client Error: Interrupted Exception " + uri.toString());
        } catch (ExecutionException e) {
            logger.error("HTTP Client Error: Execution Exception " + uri.toString());
        } catch (TimeoutException e) {
            logger.error("HTTP Client Error: Timeout Exception " + uri.toString());
        }

        return null;
    }

    public HttpResponse<String> getAsync(URI uri, Integer timeout, String login, String pwd) {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Authorization", basicAuth(login, pwd))
                .build();

        try {
            CompletableFuture<HttpResponse<String>> response =
                    httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            return response.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("HTTP Client Error: Interrupted Exception " + uri.toString());
        } catch (ExecutionException e) {
            logger.error("HTTP Client Error: Execution Exception " + uri.toString());
        } catch (TimeoutException e) {
            logger.error("HTTP Client Error: Timeout Exception " + uri.toString());
        }

        return null;
    }

    public static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }


    public void checkServices(MonitoringManager monitoringManager) {
        ConnectorList connectorList = monitoringManager.getGlobalConnectorList();
        connectorList.forEach(connector -> httpClient.sendAsync(
                connector.getRequest(),
                HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    connector.updateStatus(response);
                    monitoringManager.updateGauge(connector);
                    connector.addStep();
                    return connector;
                }));
    }

    public HttpResponse<String> get(String url) {
        return get(url, this.timeout);
    }

    public HttpResponse<String> post(String url) {
        Map<Object, Object> data = new HashMap<>();
        /*data.put("username", "abc");
        data.put("password", "123");
        data.put("custom", "secret");
        data.put("ts", System.currentTimeMillis());*/

        HttpRequest request = HttpRequest.newBuilder()
                .POST(buildFormDataFromMap(data))
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeout))
                //.setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                //.header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // System.out.println(response.statusCode());
            // System.out.println(response.body());

            return response;
        } catch (IOException e) {
            logger.error("REST Error: IO Exception " + url);
        } catch (InterruptedException e) {
            logger.error("REST Error: Interrupted Exception " + url);
        }

        return null;
    }

    private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        System.out.println(builder.toString());
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}
