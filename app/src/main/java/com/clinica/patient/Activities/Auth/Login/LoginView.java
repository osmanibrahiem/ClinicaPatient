package com.clinica.patient.Activities.Auth.Login;

import com.clinica.patient.Activities.Base.BaseView;

public interface LoginView extends BaseView {

    void showEmailError(int message);

    void showPasswordError(int message);

    void openSignUp();

    void openMain();

    void openForgetPassword();

}
