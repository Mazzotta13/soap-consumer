package com.alessio.soapconsumer.service;

public interface IPooledService<T> {

    T getPort();

    void returnIfExists();

    T borrowPort() throws Exception;

    void invalidateIfExists();
}
