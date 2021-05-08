//package com.alessio.soapconsumer.security.handler;
//
//import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
//import org.apache.wss4j.common.cache.ReplayCache;
//import org.apache.wss4j.common.crypto.Crypto;
//import org.apache.wss4j.common.crypto.CryptoFactory;
//import org.apache.wss4j.common.crypto.Merlin;
//import org.apache.wss4j.common.ext.WSSecurityException;
//import org.apache.wss4j.dom.WSConstants;
//import org.apache.wss4j.dom.engine.WSSecurityEngine;
//import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
//import org.apache.wss4j.dom.handler.RequestData;
//import org.apache.wss4j.dom.handler.WSHandlerResult;
//import org.apache.wss4j.dom.message.WSSecHeader;
//import org.apache.wss4j.dom.message.WSSecSignature;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.util.StringUtils;
//import org.w3c.dom.Document;
//
//import javax.xml.namespace.QName;
//import javax.xml.soap.SOAPException;
//import javax.xml.soap.SOAPMessage;
//import javax.xml.transform.*;
//import javax.xml.transform.dom.DOMResult;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.ws.handler.MessageContext;
//import javax.xml.ws.handler.soap.SOAPHandler;
//import javax.xml.ws.handler.soap.SOAPMessageContext;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Properties;
//import java.util.Set;
//import java.util.regex.Pattern;
//
//@Deprecated
//public class WSSecurityHandler implements SOAPHandler<SOAPMessageContext> {
//
//    protected final Logger logger = LoggerFactory.getLogger(getClass());
//
//    Properties keystOutgoingProps;
//    Properties truststoreProps;
//
//    private Boolean signOutgoingEnvelope;
//    private Boolean checkSignedIngoingEnvelope;
//    private String certificateConstraint;
//
//    public WSSecurityHandler() {
//        signOutgoingEnvelope = Boolean.FALSE;
//        checkSignedIngoingEnvelope = Boolean.FALSE;
//    }
//
//    public WSSecurityHandler(KeystoreInfo keystore, KeystoreInfo truststore, Boolean signOutgoingEnvelope, Boolean checkSignedIngoingEnvelope) {
//
//        init(signOutgoingEnvelope, keystore, checkSignedIngoingEnvelope, truststore);
//        this.certificateConstraint = null;
//    }
//
//    public WSSecurityHandler(KeystoreInfo keystore, KeystoreInfo truststore, Boolean signOutgoingEnvelope, Boolean checkSignedIngoingEnvelope, String certificateConstraint) {
//
//        init(signOutgoingEnvelope, keystore, checkSignedIngoingEnvelope, truststore);
//        if (!StringUtils.hasText(certificateConstraint)) {
//            throw new RuntimeException("Passed a null or empty regex for common name.");
//        } else {
//            this.certificateConstraint = certificateConstraint;
//        }
//    }
//
//    public final void init(Boolean signOutgoing, KeystoreInfo keystore, Boolean checkSignedIngoing, KeystoreInfo truststore) throws RuntimeException {
//        if (signOutgoing != null && signOutgoing) {
//            initKeystore(keystore);
//            signOutgoingEnvelope = Boolean.TRUE;
//        } else {
//            signOutgoingEnvelope = Boolean.FALSE;
//        }
//        if (checkSignedIngoing != null && checkSignedIngoing) {
//            initTruststore(truststore);
//            checkSignedIngoingEnvelope = Boolean.TRUE;
//        } else {
//            checkSignedIngoingEnvelope = Boolean.FALSE;
//        }
//    }
//
//    public void setCertificateConstraint(String certificateConstraint) {
//        this.certificateConstraint = certificateConstraint;
//    }
//
//    private void initKeystore(KeystoreInfo keystore) throws RuntimeException {
//        if (keystore == null) {
//            throw new RuntimeException("Passed a null keystore to sign outgoing envelope.");
//        }
//        keystOutgoingProps = new Properties();
//        keystOutgoingProps.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, keystore.getStorePath());
//        keystOutgoingProps.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, keystore.getStorePassword());
//        keystOutgoingProps.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, keystore.getStoreType());
//        keystOutgoingProps.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, keystore.getKeyAlias());
//        keystOutgoingProps.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PRIVATE_PASSWORD, keystore.getKeyPassword());
//    }
//
//    private void initTruststore(KeystoreInfo truststore) throws RuntimeException {
//        if (truststore == null) {
//            throw new RuntimeException("Passed a null keystore to check signed ingoing envelope.");
//        }
//        truststoreProps = new Properties();
//        truststoreProps.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_FILE, truststore.getStorePath());
//        truststoreProps.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_PASSWORD, truststore.getStorePassword());
//        truststoreProps.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_TYPE, truststore.getStoreType());
//    }
//
//    /**
//     * Returns the headers that this Soap Handler understand. This is needed if
//     * the client sends some header with the attribute "mustUnderstand" set to
//     * True (or 1)
//     *
//     * @return
//     */
//    @Override
//    public Set<QName> getHeaders() {
//        Set<QName> HEADERS = new HashSet<>();
//        HEADERS.add(new QName(WSConstants.WSSE_NS, "Security"));
//        HEADERS.add(new QName(WSConstants.WSSE11_NS, "Security"));
//        HEADERS.add(new QName(WSConstants.ENC_NS, "EncryptedData"));
//        return HEADERS;
//    }
//
//    /**
//     * Handles the message. Encode and Decode the message depending if it is an
//     * outgoing or incoming one.
//     *
//     * @param messageContext
//     * @return
//     */
//    @Override
//    public boolean handleMessage(SOAPMessageContext messageContext) {
//        SOAPMessage soapMessage = messageContext.getMessage();
//        Boolean isOutGoing = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
//
//        if (isOutGoing) {
//            try {
//                handleOutgoingMessage(soapMessage);
//            } catch (Exception ex) {
//                logger.error("Exception during handleOutgoingMessage", ex);
//                throw new RuntimeException(ex.getMessage()); //if we don't throw this error, the client won't get an error!
//            }
//        } else {
//            try {
//                handleIngoingMessage(soapMessage);
//            } catch (Exception ex) {
//                logger.error("Exception during handleIngoingMessage", ex);
//                throw new RuntimeException(ex.getMessage()); //if we don't throw this error, the client won't get an error!
//            }
//        }
//
//        //continue in the handler chain
//        return true;
//    }
//
//    /**
//     * Sign outgoing message
//     *
//     * @param soapMessage
//     * @throws SOAPException
//     * @throws IOException
//     * @throws Exception
//     */
//    public void handleOutgoingMessage(SOAPMessage soapMessage)
//            throws Exception {
//        if (signOutgoingEnvelope) {
//            logger.debug("Signing outgoing envelope.");
//            Document signedEnvelope = signSOAPEnvelope(soapMessage);
//            updateSOAPMessage(signedEnvelope, soapMessage);
//            soapMessage.saveChanges();
//        }
//
//        logger.debug("Envelope in uscita: " + getMessageForLog(soapMessage));
//    }
//
//    /**
//     * Check signature of ingoing message
//     *
//     * @param soapMessage
//     * @throws SOAPException
//     * @throws IOException
//     * @throws Exception
//     */
//    public void handleIngoingMessage(SOAPMessage soapMessage)
//            throws Exception {
//        logger.debug("Envelope in entrata: " + getMessageForLog(soapMessage));
//
//        if (checkSignedIngoingEnvelope) {
//            logger.debug("Checking ingoing envelope.");
//            Document envelope = getDocumentFromSoapMessage(soapMessage);
//            //checkSignatureAndDecode(envelope, ".*CN=BackofficeTestSBPS.*"); //example with regular expression on common name (inside Distinguished Name)
//            checkSignatureAndDecode(envelope, certificateConstraint);
//        }
//    }
//
//    private String getMessageForLog(SOAPMessage soapMessage)
//            throws SOAPException, IOException {
//        String soapMessageToLog;
//        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
//            soapMessage.writeTo(os);
//            soapMessageToLog = new String(os.toByteArray(), "UTF-8");
//        }
//        return soapMessageToLog;
//    }
//
//    /**
//     * Executed when an exception is thrown in handleMessage. Never executed, I
//     * don't know why
//     *
//     * @param messageContext
//     * @return
//     */
//    @Override
//    public boolean handleFault(SOAPMessageContext messageContext) {
//        logger.debug("Executing handleFault");
//        return true; //continue in the handler fault chain
//    }
//
//    /**
//     * Executed at the end, before going to the next handler in the chain
//     *
//     * @param messageContext
//     */
//    @Override
//    public void close(MessageContext messageContext) {
//        logger.trace("Executing close");
//    }
//
//    /**
//     * Execute some wtf magic and get the soap message as document
//     *
//     * @param soapMsg
//     * @return
//     * @throws Exception
//     */
//    private Document getDocumentFromSoapMessage(SOAPMessage soapMsg) throws SOAPException, TransformerConfigurationException, TransformerException {
//        Source src = soapMsg.getSOAPPart().getContent();
//        TransformerFactory tf = TransformerFactory.newInstance();
//        Transformer transformer = tf.newTransformer();
//        DOMResult result = new DOMResult();
//        transformer.transform(src, result);
//        return (Document) result.getNode();
//    }
//
//    private WSSecSignature getSignatureBuilder(Document doc) throws WSSecurityException {
//        WSSecHeader secHeader = new WSSecHeader(doc);
//        secHeader.insertSecurityHeader();
//
//        WSSecSignature builder = new WSSecSignature(secHeader);
//        builder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE); //add signing certificate as BinarySecurityToken
//        builder.setAddInclusivePrefixes(false); //sbps needed this because otherwise some elements in our response were repeated
//        builder.setUserInfo(
//                keystOutgoingProps.getProperty(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS),
//                keystOutgoingProps.getProperty(Merlin.PREFIX + Merlin.KEYSTORE_PRIVATE_PASSWORD)
//        );
//        return builder;
//    }
//
//    private SOAPMessage updateSOAPMessage(Document doc, SOAPMessage message) throws SOAPException {
//        DOMSource domSource = new DOMSource(doc);
//        message.getSOAPPart().setContent(domSource);
//        return message;
//    }
//
//    /**
//     * Sign soap envelope with private key inside keystore
//     *
//     * @param message
//     * @return
//     * @throws Exception
//     */
//    private Document signSOAPEnvelope(SOAPMessage message) throws SOAPException, TransformerException, WSSecurityException {
//        Document doc = getDocumentFromSoapMessage(message);
//
//        Crypto crypto = CryptoFactory.getInstance(keystOutgoingProps);
//        WSSecSignature builder = getSignatureBuilder(doc);
//        return builder.build(crypto);
//    }
//
//    private RequestData getRequestDataForSignatureCheck(String certConstraint) throws WSSecurityException {
//        Crypto sigCrypto = CryptoFactory.getInstance(truststoreProps);
//
//        RequestData requestData = new RequestData();
//        requestData.setSigVerCrypto(sigCrypto);
//
//        if (StringUtils.hasText(certConstraint)) {
//            logger.debug("Regular Expression: " + certConstraint);
//            Pattern subjectDNPattern = Pattern.compile(certConstraint.trim());
//            requestData.setSubjectCertConstraints(Collections.singletonList(subjectDNPattern));
//        }
//
//        return requestData;
//    }
//
//    /**
//     * Verify the signature and throw an error is something wrong happens
//     *
//     * @param signedDocument
//     * @param certConstraint
//     * @throws WSSecurityException
//     * @throws IOException
//     */
//    private void checkSignatureAndDecode(Document signedDocument, String certConstraint) throws Exception {
//        RequestData requestData = getRequestDataForSignatureCheck(certConstraint);
//
//        WSSecurityEngine secEngine = new WSSecurityEngine();
//        try {
//            WSHandlerResult processSecurityHeader = secEngine.processSecurityHeader(signedDocument, requestData);
//
//            //if no signature is found then processSecurityHeader is null and we should throw an exception
//            if (processSecurityHeader == null
//                    || processSecurityHeader.getActionResults() == null
//                    || processSecurityHeader.getActionResults().get(WSConstants.SIGN) == null) {
//                throw new Exception("No signature found");
//            }
//
//            WSSecurityEngineResult actionResult = processSecurityHeader.getActionResults().get(WSConstants.SIGN).get(0);
//
//            if (actionResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE) == null) {
//                throw new Exception("No signature certificate found");
//            }
//        } finally { //no need for catch if an error is thrown
//            //it's very important that we invoke the close method!
//            //otherwise we don't terminate the pending threads
//            //and we don't clean the temporary files
//            ReplayCache timestampReplayCache = requestData.getTimestampReplayCache();
//            //timestampReplayCache object is null when the
//            //setEnableTimestampReplayCache method is deprecated
//            if (timestampReplayCache != null) {
//                timestampReplayCache.close();
//            }
//        }
//    }
//}
