package com.mega.oceanplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MyBaseActivity extends AppCompatActivity {

    public static String TAG = MyBaseActivity.class.getSimpleName();
    public MusicController musicController = MusicController.getInstance();

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.i(TAG, "call onCreate");
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        //super.attachBaseContext(newBase);
        LogUtil.i(TAG, "call attachBaseContext: " + newBase.toString());
        Context context = languageWork(newBase);
        super.attachBaseContext(newBase);
    }

    private Context languageWork(Context context) {
        // 8.0及以上使用createConfigurationContext设置configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return updateResources(context);
        } else {
            return context;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Context updateResources(Context context) {
        Locale locale = LanguageUtil.readSavedLocale(context);
        if (locale == null) {
            return context;
        }
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        LogUtil.i(TAG, "updateResources: current locale is: " + configuration.getLocales().get(0));
        LogUtil.d(TAG, "updateResources(): new Locale=" + locale);

        //configuration.setLocale(locale);
        //configuration.setLocales(new LocaleList(locale));

        return context.createConfigurationContext(configuration);
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
