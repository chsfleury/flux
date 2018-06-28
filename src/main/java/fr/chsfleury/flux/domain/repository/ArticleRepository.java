package fr.chsfleury.flux.domain.repository;

import fr.chsfleury.flux.domain.generated.tables.records.ArticleRecord;

import java.util.List;

/**
 * @author Charles Fleury
 * @since 23/06/18.
 */
public interface ArticleRepository {

    List<ArticleRecord> findByFlux(String fluxUrl, int limit);

    int insert(Iterable<ArticleRecord> records);

}
