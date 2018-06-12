package fr.chsfleury.flux;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.server.RatpackServer;

import java.net.URL;

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

    private static String tryRead(Context ctx) throws Throwable {
        String fluxUrlValue = ctx.getRequest().getQueryParams().get("flux");
        URL fluxUrl = new URL(fluxUrlValue);
        XmlReader xmlReader = new XmlReader(fluxUrl);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(xmlReader);
        return feed.toString();
    }

    private static void read(Context ctx) {
        Try.of(() -> tryRead(ctx))
            .onSuccess(ctx::render)
            .onFailure(t -> {
                log.error("error", t);
                ctx.render(t.getMessage());
            });
    }

}
