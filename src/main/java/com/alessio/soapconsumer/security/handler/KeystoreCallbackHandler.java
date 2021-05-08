/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alessio.soapconsumer.security.handler;

import com.alessio.soapconsumer.security.ssl.KeystoreInfo;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KeystoreCallbackHandler implements CallbackHandler {

    public static Logger LOGGER = LoggerFactory.getLogger(KeystoreCallbackHandler.class);

    private Map<String, String> users = new HashMap<>();

    public KeystoreCallbackHandler(KeystoreInfo keystoreInfo) {
        users.put(keystoreInfo.getKeyAlias(), keystoreInfo.getKeyPassword());
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callback;
                if (users.containsKey(pc.getIdentifier())) {
                    pc.setPassword(users.get(pc.getIdentifier()));
                }
            } else {
                throw new UnsupportedCallbackException(callback, "Unrecognized Callback");
            }
        }
    }
}
