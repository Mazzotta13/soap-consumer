/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alessio.soapconsumer.security.utils;

import com.alessio.soapconsumer.security.ssl.AliasX509KeyManager;
import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
import com.alessio.soapconsumer.security.ssl.PreferredCipherSuiteSSLSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author claudiopupparo
 */
public class SSLUtils {

    public final static String SYSTEM_TRUSTSTORE = "system";

    /**
     * Restituisce una risorsa a partire dal path. Se la risorsa è interna al
     * classpath, resourcePath è un path relativo. In caso contrario,
     * resourcePath è un path assoluto
     *
     * @param resourcePath
     * @return
     */
    private static Resource getResource(String resourcePath) {
        Resource resource;
        //path assoluto se comincia per "/"
        if (resourcePath.startsWith(System.getProperty("file.separator"))) {
            resource = new FileSystemResource(resourcePath);
        } else //path relativo se non comincia per "/"
        {
            resource = new ClassPathResource(resourcePath);
        }

        return resource;
    }

    /**
     * Carica le informazioni di un keystore/truststore e restituisce il
     * relativo oggetto
     *
     * @param keyStoreInfo:
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     */
    public static KeyStore loadKeyStore(KeystoreInfo keyStoreInfo)
            throws IOException, NoSuchAlgorithmException,
            CertificateException, KeyStoreException {
        String path = keyStoreInfo.getStorePath();
        String type = keyStoreInfo.getStoreType();
        String password = keyStoreInfo.getStorePassword();

        Resource keyStoreResource = getResource(path);

        File file = keyStoreResource.getFile();

        InputStream is = new FileInputStream(file);
        KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(is, password.toCharArray());

        return keyStore;
    }

    /**
     * Controlla se stiamo utilizzando il truststore di sistema
     *
     * @param truststore
     * @return
     */
    private static boolean isSystemTruststore(KeystoreInfo truststore) {
        return truststore != null
                && truststore.getStorePath() != null
                && truststore.getStorePath().equalsIgnoreCase(SYSTEM_TRUSTSTORE);
    }

    /**
     * Restituisce un SSLContext dalle informazioni sul keystore, truststore e
     * SecureRandom. Il protocollo effettivamente utilizzato nella comunicazione
     * viene scelto fra quelli definiti in socket.getEnabledProtocols()
     * <p>
     * A meno di casi particolari, utilizzare
    *
     * @param keyStore
     * @param trustStore
     * @param keyManagerFactoryAlgorithm:   algoritmo per keyManagerFactory. Se
     *                                      nullo viene utilizzato il valore di default
     * @param trustManagerFactoryAlgorithm: : algoritmo per keyManagerFactory.
     *                                      Se nullo viene utilizzato il valore di default
     * @param secureRandom:                 se null, ne viene inizializzato uno internamente
     * @param sslProtocol:                  protocollo con cui configurare sslContext. Non può
     *                                      essere null.
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    public static SSLContext getSSLContext(
            KeystoreInfo keyStore, KeystoreInfo trustStore,
            String keyManagerFactoryAlgorithm, String trustManagerFactoryAlgorithm,
            SecureRandom secureRandom, String sslProtocol)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException {

        //Configura keyManagers. Se keyStore nullo, niente keystore
        KeyManager[] keyManagers = null;
        if (keyStore != null) {
            KeyStore keystore = loadKeyStore(keyStore);
            String keyAlias = keyStore.getKeyAlias();

            //Algoritmo per KeyManagerFactory. Se nullo, usa quello di default
            String kmfAlgorithm = keyManagerFactoryAlgorithm != null
                    ? keyManagerFactoryAlgorithm
                    : KeyManagerFactory.getDefaultAlgorithm();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
            kmf.init(keystore, keyStore.getStorePassword().toCharArray());
            KeyManager[] keyManagersListFromFactory = kmf.getKeyManagers();

            //if no alias is specified, we should not execute this code, otherwise no certificate will be used.
            //I don't know why, but it should be related to SunX509KeyManagerImpl.chooseClientAlias
            if (StringUtils.hasText(keyAlias)) {
                //Wrap keyManager in AliasX509KeyManager in order to force the use of the chosen alias, if any
                ArrayList<KeyManager> keyManagerArrayList = new ArrayList<>();
                for (KeyManager keyManager : keyManagersListFromFactory) {
                    if (keyManager instanceof X509KeyManager) {

                        keyManagerArrayList.add(
                                new AliasX509KeyManager((X509KeyManager) keyManager, keyAlias));
                    }
                }
                keyManagers = keyManagerArrayList.toArray(new KeyManager[0]);
            } else {
                keyManagers = keyManagersListFromFactory;
            }
        }

        //Configura trustManagers. Se trustStore nullo, niente trustore.
        //Se il path del truststore è uguale a SYSTEM_TRUSTSTORE, allora utilizza il truststore di sistema
        TrustManager[] trustManagers = null;
        if (trustStore != null && !isSystemTruststore(trustStore)) {
            KeyStore truststore = loadKeyStore(trustStore);

            //Algoritmo per TrustManagerFactory. Se nullo, usa quello di default
            String tmfAlgorithm = trustManagerFactoryAlgorithm != null
                    ? trustManagerFactoryAlgorithm
                    : TrustManagerFactory.getDefaultAlgorithm();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(truststore);
            trustManagers = tmf.getTrustManagers();
        }

        //Configura secureRandom
        if (secureRandom == null) {
            secureRandom = new SecureRandom();
        }

        //Configura SSLContext
        //sslProtocol determina la tipologia di oggetto SSLContext restituito
        //Il protocollo realmente utilizzato viene selezionato fra quelli
        //presenti in socket.getEnabledProtocols()
        SSLContext ctx = SSLContext.getInstance(sslProtocol);
        ctx.init(keyManagers, trustManagers, secureRandom);

        //il rispetto dell'ordine delle ciphersuite è impostabile solo da JDK8
        //tramite le seguenti chiamate
//        ctx.getDefaultSSLParameters().setUseCipherSuitesOrder(true);
//        ctx.getSupportedSSLParameters().setUseCipherSuitesOrder(true);
        return ctx;
    }

    /**
     * A meno di casi particolari, utilizzare
     *
     * @param sslContext         SSLContext inizializzato con getSSLContext. Non può
     *                           essere null
     * @param sslCipherSuiteList
     * @param sslProtocolList
     * @return
     */
    public static SSLSocketFactory getSSLSocketFactory(
            SSLContext sslContext,
            List<String> sslCipherSuiteList, List<String> sslProtocolList) {

        SSLSocketFactory sslSocketFactory
                = new PreferredCipherSuiteSSLSocketFactory(
                sslContext.getSocketFactory(),
                sslCipherSuiteList,
                sslProtocolList);
        return sslSocketFactory;
    }

