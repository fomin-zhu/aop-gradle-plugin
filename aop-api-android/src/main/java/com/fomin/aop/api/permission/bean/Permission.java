package com.fomin.aop.api.permission.bean;

/**
 * Created by Fomin on 2018/10/10.
 */
public class Permission {
    private String name;
    private boolean granted;

    public Permission(String name, boolean granted) {
        this.name = name;
        this.granted = granted;
    }

    public boolean isGranted() {
        return granted;
    }

    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Permission that = (Permission) o;

        if (granted != that.granted) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (granted ? 1 : 0);
        return result;
    }
}
