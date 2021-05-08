package com.alessio.soapconsumer.security.ssl;

public class KeystoreInfo {

    private String storePath;
    private String storePassword;
    private String storeType;
    private String keyAlias;
    private String keyPassword;

    /**
     * Constructor for KeystoreInfo object.
     * 
     * @param storePath It must be an absolute path
     * @param storePassword
     * @param storeType 
     */
    public KeystoreInfo(String storePath, String storePassword, String storeType) {
        this.storePath = storePath;
        this.storePassword = storePassword;
        this.storeType = storeType;
    }

    /**
     * Constructor for KeystoreInfo object.
     * 
     * @param storePath It must be an absolute path
     * @param storePassword
     * @param storeType
     * @param keyAlias
     * @param keyPassword 
     */
    public KeystoreInfo(String storePath, String storePassword, String storeType, String keyAlias, String keyPassword) {
        this.storePath = storePath;
        this.storePassword = storePassword;
        this.storeType = storeType;
        this.keyAlias = keyAlias;
        this.keyPassword = keyPassword;
    }

    public KeystoreInfo(String storePath, String storePassword, String storeType, String keyAlias) {
        this.storePath = storePath;
        this.storePassword = storePassword;
        this.storeType = storeType;
        this.keyAlias = keyAlias;
        this.keyPassword = storePassword; //keyPassword and storePassword have the same value
    }

    public String getStorePath() {
        return storePath;
    }

    public void setStorePath(String storePath) {
        this.storePath = storePath;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

}
