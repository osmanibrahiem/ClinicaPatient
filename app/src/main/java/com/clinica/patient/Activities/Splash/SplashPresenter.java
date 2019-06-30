package com.clinica.patient.Activities.Splash;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.google.firebase.auth.FirebaseAuth;

public class SplashPresenter extends BasePresenter {

    private SplashActivity activity;
    private SplashView view;

    private static final int TIME_SPLASH_CLOSE = 10000;//millis
    private static final int TIME_ANIMATION_DURATION = 3000;//millis
    private static final int TIME_ANIMATION_START = 500;//millis

    SplashPresenter(SplashView view, SplashActivity activity) {
        super(view, activity);
        this.activity = activity;
        this.view = view;
    }

    void animateLogo(View imgv) {
        imgv.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(TIME_ANIMATION_START);
        fadeIn.setDuration(TIME_ANIMATION_DURATION);
        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        imgv.setAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.openNextActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    void setLanguage() {
        if (UserData.getLocalization(activity) == -1) { // no found lang before .. first set up application
            if (Localization.getDefaultLocal(activity) == Localization.ARABIC_VALUE) { //RTL
                UserData.saveLocalization(activity, Localization.ARABIC_VALUE);
            } else {
                UserData.saveLocalization(activity, Localization.ENGLISH_VALUE);
            }
        }
        setLanguage(UserData.getLocalization(activity));
    }

    void setLanguage(int language) {
        Localization.setLanguage(activity, language);
    }

    boolean checkLogin() {
        return FirebaseAuth.getInstance().getCurrentUser() == null;
    }
}
