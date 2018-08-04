package com.criations.anrtesting;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

import icepick.Icepick;


/**
 * @author Sérgio Serra on 04/08/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new ANRWatchDog(2000).start();

        Bridge.initialize(getApplicationContext(), new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                Icepick.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                Icepick.restoreInstanceState(target, state);
            }
        });
    }
}
