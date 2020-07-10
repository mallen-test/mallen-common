package org.mallen.test.common.mongodb.querydsl;

import com.mongodb.lang.Nullable;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Visitor;

/**
 * 由于Querysdl无法根据某个字段是否为null查询（isNull或者isNotNull使用的是exists查询），所以需要使用该类来表示null值。
 * 使用方法：qEntity.field.eq(NullConstant.NULL_CONSTANT)
 * 感谢github用户butzy92，issue地址：https://github.com/querydsl/querydsl/issues/2173
 *
 * @author mallen
 * @date 7/8/20
 */
public class NullConstant implements Constant {

    public static final NullConstant NULL_CONSTANT = new NullConstant();

    private NullConstant() {
    }

    @Override
    public Object getConstant() {
        return null;
    }

    @Nullable
    @Override
    public Object accept(Visitor v, @Nullable Object context) {
        return null;
    }

    @Override
    public Class getType() {
        return Object.class;
    }
}