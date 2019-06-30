package com.clinica.patient.Activities.Auth.SignUp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.clinica.patient.Activities.Selector.SelectorActivity;
import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Main.MainActivity;
import com.clinica.patient.Models.City;
import com.clinica.patient.Models.User;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.DateInputMask;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends BaseActivity implements SignupView {

    private SignupPresenter persenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.img_profile)
    CircularImageView inputImage;
    @BindView(R.id.input_name)
    TextInputLayout inputName;
    @BindView(R.id.name_et)
    AppCompatEditText nameET;
    @BindView(R.id.input_email)
    TextInputLayout inputEmail;
    @BindView(R.id.email_et)
    AppCompatEditText emailET;
    @BindView(R.id.input_phone)
    TextInputLayout inputPhone;
    @BindView(R.id.phone_et)
    AppCompatEditText phoneET;
    @BindView(R.id.input_birthday)
    TextInputLayout inputBirthday;
    @BindView(R.id.birthday_et)
    AppCompatEditText birthdayET;
    @BindView(R.id.input_password)
    TextInputLayout inputPassword;
    @BindView(R.id.password_et)
    AppCompatEditText passwordET;
    @BindView(R.id.input_password_confirmation)
    TextInputLayout inputPasswordConfirmation;
    @BindView(R.id.password_confirmation_et)
    AppCompatEditText passwordConfirmationET;
    @BindView(R.id.input_gender)
    AppCompatSpinner inputGender;
    @BindView(R.id.input_city)
    TextInputLayout inputCity;
    @BindView(R.id.city_et)
    AppCompatEditText cityET;
    @BindView(R.id.signup_btn)
    AppCompatButton signupBtn;

    private static final int selectionCityRequestCode = 1024;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_signup;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        persenter = new SignupPresenter(this, this);
        toolbar.setTitle(getString(R.string.sign_up_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Typeface typeface = emailET.getTypeface();
        if (typeface != null) {
            inputPassword.setTypeface(typeface);
            inputPasswordConfirmation.setTypeface(typeface);
        }
        String[] ITEMS = getResources().getStringArray(R.array.sex_items);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.gender_spinner_header, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inputGender.setAdapter(adapter);

        if (UserData.getLocalization(this) == Localization.ARABIC_VALUE) {
            nameET.setGravity(Gravity.RIGHT);
            emailET.setGravity(Gravity.RIGHT);
            phoneET.setGravity(Gravity.RIGHT);
            birthdayET.setGravity(Gravity.RIGHT);
            passwordET.setGravity(Gravity.RIGHT);
            passwordConfirmationET.setGravity(Gravity.RIGHT);
            cityET.setGravity(Gravity.RIGHT);
        } else {
            nameET.setGravity(Gravity.LEFT);
            emailET.setGravity(Gravity.LEFT);
            phoneET.setGravity(Gravity.LEFT);
            birthdayET.setGravity(Gravity.LEFT);
            passwordET.setGravity(Gravity.LEFT);
            passwordConfirmationET.setGravity(Gravity.LEFT);
            cityET.setGravity(Gravity.LEFT);
        }
        birthdayET.addTextChangedListener(new DateInputMask(birthdayET));
        inputImage.setFocusableInTouchMode(true);
        inputImage.setFocusable(true);
    }

    @Override
    protected void initActions() {

    }

    @OnClick({R.id.img_plus, R.id.img_profile})
    void onProfileImageClick() {
        persenter.importImage();
    }

    @OnClick(R.id.city_et)
    void openCitySelector() {
        Intent intent = new Intent(new Intent(this, SelectorActivity.class));
        intent.putExtra(Constants.Intents.SELECTION_KEY, Constants.Intents.SELECT_CITY);
        startActivityForResult(intent, selectionCityRequestCode);
    }

    @OnClick(R.id.signup_btn)
    void createUser() {
        persenter.hideKeypad();
        inputName.setError(null);
        inputEmail.setError(null);
        inputPhone.setError(null);
        inputPassword.setError(null);
        inputPasswordConfirmation.setError(null);
        inputBirthday.setError(null);
        inputCity.setError(null);
        ((TextView) inputGender.getSelectedView()).setError(null);
        String name = nameET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String phone = phoneET.getText().toString().trim();
        String birthday = birthdayET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirm = passwordConfirmationET.getText().toString().trim();
        String gender = inputGender.getSelectedItem().toString();
        String city = cityET.getText().toString().trim();
        DateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
        User user = new User();
        try {
            long birthdayTimestamp = dateFormat.parse(birthday).getTime();
            user.setBirthdayTimestamp(birthdayTimestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setDisplayName(name);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setGender(gender);
        if (persenter.isNetworkAvailable() &&
                persenter.isValidName(name) &&
                persenter.isValidEmail(email) &&
                persenter.isValidPhone(phone) &&
                persenter.isValidBirthday(birthday) &&
                persenter.isValidPassword(password) &&
                persenter.isValidPasswordConfirmation(password, confirm) &&
                persenter.isGenderSelected(gender) &&
                persenter.isCitySelected(city)) {
            persenter.signUpUser(user, password);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        persenter.onActivityResult(requestCode, resultCode, data);
        if (requestCode == selectionCityRequestCode && resultCode == RESULT_OK) {
            City city = data.getParcelableExtra(Constants.Intents.CITY_DATA);
            if (UserData.getLocalization(this) == Localization.ARABIC_VALUE)
                cityET.setText(city.getTitleAr());
            else cityET.setText(city.getTitleEn());
            persenter.setSelectedCity(city);
        }
    }

    @Override
    public void setProfileImage(Bitmap img) {
        inputImage.setImageBitmap(img);
    }

    @Override
    public void showNameError(int message) {
        inputName.setError(getText(message));
    }

    @Override
    public void showEmailError(int message) {
        inputEmail.setError(getText(message));
    }

    @Override
    public void showPhoneError(int message) {
        inputPhone.setError(getText(message));
    }

    @Override
    public void showPasswordError(int message) {
        inputPassword.setError(getText(message));
    }

    @Override
    public void showPasswordConfirmationError(int message) {
        inputPasswordConfirmation.setError(getText(message));
    }

    @Override
    public void showGenderError(int message) {
        ((TextView) inputGender.getSelectedView()).setError(getString(message));
    }

    @Override
    public void showCityError(int message) {
        inputCity.setError(getText(message));
    }

    @Override
    public void showBirthdayError(int message) {
        inputBirthday.setError(getText(message));
    }

    @Override
    public void openMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
