package fr.chsfleury.flux.service;

import fr.chsfleury.flux.dto.FeedInput;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
public interface FeedService {

    int add(FeedInput input);

    int remove(String url);

}