    /**
     * A meno di casi particolari, utilizzare
     *
     * @param keyStore
     * @param trustStore
     * @param keyManagerFactoryAlgorithm
     * @param trustManagerFactoryAlgorithm
     * @param secureRandom
     * @param sslCipherSuiteList
     * @param sslProtocolList
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws IllegalArgumentException
     */
    public static SSLSocketFactory getSSLSocketFactory(
            KeystoreInfo keyStore, KeystoreInfo trustStore,
            String keyManagerFactoryAlgorithm, String trustManagerFactoryAlgorithm,
            SecureRandom secureRandom,
            List<String> sslCipherSuiteList, List<String> sslProtocolList)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException,
            IllegalArgumentException {

        if (sslCipherSuiteList == null || sslCipherSuiteList.size() < 1) {
            throw new IllegalArgumentException("Ciphersuites list must not be empty!");
        }

        if (sslProtocolList == null || sslProtocolList.size() < 1) {
            throw new IllegalArgumentException("Protocols list must not be empty!");
        }
        //la lista è specificata in ordine di preferenza
        String sslProtocol = sslProtocolList.get(0);

        SSLContext sslContext
                = getSSLContext(
                keyStore, trustStore,
                keyManagerFactoryAlgorithm, trustManagerFactoryAlgorithm,
                secureRandom, sslProtocol);

        return getSSLSocketFactory(sslContext, sslCipherSuiteList, sslProtocolList);
    }

    /**
     * Inizializza un SSLSocketFactory con Keystore e Truststore. I protocolli e
     * le ciphersuite utilizzate rientrano esclusivamente nel set specificato
     *
     * @param keyStore:           l'oggetto contenente informazioni sul keystore. Se null,
     *                            non viene usato un keystore
     * @param trustStore:         l'oggetto contentente informazioni sul truststore. Se
     *                            null, non viene usato un truststore
     * @param sslCipherSuiteList: Lista di ciphersuite effettivamente abilitate
     *                            per essere utilizzate. Non può essere vuota
     * @param sslProtocolList:    Lista di protocolli effettivamente abilitati per
     *                            essere utilizzati. Di default viene utilizzato il primo della lista. Non
     *                            può essere vuota
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws IllegalArgumentException
     */
    public static SSLSocketFactory getSSLSocketFactory(
            KeystoreInfo keyStore, KeystoreInfo trustStore,
            List<String> sslCipherSuiteList, List<String> sslProtocolList)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException,
            IllegalArgumentException {
        return getSSLSocketFactory(
                keyStore,
                trustStore,
                null, null,
                null,
                sslCipherSuiteList, sslProtocolList);
    }

