package com.alessio.soapconsumer.security.ssl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class PreferredCipherSuiteSSLSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory delegate;
    private List<String> sslCipherSuiteList; //lista di ciphersuite utilizzabili
    private List<String> sslProtocolList; //lista di protocolli utilizzabili

    //richiamato solo da PostgresSSLSocketFactory
    protected PreferredCipherSuiteSSLSocketFactory() {
    }

    public PreferredCipherSuiteSSLSocketFactory(SSLSocketFactory delegate,
            List<String> sslCipherSuiteList, List<String> sslProtocolList) {
        this.delegate = delegate;
        this.sslCipherSuiteList = sslCipherSuiteList;
        this.sslProtocolList = sslProtocolList;
    }

    /**
     * Costruttore mantenuto solo per retrocompatibilità Utilizzare il costruttore con lista di
     * protocolli
     *
     * @param delegate
     * @param sslCipherSuiteList
     */
    public PreferredCipherSuiteSSLSocketFactory(SSLSocketFactory delegate,
            List<String> sslCipherSuiteList) {

        this.delegate = delegate;
        this.sslCipherSuiteList = sslCipherSuiteList;
        this.sslProtocolList = new ArrayList<String>();
    }

    @Override
    public String[] getDefaultCipherSuites() {

        return sslCipherSuiteList.toArray(new String[0]);
    }

    @Override
    public String[] getSupportedCipherSuites() {

        return sslCipherSuiteList.toArray(new String[0]);
    }

    //fondamentale, altrimenti lancia una "java.net.SocketException: Unconnected sockets not implemented"
    @Override
    public Socket createSocket() throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket();
        setSocket(socket);

        return socket;
    }

    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException,
            UnknownHostException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1);
        setSocket(socket);

        return socket;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.createSocket(arg0, arg1);
        setSocket(socket);

        return socket;
    }

    @Override
    public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
            throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.
                createSocket(arg0, arg1, arg2, arg3);
        setSocket(socket);

        return socket;
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
            throws IOException, UnknownHostException {

        SSLSocket socket = (SSLSocket) this.delegate.
                createSocket(arg0, arg1, arg2, arg3);
        setSocket(socket);

        return socket;
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
            int arg3) throws IOException {

        SSLSocket socket = (SSLSocket) this.delegate.
                createSocket(arg0, arg1, arg2, arg3);
        setSocket(socket);
        return socket;
    }

    /**
     * Imposta la lista di ciphersuite e protocolli abilitati alla comunicazione tramite la socket
     * passata come parametro. Viene lanciata una IllegalArgumentException nel caso in cui i
     * protocolli impostati con socket.setEnabledProtocols(protocols) non figurino nella lista di
     * quelli supportati, lista ottenibile tramite socket.getSupportedProtocols(). La lista di
     * protocolli supportati dipende dal SSLContext, da cui è stata ricavata la SSLSocketFactory da
     * cui è stata ricavata la SSLSocket
     *
     * @param socket: La socket da configurare con ciphersuite e protocolli
     */
    private void setSocket(SSLSocket socket) {
        if (sslCipherSuiteList.size() > 0) {
            String[] cipherSuites = sslCipherSuiteList.toArray(new String[0]);
            socket.setEnabledCipherSuites(cipherSuites);
        }

        if (sslProtocolList.size() > 0) {
            String[] protocols = sslProtocolList.toArray(new String[0]);
            socket.setEnabledProtocols(protocols);
        }
    }

    public SSLSocketFactory getDelegate() {
        return delegate;
    }

    public List<String> getSslCipherSuiteList() {
        return sslCipherSuiteList;
    }

    public List<String> getSslProtocolList() {
        return sslProtocolList;
    }

    public void setDelegate(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    public void setSslCipherSuiteList(List<String> sslCipherSuiteList) {
        this.sslCipherSuiteList = sslCipherSuiteList;
    }

    public void setSslProtocolList(List<String> sslProtocolList) {
        this.sslProtocolList = sslProtocolList;
    }
}
