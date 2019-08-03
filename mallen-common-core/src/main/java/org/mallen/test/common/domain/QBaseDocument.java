package org.mallen.test.common.domain;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;

import javax.annotation.Generated;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;


/**
 * QBaseDocument is a Querydsl query type for BaseDocument
 * 用于支撑spring data mongodb使用Querydsl
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QBaseDocument extends BeanPath<BaseDocument> {

    private static final long serialVersionUID = -1510546047L;

    public static final QBaseDocument baseDocument = new QBaseDocument("baseDocument");

    public final DateTimePath<java.util.Date> createdTime = createDateTime("createdTime", java.util.Date.class);

    public final StringPath id = createString("id");

    public final DateTimePath<java.util.Date> updatedTime = createDateTime("updatedTime", java.util.Date.class);

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

