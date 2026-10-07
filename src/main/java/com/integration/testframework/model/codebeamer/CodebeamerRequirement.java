package com.integration.testframework.model.codebeamer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Immutable representation of a Codebeamer "item" (the destination/target side of
 * the synchronization flow). Codebeamer's tracker API natively accepts XML, so this
 * model is annotated for both JSON (middleware logging/diagnostics) and XML
 * (actual Codebeamer payload) serialization via jackson-dataformat-xml.
 */
@Value
@Builder
@JacksonXmlRootElement(localName = "item")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodebeamerRequirement {

    @JacksonXmlProperty(localName = "id")
    Long id;

    @JacksonXmlProperty(localName = "name")
    String name;

    @JacksonXmlProperty(localName = "description")
    String description;

    @JacksonXmlProperty(localName = "status")
    String status;

    @JacksonXmlProperty(localName = "priority")
    String priority;

    @JacksonXmlProperty(localName = "assignedTo")
    String assignedTo;

    /** External key used to correlate this Codebeamer item back to its Jira source. */
    @JacksonXmlProperty(localName = "externalId")
    String externalId;

    @JacksonXmlElementWrapper(localName = "labels")
    @JacksonXmlProperty(localName = "label")
    List<String> labels;
}
