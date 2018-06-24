package fr.chsfleury.flux.config;

import com.mitchellbosecke.pebble.PebbleEngine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ratpack.func.Action;
import ratpack.registry.RegistrySpec;
import ratpack.server.RatpackServerSpec;
import ratpack.server.ServerConfigBuilder;

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
}
