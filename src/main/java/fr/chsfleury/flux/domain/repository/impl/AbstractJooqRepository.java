package fr.chsfleury.flux.domain.repository.impl;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;

/**
 * @author Charles Fleury
 * @since 24/06/18.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractJooqRepository {

    protected DSLContext jooq;

}
