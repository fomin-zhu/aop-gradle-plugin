package com.fomin.aop.api.permission.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.app.FragmentManager;

import com.fomin.aop.api.permission.bean.Lazy;
import com.fomin.aop.api.permission.bean.Permission;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Fomin on 2018/10/10.
 */
public class PermissionRequest {

    private static final String TAG = PermissionRequest.class.getSimpleName();
    private static final Object TRIGGER = new Object();
    private Lazy<PermissionsFragment> mFragment;

    public PermissionRequest(Activity activity) {
        mFragment = getLazySingleton(activity.getFragmentManager());
    }

    private Lazy<PermissionsFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new Lazy<PermissionsFragment>() {
            private PermissionsFragment fragment;

            @Override
            public PermissionsFragment get() {
                if (fragment == null) {
                    fragment = getFragment(fragmentManager);
                }
                return fragment;
            }
        };
    }

    private PermissionsFragment getFragment(@NonNull FragmentManager fragmentManager) {
        PermissionsFragment fragment = (PermissionsFragment) fragmentManager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new PermissionsFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public Observable<Boolean> request(String... permissions) {
        return Observable.just(TRIGGER).compose(ensure(permissions));
    }

    /**
     * Map emitted items from the source observable into {@code true} if permissions in parameters
     * are granted, or {@code false} if not.
     * <p>
     * If one or several permissions have never been requested, invoke the related framework method
     * to ask the user if he allows the permissions.
     */
    @SuppressWarnings("WeakerAccess")
    public <T> ObservableTransformer<T, Boolean> ensure(final String... permissions) {
        return new ObservableTransformer<T, Boolean>() {
            @Override
            public ObservableSource<Boolean> apply(Observable<T> o) {
                return request(o, permissions)
                        // Transform Observable<Permission> to Observable<Boolean>
                        .buffer(permissions.length)
                        .flatMap(new Function<List<Permission>, ObservableSource<Boolean>>() {
                            @Override
                            public ObservableSource<Boolean> apply(List<Permission> permissions) {
                                if (permissions.isEmpty()) {
                                    // Occurs during orientation change, when the subject receives onComplete.
                                    // In that case we don't want to propagate that empty list to the
                                    // subscriber, only the onComplete.
                                    return Observable.empty();
                                }
                                // Return true if all permissions are granted.
                                for (Permission p : permissions) {
                                    if (!p.isGranted()) {
                                        return Observable.just(false);
                                    }
                                }
                                return Observable.just(true);
                            }
                        });
            }
        };
    }

    private Observable<Permission> request(final Observable<?> trigger, final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("Permissions.request requires at least one input permission");
        }
        return oneOf(trigger, pending(permissions))
                .flatMap(new Function<Object, Observable<Permission>>() {
                    @Override
                    public Observable<Permission> apply(Object o) {
                        return requestImplementation(permissions);
                    }
                });
    }

    private Observable<?> pending(final String... permissions) {
        for (String p : permissions) {
            if (!mFragment.get().containsByPermission(p)) {
                return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        if (trigger == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(trigger, pending);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private Observable<Permission> requestImplementation(final String... permissions) {
        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();

        // In case of multiple permissions, we create an Observable for each of them.
        // At the end, the observables are combined to have a unique response.
        for (String permission : permissions) {
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                list.add(Observable.just(new Permission(permission, true)));
                continue;
            }

            if (isRevoked(permission)) {
                // Revoked by a policy, return a denied Permission object.
                list.add(Observable.just(new Permission(permission, false)));
                continue;
            }

            PublishSubject<Permission> subject = mFragment.get().getSubjectByPermission(permission);
            // Create a new subject if not exists
            if (subject == null) {
                unrequestedPermissions.add(permission);
                subject = PublishSubject.create();
                mFragment.get().setSubjectForPermission(permission, subject);
            }

            list.add(subject);
        }

        if (!unrequestedPermissions.isEmpty()) {
            mFragment.get().requestPermissions(unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]));
        }
        return Observable.concat(Observable.fromIterable(list));
    }

    private boolean isAllowVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Returns true if the permission is already granted.
     * <p>
     * Always true if SDK &lt; 23.
     */
    @SuppressWarnings("WeakerAccess")
    private boolean isGranted(String permission) {
        return !isAllowVersion() || mFragment.get().isGranted(permission);
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     * <p>
     * Always false if SDK &lt; 23.
     */
    @SuppressWarnings("WeakerAccess")
    private boolean isRevoked(String permission) {
        return isAllowVersion() && mFragment.get().isRevoked(permission);
    }
}
