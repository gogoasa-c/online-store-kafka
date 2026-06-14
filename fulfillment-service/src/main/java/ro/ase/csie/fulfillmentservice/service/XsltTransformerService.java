package ro.ase.csie.fulfillmentservice.service;

import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.stereotype.Service;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class XsltTransformerService {

    // ConcurrentHashMap kept for thread-safe lazy compilation caching across listener threads
    private final TransformerFactory factory = TransformerFactory.newInstance();
    private final ConcurrentHashMap<String, Templates> cache = new ConcurrentHashMap<>();

    /**
     * Transforms {@code xmlInput} using the compiled XSL stylesheet at the given classpath path.
     * Stylesheets are compiled once and cached.
     */
    public String transform(final String xmlInput, final String xslClasspathResource) {
        final Templates templates = cache.computeIfAbsent(xslClasspathResource, this::compile);
        // Try.of wraps TransformerException (checked) into a composable value (Scala: Try[String])
        return Try.of(() -> {
                    final Transformer transformer = templates.newTransformer();
                    final StringWriter out = new StringWriter();
                    transformer.transform(new StreamSource(new StringReader(xmlInput)), new StreamResult(out));
                    return out.toString();
                })
                .getOrElseThrow(e -> e instanceof RuntimeException re
                        ? re : new RuntimeException("XSLT transformation failed: " + xslClasspathResource, e));
    }

    /**
     * Compiles the XSL stylesheet at the given classpath resource path.
     * Option.of guards the nullable getResourceAsStream result (like Scala's Option[InputStream]).
     */
    private Templates compile(final String xslPath) {
        return Try.of(() -> {
                    // Option wraps the nullable classpath resource (Scala analogue: Option[InputStream])
                    final InputStream in = Option.of(getClass().getClassLoader().getResourceAsStream(xslPath))
                            .getOrElseThrow(() -> new IllegalArgumentException(
                                    "XSL resource not found on classpath: " + xslPath));
                    try (in) {
                        return factory.newTemplates(new StreamSource(in));
                    }
                })
                .getOrElseThrow(e -> e instanceof RuntimeException re
                        ? re : new RuntimeException("Failed to compile XSL: " + xslPath, e));
    }
}
