package com.alessio.soapconsumer.service;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ServicePoolFactory<T> extends  BasePooledObjectFactory<T> {

    private final IPooledService<T> pooledService;
    public ServicePoolFactory(IPooledService pooledService) {
        this.pooledService=pooledService;
    }

    @Override
    public T create() throws Exception {
        return pooledService.getPort();
    }

    @Override
    public PooledObject<T> wrap(T obj) {
        return new DefaultPooledObject(obj);
    }

}
