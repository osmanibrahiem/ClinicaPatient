package com.clinica.patient.Activities.Auth.ForgetPassword;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Patterns;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.R;
import com.clinica.patient.Tools.ToastTool;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordPresenter extends BasePresenter {

    private ForgetPasswordActivity activity;
    private ForgetPasswordView view;
    private FirebaseAuth mAuth;

    public ForgetPasswordPresenter(ForgetPasswordView view, ForgetPasswordActivity activity) {
        super(view, activity);
        this.activity = activity;
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();

    }

    public boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            view.showEmailError(R.string.empty_email);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showEmailError(R.string.invalid_email);
            return false;
        }
        return true;
    }

    public void sendPasswordResetEmail(String email) {
        view.showLoading();
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        view.hideLoading();
                        view.returnBack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        view.hideLoading();
                        ToastTool.with(activity, e.getLocalizedMessage()).show();
                    }
                });
    }
}
