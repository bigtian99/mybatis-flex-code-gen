package com.mybatisflex.plugin.core.functions;

@FunctionalInterface
public interface SimpleFunction<T> {

    void apply(T t);
}