    /**
     * Inizializza un LayeredConnectionSocketFactory necessario per configurare
     * un httpClient per comunicare su ssl.
     * <p>
     * A meno di casi particolari, utilizzare
     * {@link SSLUtils#getLayeredConnectionSocketFactory(SSLSocketFactory) getLayeredConnectionSocketFactory}
     *
     * @param sslSocketFactory
     * @param x509HostnameVerifier
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws IllegalArgumentException
     */
    public static LayeredConnectionSocketFactory getLayeredConnectionSocketFactory(
            SSLSocketFactory sslSocketFactory,
            X509HostnameVerifier x509HostnameVerifier)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException,
            IllegalArgumentException {

        return new SSLConnectionSocketFactory(sslSocketFactory, x509HostnameVerifier);

    }

    /**
     * Inizializza un LayeredConnectionSocketFactory necessario per configurare
     * un httpClient per comunicare su ssl.
     *
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws IllegalArgumentException
     */
    public static LayeredConnectionSocketFactory getLayeredConnectionSocketFactory(
            SSLSocketFactory sslSocketFactory)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException,
            IllegalArgumentException {

        X509HostnameVerifier x509HostnameVerifier = getDefaultX509HostnameVerifier();

        return getLayeredConnectionSocketFactory(sslSocketFactory, x509HostnameVerifier);
    }

    /**
     * Restituisce un org.apache.http.conn.ssl.SSLSocketFactory, necessario per
     * la configurazione del httpClient di KMS
     *
     * @param sslCipherSuiteList
     * @param sslProtocolList
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws IllegalArgumentException
     */
    public static org.apache.http.conn.ssl.SSLSocketFactory getApacheSSLSocketFactory(
            List<String> sslCipherSuiteList, List<String> sslProtocolList)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException,
            IllegalArgumentException {

        return getApacheSSLSocketFactory(null, null, null, null, null, sslCipherSuiteList, sslProtocolList);
    }

    /**
     * Restituisce un org.apache.http.conn.ssl.SSLSocketFactory, necessario per
     * la configurazione del httpClient di KMS. I parametri addizionali sono per
     * la creazione di un SSLContext custom
     *
     * @param keyStore
     * @param trustStore
     * @param keyManagerFactoryAlgorithm
     * @param trustManagerFactoryAlgorithm
     * @param secureRandom
     * @param sslCipherSuiteList
     * @param sslProtocolList
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     * @throws IllegalArgumentException
     */
    public static org.apache.http.conn.ssl.SSLSocketFactory getApacheSSLSocketFactory(
            KeystoreInfo keyStore, KeystoreInfo trustStore,
            String keyManagerFactoryAlgorithm, String trustManagerFactoryAlgorithm,
            SecureRandom secureRandom,
            List<String> sslCipherSuiteList, List<String> sslProtocolList)
            throws IOException, NoSuchAlgorithmException, CertificateException,
            KeyStoreException, UnrecoverableKeyException, KeyManagementException,
            IllegalArgumentException {

        X509HostnameVerifier x509HostnameVerifier = getDefaultX509HostnameVerifier();
        String sslProtocol = sslProtocolList.get(0);

        SSLContext sslContext
                = SSLUtils.getSSLContext(
                keyStore, trustStore,
                keyManagerFactoryAlgorithm, trustManagerFactoryAlgorithm,
                secureRandom, sslProtocol);

        String[] protocols = sslProtocolList.toArray(new String[0]);
        String[] cipherSuites = sslCipherSuiteList.toArray(new String[0]);

        //sono costretto ad utilizzare sslContext anziché javax.net.ssl.sslSocketFactory per via di un bug:
        //creava una socket "non connessa" che non è supportata (deve essere già connessa)
        //il funzionamento è in ogni caso il medesimo, da wireshark la forzatura di protocolli e ciphersuite funziona
        return new org.apache.http.conn.ssl.SSLSocketFactory(sslContext, protocols, cipherSuites, x509HostnameVerifier);
    }

    /**
     * Restituisce l'oggetto X509HostnameVerifier per verificare che l'hostname
     * corrisponda al common name del certificato
     *
     * @return
     */
    public static X509HostnameVerifier getDefaultX509HostnameVerifier() {
        //se null, il comportamento di default è eseguire il check sul common name
        //ma sarebbe meglio specificarne uno (TODO)
        return null;
    }

    /**
     * Restituisce un X509HostnameVerifier che non effettua alcun check tra
     * hostname e common name del certificato
     *
     * @return
     */
    public static X509HostnameVerifier getAllowAllHostnameVerifier() {
        return new AllowAllHostnameVerifier();
    }
}
