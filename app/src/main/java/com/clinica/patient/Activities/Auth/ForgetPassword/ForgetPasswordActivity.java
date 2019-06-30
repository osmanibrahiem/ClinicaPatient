package com.clinica.patient.Activities.Auth.ForgetPassword;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.R;
import com.clinica.patient.Tools.ToastTool;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ForgetPasswordActivity extends BaseActivity implements ForgetPasswordView {

    private ForgetPasswordPresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_email)
    TextInputLayout inputEmail;
    @BindView(R.id.email_et)
    AppCompatEditText emailEt;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_forget_password;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        presenter = new ForgetPasswordPresenter(this, this);
        toolbar.setTitle(getString(R.string.forget_password));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initActions() {

    }

    @OnClick(R.id.reset_btn)
    void resetPassword() {
        presenter.hideKeypad();
        inputEmail.setError(null);
        String email = emailEt.getText().toString().trim();
        if (presenter.isNetworkAvailable() && presenter.isValidEmail(email))
            presenter.sendPasswordResetEmail(email);
    }

    @Override
    public void showEmailError(int message) {
        inputEmail.setError(getText(message));
    }

    @Override
    public void returnBack() {
        ToastTool.with(this, R.string.email_sent_success).show();
        finish();
    }
}
