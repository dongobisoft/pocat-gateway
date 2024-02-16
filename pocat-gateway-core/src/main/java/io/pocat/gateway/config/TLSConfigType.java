package io.pocat.gateway.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class TLSConfigType {
    @JacksonXmlProperty(localName = "key-path")
    private String keyPath;
    @JacksonXmlProperty(localName = "cert-path")
    private String certPath;
    @JacksonXmlProperty(localName = "key-password")
    private String keyPassword = "";

    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }
}
