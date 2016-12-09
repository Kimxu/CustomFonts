package me.kimxu.texttype;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class CalligraphyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Oswald-Stencbab.ttf")//指定字体
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}