package com.alessio.soapconsumer.security.handler;


import com.alessio.soapconsumer.security.handler.processor.SoapMessageProcessor;
import com.alessio.soapconsumer.security.utils.SOAPUtil;
import org.apache.wss4j.dom.WSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WSSecurityHandlerWithProcessor implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WSSecurityHandlerWithProcessor.class);

    private List<SoapMessageProcessor> outgoingMessageProcessors;
    private List<SoapMessageProcessor> ingoingMessageProcessors;

    public WSSecurityHandlerWithProcessor() {
        this.outgoingMessageProcessors = new ArrayList<>();
        this.ingoingMessageProcessors = new ArrayList<>();
    }

    /**
     * Return the headers that this Soap Handler understands. This is needed if
     * the client sends some header with the attribute "mustUnderstand" set to
     * True (or 1)
     *
     * @return
     */
    @Override
    public Set<QName> getHeaders() {
        Set<QName> headers = new HashSet<>();
        headers.add(new QName(WSConstants.WSSE_NS, WSConstants.WSSE_LN));
        headers.add(new QName(WSConstants.WSSE11_NS, WSConstants.WSSE_LN));
        return headers;
    }

    /**
     * Handle ingoing and outgoing messages.
     *
     * @param messageContext
     * @return
     */
    @Override
    public boolean handleMessage(SOAPMessageContext messageContext) {
        try {
            Boolean isOutGoing = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            if (isOutGoing) {
                handleOutgoingMessage(messageContext);
            } else {
                handleIngoingMessage(messageContext);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception during handleMessage", ex);
            throw new RuntimeException("Generic Error"); //if we don't throw this error, the client won't get an error!
        }

        return true; //continue in the handler chain
    }

    private void handleIngoingMessage(SOAPMessageContext messageContext) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Envelope in entrata: " + SOAPUtil.getMessageForLog(messageContext.getMessage()));
        }

        for (SoapMessageProcessor messageProcessor : ingoingMessageProcessors) {
            messageProcessor.process(messageContext);
        }

    }

    private void handleOutgoingMessage(SOAPMessageContext messageContext) throws Exception {
        for (SoapMessageProcessor messageProcessor : outgoingMessageProcessors) {
            messageProcessor.process(messageContext);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Envelope in uscita: " + SOAPUtil.getMessageForLog(messageContext.getMessage()));
        }
    }

    /**
     * Executed when an exception is thrown in handleMessage.
     *
     * @param messageContext
     * @return
     */
    @Override
    public boolean handleFault(SOAPMessageContext messageContext) {
        LOGGER.debug("Executing handleFault");
        return true; //continue in the handler fault chain
    }

    /**
     * Executed at the end, before going to the next handler in the chain
     *
     * @param messageContext
     */
    @Override
    public void close(MessageContext messageContext) {
        LOGGER.trace("Executing close");
    }

    public List<SoapMessageProcessor> getOutgoingMessageProcessors() {
        return outgoingMessageProcessors;
    }

    public List<SoapMessageProcessor> getIngoingMessageProcessors() {
        return ingoingMessageProcessors;
    }

    public void setOutgoingMessageProcessors(List<SoapMessageProcessor> outgoingMessageProcessors) {
        this.outgoingMessageProcessors = outgoingMessageProcessors;
    }

    public void setIngoingMessageProcessors(List<SoapMessageProcessor> ingoingMessageProcessors) {
        this.ingoingMessageProcessors = ingoingMessageProcessors;
    }

    public WSSecurityHandlerWithProcessor withOutgoingMessageProcessors(final List<SoapMessageProcessor> outgoingMessageProcessors) {
        setOutgoingMessageProcessors(outgoingMessageProcessors);
        return this;
    }

    public WSSecurityHandlerWithProcessor withIngoingMessageProcessors(final List<SoapMessageProcessor> ingoingMessageProcessors) {
        setIngoingMessageProcessors(ingoingMessageProcessors);
        return this;
    }


}
