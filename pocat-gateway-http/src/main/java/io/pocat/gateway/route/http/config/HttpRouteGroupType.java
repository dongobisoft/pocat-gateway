package io.pocat.gateway.route.http.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class HttpRouteGroupType {
    private String name;
    private String description;
    @JacksonXmlElementWrapper(localName = "http-error-templates")
    private List<HttpErrorTemplateType> errorTemplates;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HttpErrorTemplateType> getErrorTemplates() {
        return errorTemplates;
    }

    public void setErrorTemplates(List<HttpErrorTemplateType> errorTemplates) {
        this.errorTemplates = errorTemplates;
    }

    public List<HttpErrorTemplateType> listErrorTemplates() {
        return errorTemplates;
    }
}
