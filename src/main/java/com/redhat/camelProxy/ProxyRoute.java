/**
 *  Copyright 2005-2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.redhat.camelProxy;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ProxyRoute extends RouteBuilder {

    protected final static Logger LOG = LoggerFactory.getLogger(ProxyRoute.class);

    @Override
    public void configure() throws Exception {
        final RouteDefinition from, fromTLS;

        if (Files.exists(keystorePath())) {
            LOG.info("====> keystore.jks found.");
            fromTLS = from("netty4-http:proxy://0.0.0.0:8443?ssl=true&keyStoreFile=/tls/keystore.jks&passphrase=changeit&trustStoreFile=/tls/keystore.jks");
            createRoute(fromTLS);
        } else {
            LOG.warn("====> No keystore.jks found. Can't proxy HTTPS urls.");
        }

        from = from("netty4-http:proxy://0.0.0.0:8080");
        createRoute(from);

    }

    Path keystorePath() {
        return Paths.get("/tls","keystore.jks");
    }

    private static void addCustomHeader(final Exchange exchange) {
        final Message message = exchange.getIn();
        final String body = message.getBody(String.class);
        LOG.info("HEADERS before Proxying: " + message.getHeaders());

        message.setBody(body);
        LOG.info("Body:" + body);
        exchange.getOut().setHeaders(message.getHeaders());
        exchange.getOut().setHeader("Fuse-Camel-Proxy", "Request was redirected to Camel netty4 proxy service");

        LOG.info("HEADERS after Proxying: " + message.getHeaders());
        exchange.getOut().setBody(body);
    }

    private static void copyBody(final Exchange exchange) {
        final Message message = exchange.getIn();
        final String body = message.getBody(String.class);
        message.setBody(body);
    }

    private void createRoute(RouteDefinition route) {
        route
            .process(ProxyRoute::addCustomHeader)
            .toD("netty4-http:"
                    + "${headers." + Exchange.HTTP_SCHEME + "}://"
                    + "${headers." + Exchange.HTTP_HOST + "}:"
                    + "${headers." + Exchange.HTTP_PORT + "}"
                    + "${headers." + Exchange.HTTP_PATH + "}")
            .process(ProxyRoute::copyBody);
    }

}

