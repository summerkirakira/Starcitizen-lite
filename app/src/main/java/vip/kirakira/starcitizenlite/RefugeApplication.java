package vip.kirakira.starcitizenlite;

import android.app.Application;
import kotlinx.coroutines.Dispatchers;

import static kotlinx.coroutines.CoroutineScopeKt.CoroutineScope;

public class RefugeApplication extends Application {
    private static RefugeApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE).edit().putBoolean(getString(R.string.CHECK_UPDATE_KEY), true).apply();
        instance = this;
    }

    public static RefugeApplication getInstance() {
        return instance;
    }
}
