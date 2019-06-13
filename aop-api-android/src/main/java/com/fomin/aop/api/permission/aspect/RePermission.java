package com.fomin.aop.api.permission.aspect;

import android.support.annotation.StringRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Fomin on 2018/10/10.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RePermission {
    String[] value();// 权限值

    String tps() default "";// 文本提示

    @StringRes int resId() default 0;// 文本提示资源ID
}
