package fr.chsfleury.flux.service;

import com.rometools.rome.feed.synd.SyndFeed;
import fr.chsfleury.flux.dto.FeedInput;
import fr.chsfleury.flux.dto.Flux;
import io.vavr.concurrent.Future;

import java.util.Optional;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
public interface FeedService {

    Future<Integer> add(FeedInput input);

    int remove(String url);

    Flux convert(SyndFeed feed);

    Optional<Flux> get(String name, int limit);

}
