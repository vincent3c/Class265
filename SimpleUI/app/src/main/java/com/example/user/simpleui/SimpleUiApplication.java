package com.example.user.simpleui;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by user on 2016/5/5.
 */
public class SimpleUiApplication  extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                        .applicationId("9V2ClzWRelpP8hqLT7GrodvDlrpDlS1XOqN46T89")  // 辨識你是用那個app的id
//                        .applicationId("1IIbdKr6rgMlclbPtrTcibogTq4wst4GVJC2dOX2")  //作業用
                        .clientKey("5RXPj278ebCDByhSvgOg5kH9eTe5xddR6KXmZ3kR")  // 註冊時就會給你一個獨特的id，這是辨識你是誰用的，一個帳號只有一個key
//                        .clientKey("lYpFFD6Bz8mendveOW91UvjypoGruuaaQPc4EUyR")  //作業用
                        .server("https://parseapi.back4app.com/")  // SEVER位置
                        .build()
        );
    }
}
