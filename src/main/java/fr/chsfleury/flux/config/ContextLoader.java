package fr.chsfleury.flux.config;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.chsfleury.flux.domain.repository.ArticleRepository;
import fr.chsfleury.flux.domain.repository.FeedRepository;
import fr.chsfleury.flux.domain.repository.impl.ArticleJooqRepository;
import fr.chsfleury.flux.domain.repository.impl.FeedJooqRepository;
import fr.chsfleury.flux.service.FeedService;
import fr.chsfleury.flux.service.impl.DefaultFeedService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDSLContext;
import ratpack.func.Action;
import ratpack.registry.RegistrySpec;
import ratpack.server.RatpackServerSpec;
import ratpack.server.ServerConfigBuilder;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 */
@Getter
@NoArgsConstructor(staticName = "init")
public class ContextLoader implements Action<RatpackServerSpec> {

    private Path workingDir;
    private RatpackServerSpec spec;

    @Override
    public void execute(final RatpackServerSpec serverSpec) throws Exception {
        workingDir = Paths.get(System.getProperty("user.dir"));
        spec = serverSpec
                .serverConfig(this::registerConfig)
                .registryOf(this::registerPebble);
    }

    private void registerConfig(ServerConfigBuilder config) {
        config.baseDir(workingDir.resolve("static"));
    }

    private void registerPebble(RegistrySpec r) {
        PebbleEngine engine = new PebbleEngine.Builder()
                .cacheActive(false)
                .build();

        r.add(PebbleEngine.class, engine);
    }

    private void registerRepository(RegistrySpec r) {

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
    }

    private void registerServices(RegistrySpec r) {
        FeedService feedService = new DefaultFeedService()
    }
}
