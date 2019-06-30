package com.clinica.patient.Activities.Auth.SignUp;

import android.graphics.Bitmap;

import com.clinica.patient.Activities.Base.BaseView;

public interface SignupView extends BaseView {

    void setProfileImage(Bitmap img);

    void showNameError(int message);

    void showEmailError(int message);

    void showPhoneError(int message);

    void showBirthdayError(int message);

    void showPasswordError(int message);

    void showPasswordConfirmationError(int message);

    void showGenderError(int message);

    void showCityError(int message);

    void openMain();

}
