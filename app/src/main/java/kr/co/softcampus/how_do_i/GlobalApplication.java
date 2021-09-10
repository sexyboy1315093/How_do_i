package kr.co.softcampus.how_do_i;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this,"e0e7dd86fbc8cf79f83ef32724a1da87");
    }
}
