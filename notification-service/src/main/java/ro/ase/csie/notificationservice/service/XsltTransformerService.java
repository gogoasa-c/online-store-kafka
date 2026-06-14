package ro.ase.csie.notificationservice.service;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class XsltTransformerService {

    // ConcurrentHashMap retained for thread-safe XSLT template caching across Kafka listener threads
    private final TransformerFactory factory = TransformerFactory.newInstance();
    private final ConcurrentHashMap<String, Templates> cache = new ConcurrentHashMap<>();

    /** Delegates to the parameter-aware overload with an empty parameter map. */
    public String transform(final String xmlInput, final String xslClasspathResource) {
        return transform(xmlInput, xslClasspathResource, Map.of());
    }

    /**
     * Transforms {@code xmlInput} using the compiled XSL stylesheet at the given classpath path.
     * XSLT parameters (e.g. {@code trackingUrlBase}) are injected as stylesheet parameters.
     * Stylesheets are compiled once per path and cached.
     */
    public String transform(final String xmlInput, final String xslClasspathResource,
                            final Map<String, String> params) {
        final Templates templates = cache.computeIfAbsent(xslClasspathResource, this::compile);
        // Try.of wraps checked TransformerException into a composable value (Scala: Try[String])
        return Try.of(() -> {
                    final Transformer transformer = templates.newTransformer();
                    params.forEach(transformer::setParameter);
                    final StringWriter out = new StringWriter();
                    transformer.transform(new StreamSource(new StringReader(xmlInput)), new StreamResult(out));
                    return out.toString();
                })
                .getOrElseThrow(e -> e instanceof RuntimeException re
                        ? re : new RuntimeException("XSLT transformation failed: " + xslClasspathResource, e));
    }

    /**
     * Compiles and returns a reusable {@link Templates} for the given XSL classpath resource.
     * Option.of guards the nullable getResourceAsStream result (Scala analogue: Option[InputStream]).
     */
    private Templates compile(final String xslPath) {
        return Try.of(() -> {
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
