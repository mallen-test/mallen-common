package org.mallen.test.common.domain;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QBaseDocument is a Querydsl query type for BaseDocument
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QBaseDocument extends BeanPath<BaseDocument> {

    private static final long serialVersionUID = -110031736L;

    public static final QBaseDocument baseDocument = new QBaseDocument("baseDocument");

    public final NumberPath<Long> createdTime = createNumber("createdTime", Long.class);

    public final StringPath id = createString("id");

    public final NumberPath<Long> updatedTime = createNumber("updatedTime", Long.class);

    public QBaseDocument(String variable) {
        super(BaseDocument.class, forVariable(variable));
    }

    public QBaseDocument(Path<? extends BaseDocument> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseDocument(PathMetadata metadata) {
        super(BaseDocument.class, metadata);
    }

}

