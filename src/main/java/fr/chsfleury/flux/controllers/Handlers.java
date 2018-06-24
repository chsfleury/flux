package fr.chsfleury.flux.controllers;

import com.google.common.collect.ImmutableMap;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import fr.chsfleury.flux.dto.Article;
import fr.chsfleury.flux.dto.Flux;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ratpack.exec.Promise;
import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.http.MediaType;
import ratpack.http.client.HttpClient;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 */
@Slf4j
@NoArgsConstructor(staticName = "init")
public class Handlers implements Action<Chain> {

    @Override
    public void execute(final Chain chain) {
        chain
                .files()
                .get(this::defaultPage)
                .get("read", this::read)
                .get("raw_read", this::rawRead);
    }

    private void defaultPage(Context ctx) {
        ctx.render("Hello World!");
    }

    private void read(Context ctx) {
        val template = ctx.get(PebbleEngine.class).getTemplate("static/read.html");
        doRead(ctx)
                .flatMap(feed -> renderTemplate(template, feed))
                .mapError(this::renderError)
                .then(html -> ctx
                        .getResponse()
                        .send(MediaType.TEXT_HTML, html.getBytes())
                );
    }

    private Promise<String> renderTemplate(PebbleTemplate template, SyndFeed feed) {
        return Promise.sync(() -> {
            Map<String, Object> context = ImmutableMap.of(
                    "flux", convert(feed)
            );
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString();
        });
    }

    private Flux convert(SyndFeed syndFeed) {
        Flux flux = new Flux(syndFeed.getTitle(), syndFeed.getDescription());

        for (SyndEntry entry : syndFeed.getEntries()) {
            Article article = new Article(
                    entry.getTitle(),
                    entry.getAuthor(),
                    entry.getContents().get(0).getValue(),
                    entry.getCategories().stream().map(SyndCategory::getName).collect(Collectors.toList())
            );
            flux.getArticles().add(article);
        }

        return flux;
    }

    private void rawRead(Context ctx) {
        ctx.render(
                doRead(ctx)
                        .map(SyndFeed::toString)
                        .mapError(this::renderError)
        );
    }

    private Promise<SyndFeed> doRead(Context ctx) {
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

    private String renderError(Throwable t) {
        log.error("error", t);
        return t.getMessage();
    }

}
