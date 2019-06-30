package com.clinica.patient.Activities.Auth.ForgetPassword;

import com.clinica.patient.Activities.Base.BaseView;

public interface ForgetPasswordView extends BaseView {

    void showEmailError(int message);

    void returnBack();
}
