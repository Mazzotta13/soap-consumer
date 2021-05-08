package com.alessio.soapconsumer.security.utils;

import org.apache.xml.security.c14n.Canonicalizer;
import org.w3c.dom.Document;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class SOAPUtil {

    public static SOAPMessage toSOAPMessage(Document doc) throws Exception {
        Canonicalizer c14n = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            c14n.canonicalizeSubtree(doc, byteArrayOutputStream);
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray())) {
                MessageFactory factory = MessageFactory.newInstance();
                return factory.createMessage(null, byteArrayInputStream);
            }
        }
    }

    //https://coderanch.com/t/224229/java/convert-SOAP-Message-Document
    public static Document toDocument(SOAPMessage message) throws Exception {
        Source src = message.getSOAPPart().getContent();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        DOMResult result = new DOMResult();
        transformer.transform(src, result);
        return (Document) result.getNode();
    }

    public static SOAPMessage updateSOAPMessage(Document doc, SOAPMessage message) throws SOAPException {
        DOMSource domSource = new DOMSource(doc);
        message.getSOAPPart().setContent(domSource);
        return message;
    }

    public static String getMessageForLog(SOAPMessage soapMessage) throws Exception {
        String soapMessageToLog;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            soapMessage.writeTo(os);
            soapMessageToLog = new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
        return soapMessageToLog;
    }

}
