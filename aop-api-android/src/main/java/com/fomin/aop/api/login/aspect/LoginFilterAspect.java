package com.fomin.aop.api.login.aspect;

import android.content.Context;
import android.util.Log;

import com.fomin.aop.api.execption.AnnotationException;
import com.fomin.aop.api.execption.NoInitException;
import com.fomin.aop.api.login.core.ILogin;
import com.fomin.aop.api.login.core.LoginAssistant;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by Fomin on 2018/8/30.
 */
@Aspect
public class LoginFilterAspect {

    private static final String TAG = "LoginFilterAspect";

    @Pointcut("execution(@com.fomin.aop.api.login.aspect.LoginFilter * *..*.*(..))")
    public void loginFilter() {
    }

    @SuppressWarnings("CheckStyle")
    @Around("loginFilter()")
    public void aroundLoginPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        ILogin login = LoginAssistant.getInstance().getLogin();
        if (login == null) {
            throw new NoInitException("LoginSDK 没有初始化！");
        }

        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new AnnotationException("LoginFilter 注解只能用于方法上");
        }

        MethodSignature methodSignature = (MethodSignature) signature;
        Log.d("Aspect name", String.valueOf(methodSignature.getName()));
        Log.d("Aspect method", String.valueOf(methodSignature.getMethod() == null));
        LoginFilter loginFilter = methodSignature.getMethod().getAnnotation(LoginFilter.class);
        if (loginFilter == null) {
            return;
        }
        Context param = LoginAssistant.getInstance().getContext();
        Log.d("Aspect isLogin", String.valueOf(login.isLogin(param)));
        if (login.isLogin(param)) {
            joinPoint.proceed();
        } else {
            login.login(param, loginFilter.actionDefine());
        }
    }
}
