package com.alessio.soapconsumer.security.handler.processor.outgoing;

import com.alessio.soapconsumer.security.handler.processor.SoapMessageProcessor;
import com.alessio.soapconsumer.security.model.KeyIdentifierType;
import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
import com.alessio.soapconsumer.security.utils.SOAPUtil;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.message.WSSecEncrypt;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Properties;

public class EncryptionProcessor implements SoapMessageProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(EncryptionProcessor.class);

    private KeystoreInfo keystoreInfo;
    private Properties properties;
    private KeyIdentifierType keyIdentifierType;

    public EncryptionProcessor(KeystoreInfo keystoreInfo) {
        this.keystoreInfo = keystoreInfo;

        properties = new Properties();
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, keystoreInfo.getStorePath());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, keystoreInfo.getStorePassword());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreInfo.getStoreType());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, keystoreInfo.getKeyAlias());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PRIVATE_PASSWORD, keystoreInfo.getKeyPassword());

        keyIdentifierType = KeyIdentifierType.BINARY_SECURITY_TOKEN_REFERENCE;

        //we need to call this static method otherwise when calling "builder.build(crypto, key);" we will get the following error:
        // You must initialize the xml-security library correctly before you use it. Call the static method "org.apache.xml.security.Init.init();" to do that before you use any functionality from that library
        org.apache.xml.security.Init.init();
    }

    @Override
    public void process(SOAPMessageContext messageContext) throws Exception {
        SOAPMessage soapMessage = messageContext.getMessage();

        Document encryptedBody = encryptBody(soapMessage);
        SOAPUtil.updateSOAPMessage(encryptedBody, soapMessage);
        soapMessage.saveChanges();
    }

    //https://github.com/apache/ws-wss4j/blob/master/ws-security-dom/src/test/java/org/apache/wss4j/dom/message/EncryptionTest.java#L114
    private Document encryptBody(SOAPMessage soapMessage) throws Exception {
        Document doc = SOAPUtil.toDocument(soapMessage);

        WSSecEncrypt builder = getEncryptionBuilder(doc);
        Crypto crypto = CryptoFactory.getInstance(properties);
        SecretKey key = generateKey();
        return builder.build(crypto, key);
    }

    private WSSecEncrypt getEncryptionBuilder(Document doc) throws Exception {
        WSSecHeader secHeader = new WSSecHeader(doc);
        secHeader.insertSecurityHeader();

        WSSecEncrypt builder = new WSSecEncrypt(secHeader);
        builder.setUserInfo(properties.getProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS));
        builder.setKeyEncAlgo(WSConstants.KEYTRANSPORT_RSAOAEP);
        builder.setSymmetricEncAlgorithm(WSConstants.AES_256);
        builder.setDigestAlgorithm(WSConstants.SHA256);

        if (KeyIdentifierType.ISSUER_SERIAL_NUMBER_REFERENCE == keyIdentifierType) {
            builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        } else {
            builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        }
        return builder;
    }

    private SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES"); //see SymmetricEncAlgorithm in getEncryptionBuilder
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public void setKeyIdentifierType(KeyIdentifierType keyIdentifierType) {
        this.keyIdentifierType = keyIdentifierType;
    }

    public EncryptionProcessor withKeyIdentifierType(final KeyIdentifierType keyIdentifierType) {
        setKeyIdentifierType(keyIdentifierType);
        return this;
    }
}
