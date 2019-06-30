package com.clinica.patient.Activities.Doctor.DoctorsList;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Activities.Doctor.DoctorCallBack;
import com.clinica.patient.Activities.Doctor.DoctorQuery;
import com.clinica.patient.Models.City;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.List;

class DoctorListPresenter extends BasePresenter {

    private DoctorListView view;
    private DoctorListActivity activity;
    private DatabaseReference mReference;

    DoctorListPresenter(DoctorListView view, DoctorListActivity activity) {
        super(view, activity);
        this.view = view;
        this.activity = activity;

        mReference = FirebaseDatabase.getInstance().getReference();
    }

    void getAllDoctors() {
        Query query = mReference.child(Constants.DataBase.Doctors_NODE);
        new DoctorQuery(activity, query).withCallBack(new DoctorCallBack() {
            @Override
            public void onGetData(List<Doctor> doctors) {
                if (doctors != null && doctors.size() != 0) {
                    view.onGetDoctors(doctors);
                } else view.showEmptyMessage(activity.getString(R.string.empty_list));
            }

            @Override
            public void onError(String message) {
                view.showEmptyMessage(message);
            }
        });
    }

    void getAllDoctors(Specialization specialization) {
        Query query = mReference.child(Constants.DataBase.Doctors_NODE)
                .orderByChild("specializationID").equalTo(specialization.getId());
        new DoctorQuery(activity, query).withSpecialization(specialization)
                .withCallBack(new DoctorCallBack() {
                    @Override
                    public void onGetData(List<Doctor> doctors) {
                        if (doctors != null && doctors.size() != 0) {
                            view.onGetDoctors(doctors);
                        } else view.showEmptyMessage(activity.getString(R.string.empty_list));
                    }

                    @Override
                    public void onError(String message) {
                        view.showEmptyMessage(message);
                    }
                });
    }

    void getAllDoctors(City city) {
        Query query = mReference.child(Constants.DataBase.Doctors_NODE);
        new DoctorQuery(activity, query).withLocation(city)
                .withCallBack(new DoctorCallBack() {
                    @Override
                    public void onGetData(List<Doctor> doctors) {
                        if (doctors != null && doctors.size() != 0) {
                            view.onGetDoctors(doctors);
                        } else view.showEmptyMessage(activity.getString(R.string.empty_list));
                    }

                    @Override
                    public void onError(String message) {
                        view.showEmptyMessage(message);
                    }
                });
    }

    void getAllDoctors(Specialization specialization, City city) {
        Query query = mReference.child(Constants.DataBase.Doctors_NODE)
                .orderByChild("specializationID").equalTo(specialization.getId());
        new DoctorQuery(activity, query)
                .withSpecialization(specialization)
                .withLocation(city)
                .withCallBack(new DoctorCallBack() {
                    @Override
                    public void onGetData(List<Doctor> doctors) {
                        if (doctors != null && doctors.size() != 0) {
                            view.onGetDoctors(doctors);
                        } else view.showEmptyMessage(activity.getString(R.string.empty_list));
                    }

                    @Override
                    public void onError(String message) {
                        view.showEmptyMessage(message);
                    }
                });
    }
}
