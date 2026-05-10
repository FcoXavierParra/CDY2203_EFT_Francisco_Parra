package com.duoc.backend;

import java.util.List;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;

@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Value("${http.port:8080}")
    private int httpPort;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if (factory instanceof TomcatServletWebServerFactory tomcatFactory) {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setSecure(false);
            connector.setPort(httpPort);
            tomcatFactory.setAdditionalConnectors(List.of(connector));
        }
    }
}
