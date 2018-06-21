package fr.chsfleury.flux;

import com.google.common.collect.ImmutableMap;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ratpack.exec.Promise;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.http.MediaType;
import ratpack.http.client.HttpClient;
import ratpack.registry.RegistrySpec;
import ratpack.server.RatpackServer;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FluxServer {

    /*
     * https://korben.info/feed > "main > article > div.entry-container"
     * https://www.lesnumeriques.com/rss.xml
     */

    public static void main(final String[] args) throws Exception {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        RatpackServer
            .start(server ->
                server
                    .serverConfig(config ->
                        config.baseDir(workingDir.resolve("static"))
                    )
                    .handlers(FluxServer::endpoints)
                    .registryOf(FluxServer::registerPebble)
            );
    }

    private static void registerPebble(RegistrySpec r) {
        PebbleEngine engine = new PebbleEngine.Builder()
            .cacheActive(false)
            .build();
        r.add(PebbleEngine.class, engine);
    }

    private static void endpoints(Chain chain) {
        chain
            .files()
            .get(FluxServer::defaultPage)
            .get("read", FluxServer::read)
            .get("raw_read", FluxServer::rawRead);
    }

    private static void defaultPage(Context ctx) {
        ctx.render("Hello World!");
    }

    private static void read(Context ctx) {
        val template = ctx.get(PebbleEngine.class).getTemplate("static/read.html");
        doRead(ctx)
            .flatMap(feed -> renderTemplate(template, feed))
            .mapError(FluxServer::renderError)
            .then(html -> ctx
                .getResponse()
                .send(MediaType.TEXT_HTML, html.getBytes())
            );
    }

    private static Promise<String> renderTemplate(PebbleTemplate template, SyndFeed feed) {
        return Promise.sync(() -> {
            Map<String, Object> context = ImmutableMap.of(
                "flux", convert(feed)
            );
            Writer writer = new StringWriter();
            template.evaluate(writer, context);
            return writer.toString();
        });
    }

    private static Flux convert(SyndFeed syndFeed) {
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

    private static void rawRead(Context ctx) {
        ctx.render(
            doRead(ctx)
                .map(SyndFeed::toString)
                .mapError(FluxServer::renderError)
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

    private static String renderError(Throwable t) {
        log.error("error", t);
        return t.getMessage();
    }
}
