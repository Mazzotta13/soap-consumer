package com.alessio.soapconsumer.security.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Class that wraps a X509KeyManager.
 * If an alias is provided (different than null), then it will try to find the certificate and private key associated to that alias.
 * If an alias is not provided, then it will delegate the search of the alias to the given X509KeyManager delegate.
 *
 */
public class AliasX509KeyManager implements X509KeyManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliasX509KeyManager.class);

    private X509KeyManager delegate;
    private String keyAlias;

    public AliasX509KeyManager(X509KeyManager delegate, String keyAlias) {
        this.delegate = delegate;
        this.keyAlias = keyAlias;
    }

    //1) First method to be called (when looking for client data)
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
                                    Socket socket) {
        return keyAlias == null ? delegate.chooseClientAlias(keyType, issuers, socket) : keyAlias;
    }


    public String chooseServerAlias(String keyType, Principal[] issuers,
                                    Socket socket) {
        return keyAlias == null ? delegate.chooseServerAlias(keyType, issuers, socket) : keyAlias;
    }

    //2) Second method to be called
    public X509Certificate[] getCertificateChain(String alias) {
        return delegate.getCertificateChain(alias);
    }

    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return delegate.getClientAliases(keyType, issuers);
    }

    //3) Third method to be called
    public PrivateKey getPrivateKey(String alias) {
        return delegate.getPrivateKey(alias);
    }

    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return delegate.getServerAliases(keyType, issuers);
    }

}