package com.alessio.soapconsumer.service;

import com.alessio.soapconsumer.service.aspect.NoPoolAspect;
import com.alessio.soapconsumer.service.builder.GenericObjectPoolConfigBuilder;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PooledService<T> implements IPooledService<T> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private GenericObjectPool<T> pool;

    private final Map<Long, T> borrowmap = new ConcurrentHashMap<>();

    public PooledService(GenericObjectPoolConfig genericObjectPoolConfig, AbandonedConfig abandonedConfig) {
        pool = new GenericObjectPool<>(new ServicePoolFactory<T>(this), genericObjectPoolConfig, abandonedConfig);
    }

    public PooledService(String name, GenericObjectPoolConfigBuilder genericObjectPoolConfigBuilder, AbandonedConfig abandonedConfig){

        if(StringUtils.hasText(name)){
            genericObjectPoolConfigBuilder.setEnableJMX(true);
            genericObjectPoolConfigBuilder.setJmxName(name);
        }
        GenericObjectPoolConfig genericObjectPoolConfig = genericObjectPoolConfigBuilder.buildGenericObjectPoolConfig();
        pool = new GenericObjectPool<>(new ServicePoolFactory<T>(this), genericObjectPoolConfig, abandonedConfig);
    }

    @NoPoolAspect
    @Override
    public final T borrowPort() throws Exception {
        Thread currentThread = Thread.currentThread();
        long id = currentThread.getId();
        if (borrowmap.containsKey(id)) {
            return borrowmap.get(id);
        } else {
            T port = pool.borrowObject();
            borrowmap.put(id, port);
            return port;
        }
    }

    @NoPoolAspect
    @Override
    public final void returnIfExists() {
        Thread currentThread = Thread.currentThread();
        long id = currentThread.getId();
        if (borrowmap.containsKey(id)) {
            pool.returnObject(borrowmap.get(id));
            borrowmap.remove(id);
        }
    }

    @NoPoolAspect
    @Override
    public final void invalidateIfExists() {
        Thread currentThread = Thread.currentThread();
        long id = currentThread.getId();
        if (borrowmap.containsKey(id)) {
            try {
                pool.invalidateObject(borrowmap.get(id));
            } catch (Exception e) {
                logger.error("Error while abandoning object", e);
            }
            borrowmap.remove(id);
        }
    }

    @NoPoolAspect
    public final long getCurrentUsedCount() {
        return pool.getNumActive();
    }

}
