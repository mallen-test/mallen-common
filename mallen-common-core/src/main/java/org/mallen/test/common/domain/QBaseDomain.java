package org.mallen.test.common.domain;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QBaseDomain is a Querydsl query type for BaseDomain
 * 用于支撑querydsl的查询
 */
@Generated("com.querydsl.codegen.SupertypeSerializer")
public class QBaseDomain extends EntityPathBase<BaseDomain> {

    private static final long serialVersionUID = -983187736L;

    public static final QBaseDomain baseDomain = new QBaseDomain("baseDomain");

    public final NumberPath<Long> createdTime = createNumber("createdTime", Long.class);

    public final NumberPath<Long> updatedTime = createNumber("updatedTime", Long.class);

    public QBaseDomain(String variable) {
        super(BaseDomain.class, forVariable(variable));
    }

    public QBaseDomain(Path<? extends BaseDomain> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseDomain(PathMetadata metadata) {
        super(BaseDomain.class, metadata);
    }

}

