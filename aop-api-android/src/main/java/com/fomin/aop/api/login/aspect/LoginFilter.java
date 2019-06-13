package com.fomin.aop.api.login.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Fomin on 2018/8/30.
 */
@SuppressWarnings("CheckStyle")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LoginFilter {
    int actionDefine() default 0;
}
