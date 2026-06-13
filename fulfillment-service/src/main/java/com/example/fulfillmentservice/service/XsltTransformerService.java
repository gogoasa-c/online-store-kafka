package com.example.fulfillmentservice.service;

import org.springframework.stereotype.Service;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class XsltTransformerService {

    public String transform(String xmlInput, String xslClasspathResource) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();

            InputStream xslStream = getClass().getClassLoader().getResourceAsStream(xslClasspathResource);
            if (xslStream == null) {
                throw new IllegalArgumentException("XSL resource not found on classpath: " + xslClasspathResource);
            }

            Source xslSource = new StreamSource(xslStream);
            Transformer transformer = factory.newTransformer(xslSource);

            Source xmlSource = new StreamSource(new StringReader(xmlInput));
            StringWriter resultWriter = new StringWriter();
            transformer.transform(xmlSource, new StreamResult(resultWriter));

            return resultWriter.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("XSLT transformation failed for resource: " + xslClasspathResource, e);
        }
    }
}
