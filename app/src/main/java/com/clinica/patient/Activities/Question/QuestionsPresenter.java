package com.clinica.patient.Activities.Question;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.User;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

class QuestionsPresenter extends BasePresenter {

    private QuestionActivity activity;
    private QuestionsView view;
    private DatabaseReference mReference;
    private Consultation thisConsultation;

    QuestionsPresenter(QuestionsView view, QuestionActivity activity) {
        super(view, activity);
        this.activity = activity;
        this.view = view;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    void getQuestion(String questionID) {
        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Consultation_NODE).child(questionID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            thisConsultation = dataSnapshot.getValue(Consultation.class);
                            if (thisConsultation != null) {
                                thisConsultation.setId(dataSnapshot.getKey());
                                view.initConsultation(thisConsultation);
                                if (!TextUtils.isEmpty(thisConsultation.getQuestionPublisherID())) {
                                    getUser(thisConsultation.getQuestionPublisherID());
                                }
                            } else {
                                view.showMessage(R.string.data_not_found);
                                view.hideLoading();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            view.showMessage(R.string.data_not_found);
                            view.hideLoading();
                        }
                    });
        } else {
            view.showMessage(R.string.message_no_internet);
        }
    }

    private void getUser(String userID) {
        mReference.child(Constants.DataBase.USERS_NODE).child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            user.setUid(dataSnapshot.getKey());
                            view.initUser(user);
                            if (!TextUtils.isEmpty(thisConsultation.getAnswerPublisherID())) {
                                getDoctor(thisConsultation.getAnswerPublisherID());
                            } else {
                                view.hideLoading();
                            }
                        } else {
                            view.showMessage(R.string.data_not_found);
                            view.hideLoading();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        view.showMessage(R.string.data_not_found);
                        view.hideLoading();
                    }
                });
    }

    private void getDoctor(String doctorID) {
        mReference.child(Constants.DataBase.Doctors_NODE).child(doctorID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Doctor doctor = dataSnapshot.getValue(Doctor.class);
                        if (doctor != null) {
                            doctor.setUid(dataSnapshot.getKey());
                            view.initDoctor(doctor);
                            view.hideLoading();
                        } else {
                            view.showMessage(R.string.data_not_found);
                            view.hideLoading();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        view.showMessage(R.string.data_not_found);
                        view.hideLoading();
                    }
                });
    }

}
