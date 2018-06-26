package fr.chsfleury.flux.service;

import com.rometools.rome.feed.synd.SyndFeed;
import fr.chsfleury.flux.dto.FeedInput;
import fr.chsfleury.flux.dto.Flux;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
public interface FeedService {

    int add(FeedInput input);

    int remove(String url);

    Flux convert(SyndFeed feed);

}
