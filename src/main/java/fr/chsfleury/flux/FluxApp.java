package fr.chsfleury.flux;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.chsfleury.flux.controllers.HomeController;
import fr.chsfleury.flux.controllers.ReadController;
import fr.chsfleury.flux.domain.repository.ArticleRepository;
import fr.chsfleury.flux.domain.repository.FeedRepository;
import fr.chsfleury.flux.domain.repository.impl.ArticleJooqRepository;
import fr.chsfleury.flux.domain.repository.impl.FeedJooqRepository;
import fr.chsfleury.flux.service.FeedService;
import fr.chsfleury.flux.service.impl.DefaultFeedService;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import ratpack.http.client.HttpClient;
import ratpack.registry.RegistrySpec;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfigBuilder;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 *
 *      https://korben.info/feed > "main > article > div.entry-container"
 *      https://www.lesnumeriques.com/rss.xml
 *
 */
@Getter
class FluxApp {

    protected void start() throws Exception {
        RatpackServer.start(serverSpec ->
                serverSpec
                        .serverConfig(config -> registerConfig(config, Paths.get(System.getProperty("user.dir"))))
                        .registryOf(this::registerBeans)
                        .handlers(chain ->
                                chain
                                        .files()
                                        .get(HomeController::get)
                                        .get("direct_read", ReadController::get))
        );
    }

    private void registerConfig(ServerConfigBuilder config, Path workingDir) {
        config.baseDir(workingDir.resolve("static"));
    }

    private void registerBeans(RegistrySpec r) throws Exception {
        registerPebble(r);
        registerLayers(r);
    }

    private void registerPebble(RegistrySpec r) {
        PebbleEngine engine = new PebbleEngine.Builder()
                .cacheActive(false)
                .build();

        r.add(PebbleEngine.class, engine);
    }

    private void registerLayers(RegistrySpec r) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:./fluxdb");
        config.setUsername("sa");
        config.setPassword("");
        config.setSchema("FLUXDB");
        config.setDriverClassName("org.h2.Driver");
        DataSource dataSource = new HikariDataSource(config);
        r.add(DataSource.class, dataSource);

        DSLContext jooq = new DefaultDSLContext(dataSource, SQLDialect.H2);
        r.add(DSLContext.class, jooq);

        FeedRepository feedRepository = new FeedJooqRepository(jooq);
        r.add(FeedRepository.class, feedRepository);

        ArticleRepository articleRepository = new ArticleJooqRepository(jooq);
        r.add(ArticleRepository.class, articleRepository);

        HttpClient http = HttpClient.of(s -> s
                .poolSize(0)
                .byteBufAllocator(PooledByteBufAllocator.DEFAULT)
                .maxContentLength(Integer.MAX_VALUE)
        );

        FeedService feedService = new DefaultFeedService(http, feedRepository, articleRepository);
        r.add(FeedService.class, feedService);
    }

    public static void main(final String[] args) throws Exception {
        FluxApp app = new FluxApp();
        app.start();
    }

}
