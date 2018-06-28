package fr.chsfleury.flux.controllers;

import fr.chsfleury.flux.dto.FeedInput;
import fr.chsfleury.flux.service.FeedService;
import ratpack.config.ConfigData;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.http.client.HttpClient;

import java.net.URI;

import static com.google.common.io.ByteSource.wrap;

public class AddController {

    public static void get(Context ctx) {
        Promise
            .sync(() -> new URI(ctx.getRequest().getQueryParams().get("url")))
            .flatMap(uri -> ctx
                .get(HttpClient.class)
                .get(uri)
            ).map(response -> {
            ConfigData configData = ConfigData
                .builder()
                .yaml(wrap(response.getBody().getBytes()))
                .build();
            FeedInput input = configData.get(FeedInput.class);
            return ctx.get(FeedService.class).add(input).get() == 1 ? "ok" : "ko";
        })
            .then(ctx::render);
    }

}
