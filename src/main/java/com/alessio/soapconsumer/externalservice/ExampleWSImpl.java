package com.alessio.soapconsumer.externalservice;

import com.alessio.service.Greeting;
import com.alessio.service.GreetingWsService;
import com.alessio.service.GreetingWsServiceImplService;
import com.alessio.soapconsumer.service.PooledService;
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import org.springframework.stereotype.Component;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.Map;

@Component
public class ExampleWSImpl extends PooledService<GreetingWsService> implements GreetingWsService {

    private ExampleWSConnectionConfig exampleWSConnectionConfig;

    public ExampleWSImpl(ExampleWSConnectionConfig exampleWSConnectionConfig) {
        super(exampleWSConnectionConfig.getGenericObjectPoolConfig(), exampleWSConnectionConfig.getAbandonedConfig());
        this.exampleWSConnectionConfig = exampleWSConnectionConfig;
    }

    @Override
    public GreetingWsService getPort() {
        GreetingWsService port = new GreetingWsServiceImplService().getGreetingWsServiceImplPort();

        Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
        requestContext.put(JAXWSProperties.SSL_SOCKET_FACTORY, exampleWSConnectionConfig.getSslSocketFactory());
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, exampleWSConnectionConfig.getEndpoint());
        requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, exampleWSConnectionConfig.getConnectionTimeout());
        requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, exampleWSConnectionConfig.getRequestTimeout());

        Binding binding = ((BindingProvider) port).getBinding();

        List<Handler> handlerList = binding.getHandlerChain();
//        handlerList.add(new OutgoingSoapHandler());

        if (exampleWSConnectionConfig.getWsSecurityHandlerWithProcessor() != null) {
            handlerList.add(exampleWSConnectionConfig.getWsSecurityHandlerWithProcessor());
        }

//        if (exampleWSConnectionConfig.getSoapLoggingInfoHandler() != null) {
//            handlerList.add(exampleWSConnectionConfig.getSoapLoggingInfoHandler());
//        }

        binding.setHandlerChain(handlerList);

        return port;
    }

    @Override
    public Greeting sayHello(String s) {
        GreetingWsService infoService;
        try {
            infoService = borrowPort();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return infoService.sayHello(s);
    }
}
