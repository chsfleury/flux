package fr.chsfleury.flux.controllers;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author Charles Fleury
 * @since 27/06/18.
 */
@Slf4j
public class ControllerUtils {

    public static Promise<String> renderTemplate(PebbleTemplate template, Map<String, Object> context) {
        return Promise.sync(() -> {
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString();
        });
    }

    public static String renderError(Throwable t) {
        log.error("error", t);
        return t.getMessage();
    }

}
