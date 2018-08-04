package com.criations.anrtesting;

import android.os.Bundle;
import android.os.Parcel;

/**
 * @author Sérgio Serra on 04/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
final class Utils {

    private Utils() {
    }

    static int getBundleSizeInBytes(Bundle bundle) {
        Parcel parcel = Parcel.obtain();
        int size;

        parcel.writeBundle(bundle);
        size = parcel.dataSize();
        parcel.recycle();

        return size;
    }

}
