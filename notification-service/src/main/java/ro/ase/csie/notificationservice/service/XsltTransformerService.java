package ro.ase.csie.notificationservice.service;

import org.springframework.stereotype.Service;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class XsltTransformerService {

    private final TransformerFactory factory = TransformerFactory.newInstance();
    private final ConcurrentHashMap<String, Templates> cache = new ConcurrentHashMap<>();

    public String transform(String xmlInput, String xslClasspathResource) {
        return transform(xmlInput, xslClasspathResource, Map.of());
    }

    public String transform(String xmlInput, String xslClasspathResource, Map<String, String> params) {
        Templates templates = cache.computeIfAbsent(xslClasspathResource, this::compile);
        try {
            Transformer transformer = templates.newTransformer();
            params.forEach(transformer::setParameter);
            StringWriter out = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(xmlInput)), new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("XSLT transformation failed: " + xslClasspathResource, e);
        }
    }

    private Templates compile(String xslPath) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(xslPath)) {
            if (in == null) {
                throw new IllegalArgumentException("XSL resource not found on classpath: " + xslPath);
            }
            return factory.newTemplates(new StreamSource(in));
        } catch (TransformerConfigurationException | IOException e) {
            throw new RuntimeException("Failed to compile XSL: " + xslPath, e);
        }
    }
}
