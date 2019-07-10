package com.livefront.bridge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.livefront.bridge.wrapper.WrapperUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressLint("ApplySharedPref")
class BridgeDelegate {

    private static final String TAG = BridgeDelegate.class.getName();

    private static final String KEY_BUNDLE = "%s_bundle_%s";
    private static final String KEY_UUID = "uuid_%s";

    private boolean mIsClearAllowed = false;
    private boolean mIsFirstRestoreCall = true;
    private Map<String, Bundle> mUuidBundleMap = new HashMap<>();
    private Map<Object, String> mObjectUuidMap = new WeakHashMap<>();
    private SavedStateHandler mSavedStateHandler;
    private SharedPreferences mSharedPreferences;
    private Executor mExecutor;

    BridgeDelegate(@NonNull Context context,
                   @NonNull SavedStateHandler savedStateHandler) {
        mSharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        mSavedStateHandler = savedStateHandler;
        mExecutor = Executors.newSingleThreadExecutor();
        registerForLifecycleEvents(context);
    }

    void clear(@NonNull Object target) {
        if (!mIsClearAllowed) {
            return;
        }
        String uuid = mObjectUuidMap.remove(target);
        if (uuid == null) {
            return;
        }
        clearDataForUuid(target, uuid);
    }

    void clearAll() {
        mUuidBundleMap.clear();
        mObjectUuidMap.clear();
        mSharedPreferences.edit()
                .clear()
                .commit();
    }

    private void clearDataForUuid(@NonNull Object target, @NonNull String uuid) {
        mUuidBundleMap.remove(uuid);
        enqueue(() -> clearDataFromDisk(target, uuid));
    }

    private void clearDataFromDisk(@NonNull Object target, @NonNull String uuid) {
        mSharedPreferences.edit()
                .remove(getKeyForEncodedBundle(target, uuid))
                .commit();
    }

    private String getKeyForEncodedBundle(@NonNull Object target, @NonNull String uuid) {
        return String.format(KEY_BUNDLE, target.getClass().getSimpleName(), uuid);
    }

    private String getKeyForUuid(@NonNull Object target) {
        return String.format(KEY_UUID, target.getClass().getName());
    }

    @Nullable
    private Bundle readFromDisk(@NonNull Object target, @NonNull String uuid) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "readFromDisk() called with: uuid = [" + uuid + "]");
        }
        String encodedString;
        Parcel parcel = Parcel.obtain();

        try {
            encodedString = mSharedPreferences.getString(getKeyForEncodedBundle(target, uuid), null);
            if (encodedString == null) {
                return null;
            }
            byte[] parcelBytes = Base64.decode(encodedString, 0);
            parcel.unmarshall(parcelBytes, 0, parcelBytes.length);
            parcel.setDataPosition(0);
            return parcel.readBundle(BridgeDelegate.class.getClassLoader());
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            parcel.recycle();
        }

        return null;
    }

    @SuppressLint("NewApi")
    private void registerForLifecycleEvents(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Below this version we'll simply never allow clearing because we don't have a great
            // hook for knowing when a config change is happening.
            mIsClearAllowed = false;
            return;
        }
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacksAdapter() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        mIsClearAllowed = true;
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                        // Don't allow clearing during known configuration changes
                        mIsClearAllowed = !activity.isChangingConfigurations();
                    }
                }
        );
    }

    private void clearFirstState() {
        mSharedPreferences.edit()
                .clear()
                .commit();
    }

    void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "restoreInstanceState() called with: target = [" + target + "], state = [" + state + "]");
        }
        boolean isFirstRestoreCall = mIsFirstRestoreCall;
        mIsFirstRestoreCall = false;
        if (state == null) {
            if (isFirstRestoreCall) {
                enqueue(this::clearFirstState);
            }
            return;
        }
        String uuid = mObjectUuidMap.containsKey(target)
                ? mObjectUuidMap.get(target)
                : state.getString(getKeyForUuid(target), null);
        if (uuid == null) {
            return;
        }
        mObjectUuidMap.put(target, uuid);
        Bundle bundle = mUuidBundleMap.containsKey(uuid)
                ? mUuidBundleMap.get(uuid)
                : readFromDisk(target, uuid);

        if (bundle == null) {
            return;
        }
        WrapperUtils.unwrapOptimizedObjects(bundle);
        mSavedStateHandler.restoreInstanceState(target, bundle);
        clearDataForUuid(target, uuid);
    }

    void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "saveInstanceState() called with: target = [" + target + "], state = [" + state + "]");
        }
        String uuid = mObjectUuidMap.get(target);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            mObjectUuidMap.put(target, uuid);
        }
        state.putString(getKeyForUuid(target), uuid);
        Bundle bundle = new Bundle();
        mSavedStateHandler.saveInstanceState(target, bundle);
        if (bundle.isEmpty()) {
            // Don't bother saving empty bundles
            return;
        }
        WrapperUtils.wrapOptimizedObjects(bundle);
        mUuidBundleMap.put(uuid, bundle);

        final String finalUuid = uuid;
        enqueue(() -> writeToDisk(target, finalUuid, bundle));
    }

    private void writeToDisk(@NonNull Object target,
                             @NonNull String uuid,
                             @NonNull Bundle bundle) {
        Parcel parcel = Parcel.obtain();
        parcel.writeBundle(bundle);
        String encodedString;
        try {
            encodedString = Base64.encodeToString(parcel.marshall(), 0);
            mSharedPreferences.edit()
                    .putString(getKeyForEncodedBundle(target, uuid), encodedString)
                    .commit();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Wrote to disk with size: " + encodedString.getBytes().length);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            parcel.recycle();
        }
    }

    private void enqueue(Runnable r) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "enqueue() called with: r = [" + r + "]");
        }
        if (mExecutor != null) {
            mExecutor.execute(r);
        }
    }
}