package com.clinica.patient.Activities.Auth.Login;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.ImageView;

import com.clinica.patient.Activities.Auth.ForgetPassword.ForgetPasswordActivity;
import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Main.MainActivity;
import com.clinica.patient.Activities.Auth.SignUp.SignupActivity;
import com.clinica.patient.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity implements LoginView {

    private LoginPresenter presenter;

    @BindView(R.id.input_email)
    TextInputLayout inputEmail;
    @BindView(R.id.email_et)
    AppCompatEditText emailET;
    @BindView(R.id.input_password)
    TextInputLayout inputPassword;
    @BindView(R.id.password_et)
    AppCompatEditText passwordET;
    @BindView(R.id.forget_password_btn)
    AppCompatButton forgetPasswordBtn;
    @BindView(R.id.login_btn)
    AppCompatButton loginBtn;
    @BindView(R.id.signup_btn)
    AppCompatButton signupBtn;
    @BindView(R.id.fb_btn)
    ImageView fbBtn;
    @BindView(R.id.google_btn)
    ImageView googleBtn;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        presenter = new LoginPresenter(this, this);

        Typeface typeface = emailET.getTypeface();
        if (typeface != null)
            inputPassword.setTypeface(typeface);
    }

    @Override
    protected void initActions() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.hideKeypad();
                inputEmail.setError(null);
                inputPassword.setError(null);
                String email = emailET.getText().toString().trim();
                String password = passwordET.getText().toString().trim();
                if (presenter.isNetworkAvailable() &&
                        presenter.isValidEmail(email) &&
                        presenter.isValidPassword(password)) {
                    presenter.loginWithEmail(email, password);
                }
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignUp();
            }
        });

        forgetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgetPassword();
            }
        });

        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter.isNetworkAvailable())
                    presenter.loginWithFacebook();
            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter.isNetworkAvailable())
                    presenter.loginWithGoogle();
            }
        });
    }

    @Override
    public void showEmailError(int message) {
        inputEmail.setError(getText(message));
    }

    @Override
    public void showPasswordError(int message) {
        inputPassword.setError(getText(message));
    }

    @Override
    public void openSignUp() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    @Override
    public void openMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void openForgetPassword() {
        startActivity(new Intent(this, ForgetPasswordActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
