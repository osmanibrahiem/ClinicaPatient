package com.clinica.patient.Activities.Auth.SignUp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Activities.ImagePickerActivity;
import com.clinica.patient.Models.City;
import com.clinica.patient.Models.User;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.CustomTFSpan;
import com.clinica.patient.Tools.ToastTool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

class SignupPresenter extends BasePresenter {

    private SignupActivity activity;
    private SignupView view;
    private FirebaseAuth mAuth;
    private DatabaseReference mReference;
    private StorageReference storageReference;

    private User newUser;
    private static final int REQUEST_IMAGE = 762;
    private Uri imagePath;
    private int cityId = -1;

    SignupPresenter(SignupView view, SignupActivity activity) {
        super(view, activity);
        this.activity = activity;
        this.view = view;
        this.mAuth = FirebaseAuth.getInstance();
        this.mReference = FirebaseDatabase.getInstance().getReference();
        this.storageReference = FirebaseStorage.getInstance().getReference();
//                .child(Constants.Storage.PROFILE_IMAGES_FOLDER + "/" + UUID.randomUUID().toString());

        ImagePickerActivity.clearCache(activity);
        newUser = new User();
    }

    boolean isValidName(String name) {
        if (TextUtils.isEmpty(name)) {
            view.showNameError(R.string.empty_name);
            return false;
        }
        return true;
    }

    boolean isValidEmail(String email) {
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

    boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            view.showPhoneError(R.string.empty_phone);
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            view.showPhoneError(R.string.invalid_phone);
            return false;
        }
        return true;
    }

    boolean isValidBirthday(String birthday) {
        if (TextUtils.isEmpty(birthday)) {
            view.showBirthdayError(R.string.empty_birthday);
            return false;
        }
        Pattern mPattern = Pattern.compile("^(0[1-9]|[12][0-9]|3[01])[-/.](0[1-9]|1[012])[-/.](19|20)\\d\\d$");
        if (!mPattern.matcher(birthday).matches()) {
            view.showBirthdayError(R.string.invalid_birthday);
            return false;
        }
        return true;
    }

    boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            view.showPasswordError(R.string.empty_password);
            return false;
        }
        if (password.trim().length() < 8) {
            view.showPasswordError(R.string.invalid_password_2);
            return false;
        }
        return true;
    }

    boolean isValidPasswordConfirmation(String password, String confirm) {
        if (TextUtils.isEmpty(confirm)) {
            view.showPasswordConfirmationError(R.string.empty_password);
            return false;
        }
        if ((confirm.trim().length() < 8) || (!password.trim().equals(confirm.trim()))) {
            view.showPasswordConfirmationError(R.string.invalid_password_3);
            return false;
        }
        return true;
    }

    boolean isGenderSelected(String selectedGender) {
        String[] ITEMS = activity.getResources().getStringArray(R.array.sex_items);
        if (selectedGender.equals(ITEMS[1]) || selectedGender.equals(ITEMS[2])) {
            return true;
        } else {
            view.showGenderError(R.string.gender_error);
            return false;
        }
    }

    public boolean isCitySelected(String selectedCity) {
        if (TextUtils.isEmpty(selectedCity) || cityId == -1) {
            view.showCityError(R.string.city_error);
            return false;
        }
        return true;
    }

    public void signUpUser(final User user, final String password) {
        view.showLoading();
        String[] genderList = activity.getResources().getStringArray(R.array.sex_items);
        newUser.setDisplayName(user.getDisplayName());
        newUser.setEmail(user.getEmail());
        newUser.setPhoneNumber(user.getPhoneNumber());
        if (user.getGender().equals(genderList[1]))
            newUser.setGender("Female");
        else newUser.setGender("Male");
        mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull final Task<AuthResult> registerTask) {
                        if (registerTask.isSuccessful()) {
                            if (imagePath != null) {
                                final StorageReference imageRef = storageReference.child(Constants.Storage.PROFILE_IMAGES_FOLDER).child(registerTask.getResult().getUser().getUid());
                                imageRef.putFile(imagePath)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                imageRef.getDownloadUrl()
                                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                newUser.setPhotoUrl(uri.toString());
                                                                saveUser(newUser, password, registerTask.getResult().getUser().getUid());
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        view.hideLoading();
                                                        ToastTool.with(activity, e.getLocalizedMessage()).show();
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                view.hideLoading();
                                                ToastTool.with(activity, e.getLocalizedMessage()).show();
                                            }
                                        });
                            } else {
                                saveUser(newUser, password, registerTask.getResult().getUser().getUid());
                            }
                        } else {
                            view.hideLoading();
                            ToastTool.with(activity, registerTask.getException().getLocalizedMessage()).show();
                        }
                    }
                });
    }

    private void saveUser(final User user, final String password, String id) {
        mReference.child(Constants.DataBase.USERS_NODE).child(id)
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> refTask) {
                if (refTask.isSuccessful()) {
                    loginWithEmail(user.getEmail(), password);
                } else {
                    view.hideLoading();
                    ToastTool.with(activity, refTask.getException().getLocalizedMessage()).show();
                }
            }
        });
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, authResultCompleteListener);
    }

    private OnCompleteListener<AuthResult> authResultCompleteListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            view.hideLoading();
            if (task.isSuccessful()) {
                view.openMain();
            } else {
                ToastTool.with(activity, task.getException().getLocalizedMessage()).show();
            }
        }
    };

    public void setSelectedCity(City city) {
        cityId = city.getId();
        newUser.setCityID(cityId);
    }

    public void importImage() {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(activity, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);
        intent.putExtra(ImagePickerActivity.INTENT_SET_CIRCLE_IMAGE, true);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, Constants.PROFILE_IMAGE_RATIO_WIDTH); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, Constants.PROFILE_IMAGE_RATIO_HIGHT);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, Constants.MAXIMUM_PROFILE_IMAGE_WIDTH);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, Constants.MAXIMUM_PROFILE_IMAGE_HIGHT);

        activity.startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);
        intent.putExtra(ImagePickerActivity.INTENT_SET_CIRCLE_IMAGE, true);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, Constants.PROFILE_IMAGE_RATIO_WIDTH); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, Constants.PROFILE_IMAGE_RATIO_HIGHT);
        activity.startActivityForResult(intent, REQUEST_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                imagePath = data.getParcelableExtra("path");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imagePath);
                    view.setProfileImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        Typeface tf = ResourcesCompat.getFont(activity, R.font.typing);
        CustomTFSpan tfSpan = new CustomTFSpan(tf);
        SpannableString spannableString = new SpannableString(activity.getString(R.string.dialog_permission_title));
        spannableString.setSpan(tfSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(spannableString);
        builder.setMessage(activity.getString(R.string.dialog_permission_message));
        builder.setPositiveButton(activity.getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(activity.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, 101);
    }
}
