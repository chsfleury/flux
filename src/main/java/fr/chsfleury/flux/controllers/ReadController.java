package fr.chsfleury.flux.controllers;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import fr.chsfleury.flux.dto.Article;
import fr.chsfleury.flux.dto.Flux;
import fr.chsfleury.flux.service.FeedService;
import lombok.val;
import ratpack.exec.Promise;
import ratpack.handling.Context;
import ratpack.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

/**
 * @author Charles Fleury
 * @since 28/06/18.
 */
public class ReadController {

    private static SyndFeedOutput output = new SyndFeedOutput();

    public static void get(Context ctx) {
        val feedService = ctx.get(FeedService.class);
        MultiValueMap<String, String> queryParams = ctx.getRequest().getQueryParams();
        String name = queryParams.get("name");
        String limitParam = queryParams.getOrDefault("limit", "20");
        int limit;
        try {
            limit = Integer.valueOf(limitParam);
        } catch (NumberFormatException e) {
            limit = 20;
        }

        Optional<Flux> optFlux = feedService.get(name, limit);
        if (optFlux.isPresent()) {
            Flux flux = optFlux.get();
            SyndFeedImpl feed = new SyndFeedImpl();
            feed.setFeedType("atom_1.0");
            feed.setTitle(flux.getTitle());
            feed.setDescription(flux.getDescription());

            List<SyndEntry> entries = new ArrayList<>();

            for (Article article : flux.getArticles()) {
                SyndEntryImpl entry = new SyndEntryImpl();
                entry.setTitle(article.getTitle());

                SyndContentImpl content = new SyndContentImpl();
                content.setValue(article.getContent());
                entry.setContents(singletonList(content));

                entry.setAuthor(article.getAuthor());
                entry.setUri(article.getUrl());

                List<SyndCategory> categories = new ArrayList<>();

                for (String tag : article.getTags()) {
                    SyndCategoryImpl category = new SyndCategoryImpl();
                    category.setName(tag);
                }

                entry.setCategories(categories);
                entries.add(entry);
            }

            feed.setEntries(entries);

            ctx.render(
                    Promise.sync(() -> output.outputString(feed))
                            .mapError(Throwable::getMessage)
            );
        }

    }

}
