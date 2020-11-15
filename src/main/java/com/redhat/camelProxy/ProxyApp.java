package com.redhat.camelProxy;


import io.netty.handler.logging.LoggingHandler;

import org.apache.camel.main.Main;

public class ProxyApp {
    public static void main(final String[] args) throws Exception {
        final Main main = new Main();
        launch(main);
    }

    static void launch(final Main main) throws Exception {
        try {
            main.bind("logging-handler", new LoggingHandler());
            main.addRouteBuilder(new ProxyRoute());
            main.run();
        } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
        } finally {
            main.stop();
        }
    }
}