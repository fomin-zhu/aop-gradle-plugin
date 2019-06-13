package com.fomin.aop.api.permission.core;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fomin.aop.api.permission.bean.Permission;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by Fomin on 2018/10/10.
 */
public class PermissionsFragment extends Fragment {
    private static final String TAG = PermissionsFragment.class.getSimpleName();
    private static final int REQUEST_CODE = 1001;
    private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();

    public PermissionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @TargetApi(Build.VERSION_CODES.M)
    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CODE) return;
        for (int i = 0; i < permissions.length; i++) {
            PublishSubject<Permission> subject = mSubjects.get(permissions[i]);
            if (subject == null) {
                Log.e(TAG, "Permissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
                return;
            }
            mSubjects.remove(permissions[i]);
            subject.onNext(new Permission(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED));
            subject.onComplete();
        }
    }

    public PublishSubject<Permission> getSubjectByPermission(@NonNull String permission) {
        return mSubjects.get(permission);
    }

    public void setSubjectForPermission(@NonNull String permission, @NonNull PublishSubject<Permission> subject) {
        mSubjects.put(permission, subject);
    }

    public boolean containsByPermission(@NonNull String permission) {
        return mSubjects.containsKey(permission);
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        final Activity activity = getActivity();
        if (getActivity() == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        }
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    boolean isRevoked(String permission) {
        final Activity activity = getActivity();
        if (activity == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        }
        return activity.getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }
}
