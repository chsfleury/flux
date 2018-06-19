package fr.chsfleury.flux;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import ratpack.exec.Promise;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.http.client.HttpClient;
import ratpack.server.RatpackServer;

import java.net.URI;

@Slf4j
public class Flux {

    /*
     * https://korben.info/feed
     * https://www.lesnumeriques.com/rss.xml
     */

    public static void main(final String[] args) throws Exception {
        RatpackServer.start(server -> server.handlers(Flux::endpoints));
    }

    private static void endpoints(Chain chain) {
        chain
            .get(Flux::defaultPage)
            .get("read", Flux::read);
    }

    private static void defaultPage(Context ctx) {
        ctx.render("Hello World!");
    }

    private static void read(Context ctx) {
        ctx.render(
            doRead(ctx)
                .map(SyndFeed::toString)
                .mapError(Flux::renderError)
        );
    }

    private static String renderError(Throwable t) {
        log.error("error", t);
        return t.getMessage();
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
