package com.erp.system.common.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatRootHealthConfiguration {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> rootHealthEngineValve() {
        return factory -> factory.addEngineValves(new RootHealthEngineValve());
    }
}
