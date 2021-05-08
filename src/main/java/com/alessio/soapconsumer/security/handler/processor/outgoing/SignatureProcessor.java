package com.alessio.soapconsumer.security.handler.processor.outgoing;

import com.alessio.soapconsumer.security.handler.processor.SoapMessageProcessor;
import com.alessio.soapconsumer.security.model.KeyIdentifierType;
import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
import com.alessio.soapconsumer.security.utils.SOAPUtil;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Properties;

public class SignatureProcessor implements SoapMessageProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(SignatureProcessor.class);

    private final KeystoreInfo keystoreInfo;
    private final Properties properties;
    private KeyIdentifierType keyIdentifierType;

    public SignatureProcessor(KeystoreInfo keystoreInfo) {
        this.keystoreInfo = keystoreInfo;

        properties = new Properties();
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, keystoreInfo.getStorePath());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, keystoreInfo.getStorePassword());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystoreInfo.getStoreType());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, keystoreInfo.getKeyAlias());
        properties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PRIVATE_PASSWORD, keystoreInfo.getKeyPassword());

        keyIdentifierType = KeyIdentifierType.BINARY_SECURITY_TOKEN_REFERENCE;
    }

    @Override
    public void process(SOAPMessageContext messageContext) throws Exception {
        SOAPMessage soapMessage = messageContext.getMessage();

        Document signedEnvelope = signSOAPEnvelope(soapMessage);
        SOAPUtil.updateSOAPMessage(signedEnvelope, soapMessage);
        soapMessage.saveChanges();
    }

    //https://github.com/apache/ws-wss4j/blob/master/ws-security-dom/src/test/java/org/apache/wss4j/dom/message/SignatureTest.java#L282
    private Document signSOAPEnvelope(SOAPMessage message) throws Exception {
        Document doc = SOAPUtil.toDocument(message);

        Crypto crypto = CryptoFactory.getInstance(properties);
        WSSecSignature builder = getSignatureBuilder(doc);
        return builder.build(crypto);
    }

    private WSSecSignature getSignatureBuilder(Document doc) throws Exception {
        WSSecHeader secHeader = new WSSecHeader(doc);
        secHeader.insertSecurityHeader();

        WSSecSignature builder = new WSSecSignature(secHeader);
        if (KeyIdentifierType.ISSUER_SERIAL_NUMBER_REFERENCE == keyIdentifierType) {
            builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        } else {
            builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        }

        builder.setAddInclusivePrefixes(false); //sbps needed this because otherwise some elements in our response were repeated multiple times
        builder.setUserInfo(
                properties.getProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS),
                properties.getProperty(Merlin.PREFIX + Merlin.KEYSTORE_PRIVATE_PASSWORD)
        );
        return builder;
    }

    public void setKeyIdentifierType(KeyIdentifierType keyIdentifierType) {
        this.keyIdentifierType = keyIdentifierType;
    }

    public SignatureProcessor withKeyIdentifierType(final KeyIdentifierType keyIdentifierType) {
        setKeyIdentifierType(keyIdentifierType);
        return this;
    }

}
