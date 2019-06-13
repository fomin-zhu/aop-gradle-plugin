package com.fomin.aop.api.permission.bean;

/**
 * Created by Fomin on 2018/10/10.
 */
@FunctionalInterface
public interface Lazy<V> {
    V get();
}
