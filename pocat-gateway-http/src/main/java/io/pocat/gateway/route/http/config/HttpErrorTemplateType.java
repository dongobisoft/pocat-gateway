package io.pocat.gateway.route.http.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class HttpErrorTemplateType {
    @JacksonXmlProperty(localName = "error-code")
    private int errorCode;
    @JacksonXmlProperty(localName = "response-code")
    private int responseCode;
    @JacksonXmlElementWrapper(localName = "response-formats")
    private List<HttpResponseFormatType> responseFormats;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<HttpResponseFormatType> getResponseFormats() {
        return responseFormats;
    }

    public void setResponseFormats(List<HttpResponseFormatType> responseFormats) {
        this.responseFormats = responseFormats;
    }
}
