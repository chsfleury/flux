package fr.chsfleury.flux.domain.repository;

import fr.chsfleury.flux.domain.generated.tables.records.FeedRecord;

import java.util.List;
import java.util.Optional;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 */
public interface FeedRepository {

    List<FeedRecord> all();
    List<FeedRecord> findReadyToScan();

    Optional<FeedRecord> find(String name);

    int insert(FeedRecord record);

    int delete(String feedUrl);

    int update(FeedRecord record);
}
