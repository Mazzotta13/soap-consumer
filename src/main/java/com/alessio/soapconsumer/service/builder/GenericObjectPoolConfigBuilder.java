package com.alessio.soapconsumer.service.builder;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class GenericObjectPoolConfigBuilder {

    private static final Integer connectionPoolMaxTotal_DEFAULT = 100;
    private static final Integer connectionPoolMaxIdle_DEFAULT = 5;
    private static final Integer connectionPoolMinIdle_DEFAULT = 5;

    private Integer connectionPoolMaxTotal;
    private Integer connectionPoolMaxIdle;
    private Integer connectionPoolMinIdle;
    private boolean enableJMX;
    private String jmxName;

    public GenericObjectPoolConfigBuilder() {
    }

    public GenericObjectPoolConfigBuilder(Integer connectionPoolMaxTotal, Integer connectionPoolMaxIdle, Integer connectionPoolMinIdle) {
        this.connectionPoolMaxTotal = connectionPoolMaxTotal;
        this.connectionPoolMaxIdle = connectionPoolMaxIdle;
        this.connectionPoolMinIdle = connectionPoolMinIdle;
    }

    public GenericObjectPoolConfigBuilder(Integer connectionPoolMaxTotal, Integer connectionPoolMaxIdle, Integer connectionPoolMinIdle, boolean enableJMX, String jmxName) {
        this.connectionPoolMaxTotal = connectionPoolMaxTotal;
        this.connectionPoolMaxIdle = connectionPoolMaxIdle;
        this.connectionPoolMinIdle = connectionPoolMinIdle;
        this.enableJMX = enableJMX;
        this.jmxName = jmxName;
    }

    public GenericObjectPoolConfig createStandardGenericObjectPoolConfig() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        if (connectionPoolMaxTotal == null || connectionPoolMaxTotal == 0) {
            connectionPoolMaxTotal = connectionPoolMaxTotal_DEFAULT;
        }
        genericObjectPoolConfig.setMaxTotal(connectionPoolMaxTotal);

        if (connectionPoolMaxIdle == null || connectionPoolMaxIdle == 0) {
            connectionPoolMaxIdle = connectionPoolMaxIdle_DEFAULT;
        }
        genericObjectPoolConfig.setMaxIdle(connectionPoolMaxIdle);
        if (connectionPoolMinIdle == null || connectionPoolMinIdle == 0) {
            connectionPoolMinIdle = connectionPoolMinIdle_DEFAULT;
        }
        genericObjectPoolConfig.setMinIdle(connectionPoolMinIdle);
        genericObjectPoolConfig.setBlockWhenExhausted(false); //no queue
        genericObjectPoolConfig.setMinEvictableIdleTimeMillis(-1); //only soft condition
        //Soft condition: An idle object is destroyed if it has been idle for more than softMinEvictableIdleTimeMillis
        //and more than minIdle objects are inside the pool
        genericObjectPoolConfig.setSoftMinEvictableIdleTimeMillis((long) 1000 * 60 * 5); //5 minutes
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis((long) 1000 * 60); //idle object evitor runs every 1 minute
        genericObjectPoolConfig.setNumTestsPerEvictionRun(100); //min(minIdle, 100) objects are tested if they must be destroyed by the idle object evictor thread
        return genericObjectPoolConfig;
    }

    public GenericObjectPoolConfig buildGenericObjectPoolConfig() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        if (connectionPoolMaxTotal == null || connectionPoolMaxTotal == 0) {
            throw new RuntimeException("Missing configuration");
        }
        genericObjectPoolConfig.setMaxTotal(connectionPoolMaxTotal);

        if (connectionPoolMaxIdle == null || connectionPoolMaxIdle == 0) {
            throw new RuntimeException("Missing configuration");
        }
        genericObjectPoolConfig.setMaxIdle(connectionPoolMaxIdle);
        if (connectionPoolMinIdle == null || connectionPoolMinIdle == 0) {
            throw new RuntimeException("Missing configuration");
        }
        genericObjectPoolConfig.setMinIdle(connectionPoolMinIdle);
        genericObjectPoolConfig.setBlockWhenExhausted(false); //no queue
        genericObjectPoolConfig.setMinEvictableIdleTimeMillis(-1); //only soft condition
        //Soft condition: An idle object is destroyed if it has been idle for more than softMinEvictableIdleTimeMillis
        //and more than minIdle objects are inside the pool
        genericObjectPoolConfig.setSoftMinEvictableIdleTimeMillis((long) 1000 * 60 * 5); //5 minutes
        genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis((long) 1000 * 60); //idle object evitor runs every 1 minute
        genericObjectPoolConfig.setNumTestsPerEvictionRun(100); //min(minIdle, 100) objects are tested if they must be destroyed by the idle object evictor thread
        if (enableJMX) {
            genericObjectPoolConfig.setJmxNamePrefix(jmxName);
            genericObjectPoolConfig.setJmxEnabled(true);
        }
        return genericObjectPoolConfig;
    }

    public GenericObjectPoolConfigBuilder withConnectionPoolMaxTotal(final Integer connectionPoolMaxTotal) {
        setConnectionPoolMaxTotal(connectionPoolMaxTotal);
        return this;
    }

    public GenericObjectPoolConfigBuilder withConnectionPoolMaxIdle(final Integer connectionPoolMaxIdle) {
        setConnectionPoolMaxIdle(connectionPoolMaxIdle);
        return this;
    }

    public GenericObjectPoolConfigBuilder withConnectionPoolMinIdle(final Integer connectionPoolMinIdle) {
        setConnectionPoolMinIdle(connectionPoolMinIdle);
        return this;
    }

    public GenericObjectPoolConfigBuilder withEnableJMX(final boolean enableJMX) {
        setEnableJMX(enableJMX);
        return this;
    }

    public GenericObjectPoolConfigBuilder withJmxName(final String jmxName) {
        setJmxName(jmxName);
        return this;
    }

    public Integer getConnectionPoolMaxTotal() {
        return connectionPoolMaxTotal;
    }

    public void setConnectionPoolMaxTotal(Integer connectionPoolMaxTotal) {
        this.connectionPoolMaxTotal = connectionPoolMaxTotal;
    }

    public Integer getConnectionPoolMaxIdle() {
        return connectionPoolMaxIdle;
    }

    public void setConnectionPoolMaxIdle(Integer connectionPoolMaxIdle) {
        this.connectionPoolMaxIdle = connectionPoolMaxIdle;
    }

    public Integer getConnectionPoolMinIdle() {
        return connectionPoolMinIdle;
    }

    public void setConnectionPoolMinIdle(Integer connectionPoolMinIdle) {
        this.connectionPoolMinIdle = connectionPoolMinIdle;
    }

    public boolean isEnableJMX() {
        return enableJMX;
    }

    public void setEnableJMX(boolean enableJMX) {
        this.enableJMX = enableJMX;
    }

    public String getJmxName() {
        return jmxName;
    }

    public void setJmxName(String jmxName) {
        this.jmxName = jmxName;
    }
}
