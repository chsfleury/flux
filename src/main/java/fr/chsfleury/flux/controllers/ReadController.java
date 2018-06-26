package fr.chsfleury.flux.controllers;

import com.google.common.collect.ImmutableMap;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import fr.chsfleury.flux.service.FeedService;
import lombok.val;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.http.MediaType;
import ratpack.http.client.HttpClient;

import java.net.URI;

import static fr.chsfleury.flux.controllers.ControllerUtils.renderTemplate;

/**
 * @author Charles Fleury
 * @since 27/06/18.
 */
public class ReadController {

    public static void get(Context ctx) {
        val template = ctx.get(PebbleEngine.class).getTemplate("static/read.html");
        val feedService = ctx.get(FeedService.class);
        doRead(ctx)
                .flatMap(feed -> renderTemplate(template, ImmutableMap.of("flux", feedService.convert(feed))))
                .mapError(ControllerUtils::renderError)
                .then(html -> ctx
                        .getResponse()
                        .send(MediaType.TEXT_HTML, html.getBytes())
                );
    }

    private static Promise<SyndFeed> doRead(Context ctx) {
        return Promise
                .sync(() -> new URI(ctx.getRequest().getQueryParams().get("flux")))
                .flatMap(uri -> ctx
                        .get(HttpClient.class)
                        .get(uri)
                ).map(response -> {
                    XmlReader reader = new XmlReader(response.getBody().getInputStream());
                    SyndFeedInput input = new SyndFeedInput();
                    return input.build(reader);
                });
    }

}
