package fr.chsfleury.flux.domain.repository.impl;

import fr.chsfleury.flux.domain.generated.tables.records.FeedRecord;
import fr.chsfleury.flux.domain.repository.FeedRepository;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;
import org.jooq.impl.DSL;

import java.util.List;

import static fr.chsfleury.flux.domain.generated.tables.Feed.FEED;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
public class FeedJooqRepository extends AbstractJooqRepository implements FeedRepository {

    public FeedJooqRepository(final DSLContext jooq) {
        super(jooq);
    }

    @Override
    public List<FeedRecord> all() {
        return jooq
                .selectFrom(FEED)
                .fetch();
    }

    @Override
    public List<FeedRecord> findReadyToScan() {
        return jooq
                .selectFrom(FEED)
                .where(FEED.NEXT_SCAN.lessThan(DSL.currentTimestamp()))
                .fetch();
    }

    @Override
    public int insert(final FeedRecord record) {
        InsertQuery<FeedRecord> insert = jooq.insertQuery(FEED);
        insert.onDuplicateKeyIgnore(true);
        insert.addRecord(record);
        return insert.execute();
    }

    @Override
    public int delete(final String feedUrl) {
        return jooq
            .delete(FEED)
            .where(FEED.URL.eq(feedUrl))
            .execute();
    }

}
