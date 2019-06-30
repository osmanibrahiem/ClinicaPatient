package com.clinica.patient.Activities.Doctor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Models.City;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.R;
import com.clinica.patient.Tools.CustomTFSpan;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DoctorQuery implements ValueEventListener {

    private final String TAG = "DoctorQuery";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Represents a geographical location.
     */
    private Location mLastLocation;

    private Query query;
    private Specialization specialization;
    private City city;
    private BaseActivity activity;
    private DoctorCallBack callBack;
    private List<Doctor> doctorList, fullList;


    public DoctorQuery(BaseActivity activity, Query query) {
        this.activity = activity;
        this.query = query;
        this.doctorList = new ArrayList<>();
        this.fullList = new ArrayList<>();

        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getAddress();
        }
    }

    public DoctorQuery withSpecialization(Specialization specialization) {
        this.specialization = specialization;
        return this;
    }

    public DoctorQuery withLocation(City city) {
        this.city = city;
        return this;
    }

    public void withCallBack(DoctorCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        doctorList.clear();
        fullList.clear();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Doctor doctor = snapshot.getValue(Doctor.class);
            if (doctor != null) {
                doctor.setUid(snapshot.getKey());

                addDoctorToList(doctor);
            }
        }

        if (doctorList.isEmpty()) {
            for (int i = 0; i < 10 && i < fullList.size(); i++) {
                doctorList.add(fullList.get(i));
            }
        } else if (doctorList.size() < 10) {
            Doctor lastDoctor = doctorList.get(doctorList.size() - 1);
            int i = 0;
            for (; i < fullList.size(); i++) {
                if (fullList.get(i).getUid().equals(lastDoctor.getUid()))
                    break;
            }
            for (int j = i + 1; doctorList.size() < 10 && j < fullList.size(); j++) {
                doctorList.add(fullList.get(j));
            }
        }
        if (callBack != null)
            callBack.onGetData(doctorList);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        if (callBack != null)
            callBack.onError(activity.getString(R.string.empty_list));
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        Dexter.withActivity(activity)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            getAddress();
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

    @SuppressWarnings("MissingPermission")
    private void getAddress() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.w(TAG, "onSuccess:null");
                            if (callBack != null)
                                callBack.onError(activity.getString(R.string.location_error_happened));
                            return;
                        }

                        mLastLocation = location;

                        getDoctors();
                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getLastLocation:onFailure", e);
                        if (callBack != null)
                            callBack.onError(activity.getString(R.string.location_error_happened));
                    }
                });
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

    private void getDoctors() {
        query.addListenerForSingleValueEvent(this);
    }

    private void sortList(List<Doctor> list) {
        Collections.sort(list, new Comparator<Doctor>() {
            @Override
            public int compare(Doctor doctor1, Doctor doctor2) {

                float[] result1 = new float[3];
                Location.distanceBetween(
                        mLastLocation.getLatitude(),
                        mLastLocation.getLongitude(),
                        doctor1.getClinicInformation().getAddressEn().getLat(),
                        doctor1.getClinicInformation().getAddressEn().getLng(),
                        result1);
                Float distance1 = result1[0];

                float[] result2 = new float[3];
                Location.distanceBetween(
                        mLastLocation.getLatitude(),
                        mLastLocation.getLongitude(),
                        doctor2.getClinicInformation().getAddressEn().getLat(),
                        doctor2.getClinicInformation().getAddressEn().getLng(),
                        result2);
                Float distance2 = result2[0];

                return distance1.compareTo(distance2);
            }
        });
    }

    private void addDoctorToList(Doctor doctor) {
        if (doctor != null && (!TextUtils.isEmpty(doctor.getAccountStatus())) &&
                doctor.getAccountStatus().equals(Doctor.ACTIVE_STATUS)) {
            if (specialization != null) {
                if (doctor.getSpecializationID().equals(specialization.getId())) {
                    doctor.setSpecialization(specialization);

                    fullList.add(doctor);
                    sortList(fullList);

                    if (city != null) {
                        if (doctor.getClinicInformation().getAddressAr().getCity().contains(city.getTitleAr()) ||
                                doctor.getClinicInformation().getAddressEn().getCity().contains(city.getTitleEn())) {

                            doctorList.add(doctor);
                            sortList(doctorList);
                        }
                    } else {
                        doctorList.add(doctor);
                        sortList(doctorList);
                    }
                }
            } else if (city != null) {
                fullList.add(doctor);
                sortList(fullList);

                if (doctor.getClinicInformation().getAddressAr().getCity().contains(city.getTitleAr()) ||
                        doctor.getClinicInformation().getAddressEn().getCity().contains(city.getTitleEn())) {

                    doctorList.add(doctor);
                    sortList(doctorList);
                }
            } else {
                fullList.add(doctor);
                sortList(fullList);

                doctorList.add(doctor);
                sortList(doctorList);
            }
        }
    }
}
