package com.clinica.patient.Activities;

import android.Manifest;
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
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Base.BaseFragment;
import com.clinica.patient.Activities.Doctor.DoctorCallBack;
import com.clinica.patient.Activities.Doctor.DoctorQuery;
import com.clinica.patient.Activities.Selector.SelectorActivity;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.Models.NotificationModel;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.CustomTFSpan;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.clinica.patient.Tools.ToastTool;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class AskFragment extends BaseFragment {

    private static final int REQUEST_IMAGE = 911;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_message)
    AppCompatEditText inputMessage;
    @BindView(R.id.specialization)
    AppCompatEditText specialization;
    @BindView(R.id.img_container)
    ConstraintLayout imgContainer;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.choose_btn)
    AppCompatButton chooseBtn;
    @BindView(R.id.ask_btn)
    AppCompatButton askBtn;

    private String specializationID;
    private Specialization QuestionSpecialization;
    private Uri imagePath;
    private final int selectionSpecialtyRequestCode = 808;

    private Consultation thisConsultation;

    public AskFragment() {
    }

    @Override
    protected int setLayoutView() {
        return R.layout.fragment_ask;
    }

    @Override
    protected void initViews(View view) {
        ButterKnife.bind(this, view);
        if (getActivity() != null) {
            toolbar.setTitle(R.string.questions_title);
            ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
            ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        thisConsultation = new Consultation();
    }

    @Override
    protected void initActions() {
        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(inputMessage.getText().toString().trim()) ||
                        TextUtils.isEmpty(specializationID)) {
                    askBtn.setEnabled(false);
                } else {
                    askBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @OnClick(R.id.specialization)
    void getSpecialization() {
        Intent intent = new Intent(new Intent(getActivity(), SelectorActivity.class));
        intent.putExtra(Constants.Intents.SELECTION_KEY, Constants.Intents.SELECT_SPECIALIZATION);
        startActivityForResult(intent, selectionSpecialtyRequestCode);
    }

    @OnClick({R.id.image, R.id.img_container, R.id.choose_btn})
    void importImage() {
        Dexter.withActivity(getActivity())
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
        ImagePickerActivity.showImagePickerOptions(getActivity(), new ImagePickerActivity.PickerOptionListener() {
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
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);
        intent.putExtra(ImagePickerActivity.INTENT_SET_CIRCLE_IMAGE, false);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, Constants.PROFILE_IMAGE_RATIO_WIDTH); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, Constants.PROFILE_IMAGE_RATIO_HIGHT);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, Constants.MAXIMUM_PROFILE_IMAGE_WIDTH);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, Constants.MAXIMUM_PROFILE_IMAGE_HIGHT);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(getActivity(), ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);
        intent.putExtra(ImagePickerActivity.INTENT_SET_CIRCLE_IMAGE, false);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, Constants.PROFILE_IMAGE_RATIO_WIDTH); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, Constants.PROFILE_IMAGE_RATIO_HIGHT);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == selectionSpecialtyRequestCode && resultCode == RESULT_OK) {
            QuestionSpecialization = data.getParcelableExtra(Constants.Intents.SPECIALIZATION_DATA);
            specializationID = QuestionSpecialization.getId();
            if (UserData.getLocalization(getActivity()) == Localization.ARABIC_VALUE)
                specialization.setText(QuestionSpecialization.getTitleAr());
            else specialization.setText(QuestionSpecialization.getTitleEn());
        } else if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            imagePath = data.getParcelableExtra("path");
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagePath);
                image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Typeface tf = ResourcesCompat.getFont(getActivity(), R.font.typing);
        CustomTFSpan tfSpan = new CustomTFSpan(tf);
        SpannableString spannableString = new SpannableString(getString(R.string.dialog_permission_title));
        spannableString.setSpan(tfSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(spannableString);
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @OnClick(R.id.ask_btn)
    void ask() {
        if (isNetworkAvailable())
            showLoading();
        if (imagePath != null) {
            uploadImage();
        } else {
            saveConsultation();
        }
    }

    private void uploadImage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference imageRef = storageReference.child(Constants.Storage.QUESTIONS_IMAGES_FOLDER)
                .child(FirebaseAuth.getInstance().getUid());
        imageRef.putFile(imagePath)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoading();
                        ToastTool.with(getActivity(), R.string.error_happened_2).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        hideLoading();
                                        ToastTool.with(getActivity(), R.string.error_happened_2).show();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        thisConsultation.setImage(uri.toString());

                                        saveConsultation();
                                    }
                                });
                    }
                });
    }

    private void saveConsultation() {
        thisConsultation.setQuestion(inputMessage.getText().toString().trim());
        thisConsultation.setQuestionDate(new Date().getTime());
        thisConsultation.setQuestionPublisherID(FirebaseAuth.getInstance().getUid());
        thisConsultation.setSpecializationID(specializationID);
        thisConsultation.setStatus(Consultation.STATUS_ACTIVE);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final String questionID = reference.child(Constants.DataBase.Consultation_NODE).push().getKey();
        reference.child(Constants.DataBase.Consultation_NODE).child(questionID).setValue(thisConsultation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideLoading();
                        ToastTool.with(getActivity(), R.string.error_happened_2).show();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideLoading();
                        ToastTool.with(getActivity(), R.string.question_saved_success).show();
                        sendNotificationToDoctors(questionID);
                        getActivity().onBackPressed();
                    }
                });
    }

    private void sendNotificationToDoctors(final String questionID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(Constants.DataBase.Doctors_NODE);
        new DoctorQuery((BaseActivity) getActivity(), query)
                .withSpecialization(QuestionSpecialization)
                .withCallBack(new DoctorCallBack() {
                    @Override
                    public void onGetData(List<Doctor> doctors) {
                        if (doctors != null && doctors.size() != 0) {
                            for (Doctor doctor : doctors) {
                                sendNotificationToDoctor(doctor.getUid(), NotificationModel.USER_REQUESTED_QUESTION,
                                        Constants.DataBase.Consultation_NODE, questionID);
                            }
                        }
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
    }

    public void sendNotificationToDoctor(String doctorID, String type, String catID, String destinationID) {
        NotificationModel notification = new NotificationModel();
        notification.setCatID(catID);
        notification.setDestinationID(destinationID);
        notification.setFrom(FirebaseAuth.getInstance().getUid());
        notification.setTo(doctorID);
        notification.setDate(new Date().getTime());
        notification.setType(type);

        FirebaseDatabase.getInstance().getReference().child(Constants.DataBase.Notifications_NODE)
                .push().setValue(notification);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager mConnectivity =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivity.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            ToastTool.with(getActivity(), R.string.message_no_internet).show();
            return false;
        }
    }
}
