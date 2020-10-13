package org.mallen.test.common.rest.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author mallen
 * @date 8/13/19
 */
public abstract class ResponseType<T> {
    private final Type type;

    protected ResponseType() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        } else {
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    public Type getType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        return this.type.hashCode();
    }

    @Override
    public String toString() {
        return "ParameterizedTypeReference<" + this.type + ">";
    }


}
