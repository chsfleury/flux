package fr.chsfleury.flux.domain.repository.impl;

import fr.chsfleury.flux.domain.generated.tables.records.ArticleRecord;
import fr.chsfleury.flux.domain.repository.ArticleRepository;
import org.jooq.DSLContext;
import org.jooq.InsertQuery;

import java.util.List;

import static fr.chsfleury.flux.domain.generated.tables.Article.ARTICLE;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
public class ArticleJooqRepository extends AbstractJooqRepository implements ArticleRepository {

    public ArticleJooqRepository(final DSLContext jooq) {
        super(jooq);
    }

    @Override
    public List<ArticleRecord> findByFlux(final String fluxUrl, int limit) {
        return jooq
                .selectFrom(ARTICLE)
                .where(ARTICLE.FLUX_URL.eq(fluxUrl))
                .limit(limit)
                .fetch();
    }

    @Override
    public int insert(final Iterable<ArticleRecord> records) {
        InsertQuery<ArticleRecord> insert = jooq.insertQuery(ARTICLE);
        insert.onDuplicateKeyIgnore(true);
        records.forEach(insert::addRecord);
        return insert.execute();
    }

}
