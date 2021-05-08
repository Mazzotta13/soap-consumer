package com.alessio.soapconsumer.externalservice;

import com.alessio.soapconsumer.security.handler.WSSecurityHandlerWithProcessor;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.net.ssl.SSLSocketFactory;

public class ExampleWSConnectionConfig {
    private int connectionTimeout;
    private int requestTimeout;
    private SSLSocketFactory sslSocketFactory;
    private String endpoint;
    private GenericObjectPoolConfig genericObjectPoolConfig;
    private AbandonedConfig abandonedConfig;
    private WSSecurityHandlerWithProcessor wsSecurityHandlerWithProcessor;

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public GenericObjectPoolConfig getGenericObjectPoolConfig() {
        return genericObjectPoolConfig;
    }

    public void setGenericObjectPoolConfig(GenericObjectPoolConfig genericObjectPoolConfig) {
        this.genericObjectPoolConfig = genericObjectPoolConfig;
    }

    public AbandonedConfig getAbandonedConfig() {
        return abandonedConfig;
    }

    public void setAbandonedConfig(AbandonedConfig abandonedConfig) {
        this.abandonedConfig = abandonedConfig;
    }

    public WSSecurityHandlerWithProcessor getWsSecurityHandlerWithProcessor() {
        return wsSecurityHandlerWithProcessor;
    }

    public void setWsSecurityHandlerWithProcessor(WSSecurityHandlerWithProcessor wsSecurityHandlerWithProcessor) {
        this.wsSecurityHandlerWithProcessor = wsSecurityHandlerWithProcessor;
    }
}
