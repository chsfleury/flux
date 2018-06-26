package fr.chsfleury.flux.domain.repository;

import fr.chsfleury.flux.domain.generated.tables.records.FeedRecord;

import java.util.List;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 */
public interface FeedRepository {

    List<FeedRecord> all();

    List<FeedRecord> findReadyToScan();

    int insert(FeedRecord record);

    int delete(String feedUrl);

}
