package com.clinica.patient.Activities.Doctor.DoctorProfile;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

class DoctorProfilePresenter extends BasePresenter {

    private DoctorProfileView view;
    private DoctorProfileActivity activity;
    private DatabaseReference mReference;

    DoctorProfilePresenter(DoctorProfileView view, DoctorProfileActivity activity) {
        super(view, activity);
        this.view = view;
        this.activity = activity;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    void checkDoctor(String id) {
        Log.i("DoctorProfile", "checkDoctor: ");
        view.showLoading();
        final DatabaseReference doctorReference = mReference.child(Constants.DataBase.Doctors_NODE).child(id);
        doctorReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("DoctorProfile", "checkDoctor: onDataChange: " + dataSnapshot.toString());
                if (dataSnapshot.exists()) {
                    final Doctor doctor = dataSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        Log.i("DoctorProfile", "checkDoctor: onDataChange: doctor != null: " + doctor.toString());
                        doctor.setUid(dataSnapshot.getKey());
                        mReference.child(Constants.DataBase.Specialization_NODE).child(doctor.getSpecializationID())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.i("DoctorProfile", "checkDoctor: onDataChange: onDataChange: " + dataSnapshot.toString());
                                        view.hideLoading();
                                        Specialization specialization = dataSnapshot.getValue(Specialization.class);
                                        if (specialization != null) {
                                            specialization.setId(dataSnapshot.getKey());
                                        }
                                        doctor.setSpecialization(specialization);

                                        view.initViews(doctor);

                                        checkQuestions(doctor.getUid());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.i("DoctorProfile", "checkDoctor: onDataChange: onCancelled: " + databaseError.toException().toString());
                                        view.hideLoading();
                                        view.showErrorMessage(R.string.message_doctor_not_found);
                                    }
                                });
                    } else {
                        Log.i("DoctorProfile", "checkDoctor: onDataChange: doctor == null: ");
                        view.hideLoading();
                        view.showErrorMessage(R.string.message_doctor_not_found);
                    }
                } else {
                    Log.i("DoctorProfile", "checkDoctor: onDataChange: dataSnapshot is not exists: ");
                    view.hideLoading();
                    view.showErrorMessage(R.string.message_doctor_not_found);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("DoctorProfile", "checkDoctor: onCancelled: " + databaseError.toException().toString());
                view.hideLoading();
                view.showErrorMessage(R.string.message_doctor_not_found);
            }
        });
    }

    private void checkQuestions(final String doctorUid) {
        final DatabaseReference reference = mReference.child(Constants.DataBase.Consultation_NODE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Consultation> consultationList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Consultation consultation = snapshot.getValue(Consultation.class);
                    if (consultation != null && !TextUtils.isEmpty(consultation.getAnswerPublisherID())
                            && consultation.getAnswerPublisherID().equals(doctorUid)) {
                        consultation.setId(snapshot.getKey());
                        consultationList.add(consultation);
                    }

                    if (consultationList.size() > 0) {
                        view.initConsultations(consultationList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
