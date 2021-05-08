package com.alessio.soapconsumer.externalservice;

import com.alessio.service.GreetingWsService;
import com.alessio.soapconsumer.security.handler.WSSecurityHandlerWithProcessor;
import com.alessio.soapconsumer.security.handler.processor.SoapMessageProcessor;
import com.alessio.soapconsumer.security.handler.processor.outgoing.SignatureProcessor;
import com.alessio.soapconsumer.security.model.KeyIdentifierType;
import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
import com.alessio.soapconsumer.security.utils.SSLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ExampleWsConfig {

    private static Logger LOGGER = LoggerFactory.getLogger(ExampleWsConfig.class);

    public static final String SBPS_AUTHENTICATOR_SSL_SOCKET_FACTORY_BEAN_NAME = "sbpsAuthenticatorSSLSocketFactory";
    public static final String SBPS_AUTHENTICATOR_AUTHENTICATION_WS_CONFIG_BEAN_NAME = "sbpsAuthenticatorAuthenticationWsConfigBean";
    public static final String SBPS_AUTHENTICATOR_WS_GENERIC_OBJECT_POOL_CONFIG_BEAN_NAME = "sbpsAuthenticatorWsGenericObjectPoolConfigBean";
    public static final String SBPS_AUTHENTICATOR_WS_ABANDONED_CONFIG_BEAN_NAME = "sbpsAuthenticatorWsAbandonedConfigBean";
    private static final String SBPS_AUTHENTICATOR_WS_SIGNATURE_KEYSTORE_BEAN_NAME = "sbpsAuthenticatorWsSignatureKeystoreBean";
    private static final String SBPS_AUTHENTICATOR_WS_WS_SECURITY_HANDLER_BEAN_NAME = "sbpsAuthenticatorWsWsSecurityHandlerBean";

    public static final String SBPS_AUTHENTICATOR_AUTHENTICATION_WS_IMPL_BIN = "sbpsAuthenticatorAuthenticationWsImpl";

    @Value("${examplews.endpoint}")
    private String endpointExampleWs;

    @Value("${examplews.logInterceptorEnabled:false}")
    private boolean logInterceptorEnabled;

    @Value("${examplews.connectionPool.maxTotal}")
    private Integer connectionPoolMaxTotal;
    @Value("${examplews.connectionPool.maxIdle}")
    private Integer connectionPoolMaxIdle;
    @Value("${examplews.connectionPool.minIdle}")
    private Integer connectionPoolMinIdle;
    @Value("${examplews.connectionPool.removeAbandonedTimeout}")
    private Integer connectionRemoveAbandonedTimeout;

    @Value("${examplews.timeout.connection}")
    private int connectionTimeout;
    @Value("${examplews.timeout.request}")
    private int requestTimeout;

    @Value("#{'${examplews.ssl.protocol}'.split(',')}")
    private List<String> sslProtocolList;
    @Value("#{'${examplews.ssl.ciphersuite}'.split(',')}")
    private List<String> sslCipherSuiteList;

    @Value("${examplews.acstoken.ssl.keystore.path:}")
    private String sslKeyStorePath;
    @Value("${examplews.acstoken.ssl.keystore.password:}")
    private String sslKeyStorePassword;
    @Value("${examplews.ssl.keystore.type:jks}")
    private String sslKeyStoreType;
    @Value("${examplews.ssl.keystore.alias:}")
    private String sslKeyStoreAlias;

    @Value("${examplews.ssl.truststore.path:}")
    private String sslTrustStorePath;
    @Value("${examplews.ssl.truststore.password:}")
    private String sslTrustStorePassword;
    @Value("${examplews.ssl.truststore.type:jks}")
    private String sslTrustStoreType;

    @Value("${examplews.wssecurity.signature.keystore.path:}")
    private String signatureKeystorePath;
    @Value("${examplews.wssecurity.signature.keystore.password:}")
    private String signatureKeystorePassword;
    @Value("${examplews.wssecurity.signature.keystore.alias:}")
    private String signatureKeystoreAlias;
    @Value("${examplews.wssecurity.signature.keystore.type:jks}")
    private String signatureKeystoreType;

    @Bean(name = SBPS_AUTHENTICATOR_SSL_SOCKET_FACTORY_BEAN_NAME)
    public SSLSocketFactory getSSLSocketFactory() {
        try {
            KeystoreInfo sslKeystoreInfo = null;
            if (StringUtils.isNotBlank(sslKeyStorePath)) {
                sslKeystoreInfo =
                        new KeystoreInfo(
                                sslKeyStorePath,
                                sslKeyStorePassword,
                                sslKeyStoreType,
                                sslKeyStoreAlias);
            }

            KeystoreInfo sslTruststoreInfo = null;
            if (StringUtils.isNotBlank(sslTrustStorePath)) {
                sslTruststoreInfo =
                        new KeystoreInfo(
                                sslTrustStorePath,
                                sslTrustStorePassword,
                                sslTrustStoreType);
            }

            return SSLUtils.getSSLSocketFactory(sslKeystoreInfo, sslTruststoreInfo, sslCipherSuiteList, sslProtocolList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean(SBPS_AUTHENTICATOR_WS_SIGNATURE_KEYSTORE_BEAN_NAME)
    public KeystoreInfo getSignatureKeystore() {

        return new KeystoreInfo(
                signatureKeystorePath,
                signatureKeystorePassword,
                signatureKeystoreType,
                signatureKeystoreAlias,
                signatureKeystorePassword);
    }

    @Bean(SBPS_AUTHENTICATOR_WS_WS_SECURITY_HANDLER_BEAN_NAME)
    public WSSecurityHandlerWithProcessor getWsSecurityHandler(
            @Qualifier(SBPS_AUTHENTICATOR_WS_SIGNATURE_KEYSTORE_BEAN_NAME) KeystoreInfo outgoingSignatureKeystore) {
        //Definizione processor per aggiungere firma applicativa e cifratura in uscita
        ArrayList<SoapMessageProcessor> outgoingMessageProcessors = new ArrayList<>();

        //Processor per aggiungere firma applicativa in uscita
        //Il certificato del client con cui il client effettua la firma applicativa viene qui specificato in modalità "Reference to a Binary Security Token"
        outgoingMessageProcessors.add(
                new SignatureProcessor(outgoingSignatureKeystore)
                        .withKeyIdentifierType(KeyIdentifierType.BINARY_SECURITY_TOKEN_REFERENCE));


        return new WSSecurityHandlerWithProcessor()
                .withOutgoingMessageProcessors(outgoingMessageProcessors);

    }

    @Bean(SBPS_AUTHENTICATOR_WS_GENERIC_OBJECT_POOL_CONFIG_BEAN_NAME)
    public GenericObjectPoolConfig getGenericObjectPoolConfig() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxTotal(connectionPoolMaxTotal);
        genericObjectPoolConfig.setMaxIdle(connectionPoolMaxIdle);
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

    @Bean(SBPS_AUTHENTICATOR_WS_ABANDONED_CONFIG_BEAN_NAME)
    public AbandonedConfig abandonedConfig() {
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setRemoveAbandonedTimeout(connectionRemoveAbandonedTimeout);
        abandonedConfig.setRemoveAbandonedOnBorrow(true);
        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
        return abandonedConfig;
    }

    @Bean(SBPS_AUTHENTICATOR_AUTHENTICATION_WS_CONFIG_BEAN_NAME)
    public ExampleWSConnectionConfig getExampleWSConnectionConfig(
            @Qualifier(SBPS_AUTHENTICATOR_SSL_SOCKET_FACTORY_BEAN_NAME) SSLSocketFactory sslSocketFactory,
            @Qualifier(SBPS_AUTHENTICATOR_WS_GENERIC_OBJECT_POOL_CONFIG_BEAN_NAME) GenericObjectPoolConfig genericObjectPoolConfig,
            @Qualifier(SBPS_AUTHENTICATOR_WS_ABANDONED_CONFIG_BEAN_NAME) AbandonedConfig abandonedConfig,
            @Qualifier(SBPS_AUTHENTICATOR_WS_WS_SECURITY_HANDLER_BEAN_NAME) WSSecurityHandlerWithProcessor wsSecurityHandlerWithProcessor) {
        ExampleWSConnectionConfig exampleWSConnectionConfig = new ExampleWSConnectionConfig();
        exampleWSConnectionConfig.setConnectionTimeout(connectionTimeout);
        exampleWSConnectionConfig.setRequestTimeout(requestTimeout);
        exampleWSConnectionConfig.setSslSocketFactory(sslSocketFactory);
        exampleWSConnectionConfig.setEndpoint(endpointExampleWs);
        exampleWSConnectionConfig.setGenericObjectPoolConfig(genericObjectPoolConfig);
        exampleWSConnectionConfig.setAbandonedConfig(abandonedConfig);
        // remove comment to activate sign
        exampleWSConnectionConfig.setWsSecurityHandlerWithProcessor(wsSecurityHandlerWithProcessor);

//        if (logInterceptorEnabled) {
//            exampleWSConnectionConfig.setSoapLoggingInfoHandler(new SOAPLoggingInfoHandler());
//        }
        return exampleWSConnectionConfig;
    }

    @Bean(SBPS_AUTHENTICATOR_AUTHENTICATION_WS_IMPL_BIN)
    public GreetingWsService getAuthenticationWs(
            @Qualifier(SBPS_AUTHENTICATOR_AUTHENTICATION_WS_CONFIG_BEAN_NAME) ExampleWSConnectionConfig exampleWSConnectionConfig) {
        return new ExampleWSImpl(exampleWSConnectionConfig);
    }



}
