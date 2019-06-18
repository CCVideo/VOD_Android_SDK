package com.bokecc.vod.data;

import android.content.Context;

import io.objectbox.BoxStore;

public class ObjectBox {
    private static BoxStore boxStore;
    public static void init(Context context) {
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
        DataSet.init(boxStore);
    }

    public static BoxStore get() { return boxStore; }
}
