package fr.chsfleury.flux;

import fr.chsfleury.flux.config.ContextLoader;
import fr.chsfleury.flux.controllers.Handlers;
import lombok.Getter;
import ratpack.server.RatpackServer;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 */
@Getter
class FluxApp {

    private RatpackServer server;

    protected void start() throws Exception {
        server = RatpackServer.start(
                ContextLoader.init()
                        .append(spec -> spec.handlers(Handlers.init()))
        );
    }

}
