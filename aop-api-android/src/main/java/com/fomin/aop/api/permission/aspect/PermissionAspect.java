package com.fomin.aop.api.permission.aspect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fomin.aop.api.execption.AnnotationException;
import com.fomin.aop.api.permission.core.PermissionAssistant;
import com.fomin.aop.api.permission.core.PermissionRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import io.reactivex.functions.Consumer;

/**
 * Created by Fomin on 2018/10/11.
 */
@Aspect
public class PermissionAspect {

    @Pointcut("execution(@com.fomin.aop.api.permission.aspect.RePermission * *..*.*(..))")
    public void rePermission() {
    }

    @SuppressLint("CheckResult")
    @Around("rePermission()")
    public void aroundPermission(final ProceedingJoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new AnnotationException("RePermission 注解只能用于方法上");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        final RePermission rePermission = methodSignature.getMethod().getAnnotation(RePermission.class);
        if (rePermission == null || rePermission.value().length == 0) {
            return;
        }
        Activity activity;
        final Object object = joinPoint.getThis();
        if (object instanceof Activity) {
            activity = (Activity) object;
        } else if (object instanceof Fragment) {
            activity = ((Fragment) object).getActivity();
        } else if (object instanceof android.support.v4.app.Fragment) {
            activity = ((android.support.v4.app.Fragment) object).getActivity();
        } else {
            activity = PermissionAssistant.getInstance().getActivity();
        }
        if (activity == null) return;
        Log.d("Permissions", activity.getLocalClassName());
        final Context context = activity;
        new PermissionRequest(activity).request(rePermission.value())
                .subscribe(new Consumer<Boolean>() {

                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.d("Permissions", "accept:" + aBoolean);

                        if (aBoolean) {
                            try {
                                joinPoint.proceed();
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        } else if (rePermission.tps().length() > 0) {
                            Toast.makeText(context, rePermission.tps(), Toast.LENGTH_SHORT).show();
                        } else if (rePermission.resId() != 0) {
                            Toast.makeText(context, context.getResources().getString(rePermission.resId()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("Permission", throwable.toString());
                    }
                });
    }
}
