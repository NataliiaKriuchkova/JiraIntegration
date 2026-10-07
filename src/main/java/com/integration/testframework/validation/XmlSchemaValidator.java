package com.integration.testframework.validation;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates an XML payload (e.g. the Codebeamer item XML produced by
 * {@code JiraToCodebeamerTransformer}) against an XSD, collecting every
 * violation instead of failing fast on the first one.
 */
public final class XmlSchemaValidator {

    private XmlSchemaValidator() {
    }

    /**
     * Validates {@code xmlPayload} against the XSD found at {@code classpathXsdPath}
     * (resolved from the test classpath, e.g. "schema/codebeamer-item.xsd").
     *
     * @return the list of validation error messages; empty if the document is valid.
     */
    public static List<String> validate(String xmlPayload, String classpathXsdPath) {
        List<String> violations = new ArrayList<>();

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try (InputStream xsdStream = XmlSchemaValidator.class.getClassLoader()
                .getResourceAsStream(classpathXsdPath)) {

            if (xsdStream == null) {
                throw new IllegalArgumentException("XSD not found on classpath: " + classpathXsdPath);
            }

            Schema schema = schemaFactory.newSchema(new StreamSource(xsdStream));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new CollectingErrorHandler(violations));
            validator.validate(new StreamSource(new StringReader(xmlPayload)));

        } catch (org.xml.sax.SAXException | IOException e) {
            violations.add("Schema loading/validation error: " + e.getMessage());
        }

        return violations;
    }

    public static boolean isValid(String xmlPayload, String classpathXsdPath) {
        return validate(xmlPayload, classpathXsdPath).isEmpty();
    }

    /** Accumulates warnings/errors/fatal errors instead of throwing on the first one. */
    private record CollectingErrorHandler(List<String> violations) implements org.xml.sax.ErrorHandler {

        @Override
        public void warning(org.xml.sax.SAXParseException exception) {
            violations.add("WARNING: " + exception.getMessage());
        }

        @Override
        public void error(org.xml.sax.SAXParseException exception) {
            violations.add("ERROR: " + exception.getMessage());
        }

        @Override
        public void fatalError(org.xml.sax.SAXParseException exception) {
            violations.add("FATAL: " + exception.getMessage());
        }
    }
}
