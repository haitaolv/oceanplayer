package com.mega.oceanplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageUtil {


    public static final int LANGUAGE_FOLLOW_SYSTEM = 0;
    public static final int LANGUAGE_CHINA = 1;
    public static final int LANGUAGE_US = 2;

    private static final String LOCALE_SP = "LOCALE_SP";
    private static final String LOCALE_SP_KEY = "LOCALE_SP_KEY";
    private static final String TAG = LanguageUtil.class.getSimpleName();

    public static Locale getLanguageById(int id) {
        Locale locale = Locale.getDefault();
        switch (id) {
            case LANGUAGE_FOLLOW_SYSTEM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    locale = Resources.getSystem().getConfiguration().getLocales().get(0);
                }
                else {
                    locale = Resources.getSystem().getConfiguration().locale;
                }
                break;
            case LANGUAGE_US:
                locale = Locale.US;
                break;
            case LANGUAGE_CHINA:
                locale = Locale.CHINA;
                break;
        }
        return locale;
    }

    public static Locale readSavedLocale(Context context) {
        SharedPreferences spLocale = context.getSharedPreferences(LOCALE_SP, Context.MODE_PRIVATE);
        int language = 0;
        try {
            language = spLocale.getInt(LOCALE_SP_KEY, 0);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        LogUtil.d(TAG, "read saved Locale from " + context.toString() + ", locale=" + language);
        return getLanguageById(language);
    }

    public static void saveLocale(Context context, int locale) {
        SharedPreferences spLocal = context.getSharedPreferences(LOCALE_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = spLocal.edit();
        edit.putInt(LOCALE_SP_KEY, locale);
        if(edit.commit()) {
            LogUtil.d(TAG, "save Locale to " + context.toString() + ", locale=" + locale);
        }else {
            LogUtil.d(TAG, "Failed to save Locale to " + context.toString() + ", locale=" + locale);
        }
    }

    public static boolean updateLocale(Context context, Locale locale) {
        if (needUpdateLocale(context, locale)) {
            Resources resources = context.getResources();
            Configuration configuration = resources.getConfiguration();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, displayMetrics);
            return true;
        }
        else {
            LogUtil.d(TAG, "no need to change language: currentLocal=" + getCurrentLocale(context) + ", newLocale=" + locale);
        }
        return false;
    }

    public static boolean needUpdateLocale(Context pContext, Locale newUserLocale) {
        return newUserLocale != null && !getCurrentLocale(pContext).equals(newUserLocale);
    }

    public static Locale getCurrentLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //7.0有多语言设置获取顶部的语言
            Configuration config = context.getResources().getConfiguration();
            locale = config.getLocales().get(0);

            LocaleList ll = config.getLocales();
            for(int i=0; i< ll.size(); i++){
                LogUtil.d(TAG, "Find Locale in current configuration locale list: " + ll.get(i));
            }
        }
        else {
            locale = context.getResources().getConfiguration().locale;
        }

        LogUtil.d(TAG, "current locale is: " + locale);
        return locale;
    }
}
