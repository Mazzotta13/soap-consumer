package com.alessio.soapconsumer.security.handler.processor.ingoing;

import com.alessio.soapconsumer.security.handler.KeystoreCallbackHandler;
import com.alessio.soapconsumer.security.handler.processor.SoapMessageProcessor;
import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
import com.alessio.soapconsumer.security.utils.SOAPUtil;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Pattern;

public class DecryptionAndSignatureProcessor implements SoapMessageProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(DecryptionAndSignatureProcessor.class);

    private KeystoreInfo decryptionKeystoreInfo;
    private KeystoreInfo signatureVerificationKeystoreInfo;

    private Properties decryptionProperties;
    private Properties signatureVerificationProperties;

    private boolean mustDecrypt = false;
    private boolean mustVerifySignature = false;

    private String signatureCertificateSubjectConstraints;

    @Override
    public void process(SOAPMessageContext messageContext) throws Exception {
        SOAPMessage ingoingSoapMessage = messageContext.getMessage();
        Document envelope = SOAPUtil.toDocument(ingoingSoapMessage);

        SOAPMessage verifiedSoapMessage = verifySignatureAndDecrypt(envelope);
        messageContext.setMessage(verifiedSoapMessage);
    }

    private RequestData getRequestData() throws Exception {

        RequestData requestData = new RequestData();

        if (mustVerifySignature) {
            Crypto sigCrypto = CryptoFactory.getInstance(signatureVerificationProperties);
            requestData.setSigVerCrypto(sigCrypto);

            if (StringUtils.hasText(signatureCertificateSubjectConstraints)) {
                Pattern subjectDNPattern = Pattern.compile(signatureCertificateSubjectConstraints.trim());
                requestData.setSubjectCertConstraints(Collections.singletonList(subjectDNPattern));
            }
        }

        if (mustDecrypt) {
            Crypto decCrypto = CryptoFactory.getInstance(decryptionProperties);
            requestData.setDecCrypto(decCrypto);
        }

        return requestData;
    }

    private SOAPMessage verifySignatureAndDecrypt(Document envelope) throws Exception {
        RequestData requestData = getRequestData();
        WSSecurityEngine secEngine = new WSSecurityEngine();
        WSHandlerResult wsHandlerResult;

        if (mustDecrypt && mustVerifySignature) {
            CallbackHandler keystoreCallbackHandler = new KeystoreCallbackHandler(decryptionKeystoreInfo);

            wsHandlerResult = secEngine.processSecurityHeader(
                    envelope,
                    null,
                    keystoreCallbackHandler,
                    requestData.getSigVerCrypto(),
                    requestData.getDecCrypto());
        } else if (mustDecrypt) {
            CallbackHandler keystoreCallbackHandler = new KeystoreCallbackHandler(decryptionKeystoreInfo);

            wsHandlerResult = secEngine.processSecurityHeader(
                    envelope,
                    null,
                    keystoreCallbackHandler,
                    requestData.getDecCrypto());
        } else if (mustVerifySignature) {
            wsHandlerResult = secEngine.processSecurityHeader(
                    envelope,
                    requestData);
        } else {
            return SOAPUtil.toSOAPMessage(envelope);
        }

        checkResult(wsHandlerResult);

        return SOAPUtil.toSOAPMessage(envelope);
    }

    private void checkResult(WSHandlerResult wsHandlerResult) throws Exception {
        if (wsHandlerResult == null || wsHandlerResult.getActionResults() == null) {
            throw new Exception("Error during validation of security header");
        }

        if (mustDecrypt && wsHandlerResult.getActionResults().get(WSConstants.ENCR) == null) {
            throw new Exception("No encryption found");
        }

        if (mustVerifySignature && wsHandlerResult.getActionResults().get(WSConstants.SIGN) == null) {
            throw new Exception("No signature found");
        }
    }

    public DecryptionAndSignatureProcessor withDecryptionKeystoreInfo(final KeystoreInfo decryptionKeystoreInfo) {
        setDecryptionKeystoreInfo(decryptionKeystoreInfo);
        return this;
    }

    public DecryptionAndSignatureProcessor withSignatureVerificationKeystoreInfo(final KeystoreInfo signatureVerificationKeystoreInfo) {
        setSignatureVerificationKeystoreInfo(signatureVerificationKeystoreInfo);
        return this;
    }

    public DecryptionAndSignatureProcessor withSignatureCertificateSubjectConstraints(final String signatureCertificateSubjectConstraints) {
        this.signatureCertificateSubjectConstraints = signatureCertificateSubjectConstraints;
        return this;
    }

    public void setDecryptionKeystoreInfo(KeystoreInfo decryptionKeystoreInfo) {
        decryptionProperties = new Properties();
        decryptionProperties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_FILE, decryptionKeystoreInfo.getStorePath());
        decryptionProperties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, decryptionKeystoreInfo.getStorePassword());
        decryptionProperties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, decryptionKeystoreInfo.getStoreType());
        //The alias must not be set. When decrypting the right key is chosen starting from the certificate inside the client request
        decryptionProperties.setProperty(Merlin.PREFIX + Merlin.KEYSTORE_PRIVATE_PASSWORD, decryptionKeystoreInfo.getKeyPassword());
        mustDecrypt = true;

        this.decryptionKeystoreInfo = decryptionKeystoreInfo;
    }

    public void setSignatureVerificationKeystoreInfo(KeystoreInfo signatureVerificationKeystoreInfo) {
        signatureVerificationProperties = new Properties();
        signatureVerificationProperties.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_FILE, signatureVerificationKeystoreInfo.getStorePath());
        signatureVerificationProperties.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_PASSWORD, signatureVerificationKeystoreInfo.getStorePassword());
        signatureVerificationProperties.setProperty(Merlin.PREFIX + Merlin.TRUSTSTORE_TYPE, signatureVerificationKeystoreInfo.getStoreType());
        mustVerifySignature = true;

        this.signatureVerificationKeystoreInfo = signatureVerificationKeystoreInfo;
    }

    public void setSignatureCertificateSubjectConstraints(String signatureCertificateSubjectConstraints) {
        this.signatureCertificateSubjectConstraints = signatureCertificateSubjectConstraints;
    }
}
