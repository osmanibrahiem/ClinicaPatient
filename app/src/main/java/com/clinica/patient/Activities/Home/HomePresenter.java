package com.clinica.patient.Activities.Home;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.Ads;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.News;
import com.clinica.patient.Tools.Constants;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class HomePresenter extends BasePresenter {

    private HomeView view;
    private HomeFragment fragment;
    private DatabaseReference mReference;

    private static final String TAG = "mDataBase";

    HomePresenter(HomeView view, HomeFragment fragment) {
        super(view, fragment);
        this.view = view;
        this.fragment = fragment;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    void checkAds() {
        mReference.child(Constants.DataBase.ADS_NODE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mReference.child(Constants.DataBase.ADS_NODE).removeEventListener(this);
                    view.initAdsRecycler(dataSnapshot.getChildrenCount());
                    getAllAds();
                } else {
                    view.hideAdsPager();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.hideAdsPager();
            }
        });
    }

    private void getAllAds() {
        mReference.child(Constants.DataBase.ADS_NODE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Ads ads = dataSnapshot.getValue(Ads.class);
                ads.setId(dataSnapshot.getKey());
                if (ads.getStatus().equals(Constants.ACTIVE_STATUS)) {
                    long startDate = ads.getPublicationDate();
                    long endDate = ads.getExpiryDate();
                    long timeNow = new Date().getTime();

                    if (timeNow >= startDate && timeNow <= endDate) {
                        view.addAds(ads);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Ads ads = dataSnapshot.getValue(Ads.class);
                ads.setId(dataSnapshot.getKey());
                view.updateAds(ads);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Ads ads = dataSnapshot.getValue(Ads.class);
                ads.setId(dataSnapshot.getKey());
                view.deleteAds(ads);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.hideAdsPager();
            }
        });
    }

    void getDoctorsCount() {
        mReference.child(Constants.DataBase.Doctors_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long count = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Doctor doctor = snapshot.getValue(Doctor.class);
                        if (doctor != null && doctor.getAccountStatus().equals(Doctor.ACTIVE_STATUS))
                            count++;
                    }
                    view.setDoctorsCount(count);
                } else {
                    view.setDoctorsCount(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.setDoctorsCount(0);
            }
        });
    }

    void getDiseasesCount() {
        mReference.child(Constants.DataBase.Diseases_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    view.setDiseasesCount(dataSnapshot.getChildrenCount());
                } else {
                    view.setDiseasesCount(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void getSymptomsCount() {
        mReference.child(Constants.DataBase.Symptoms_NODE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    view.setSymptomsCount(dataSnapshot.getChildrenCount());
                } else {
                    view.setSymptomsCount(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void checkNews() {
        mReference.child(Constants.DataBase.News_NODE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mReference.child(Constants.DataBase.News_NODE).removeEventListener(this);
                    view.initNewsSection();
                    getAllNews();
                } else {
                    view.hideNewsSection();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.hideNewsSection();
            }
        });
    }

    private void getAllNews() {
        mReference.child(Constants.DataBase.News_NODE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                News news = dataSnapshot.getValue(News.class);
                news.setId(dataSnapshot.getKey());
                if (news.getStatus().equals(Constants.ACTIVE_STATUS)) {
                    view.addNews(news);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                News news = dataSnapshot.getValue(News.class);
                news.setId(dataSnapshot.getKey());
                view.updateNews(news);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                News news = dataSnapshot.getValue(News.class);
                news.setId(dataSnapshot.getKey());
                view.deleteNews(news);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void checkConsultations() {
        mReference.child(Constants.DataBase.Consultation_NODE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mReference.child(Constants.DataBase.Consultation_NODE).removeEventListener(this);
                    view.initConsultationsSection();
                    getAllConsultations();
                } else {
                    view.hideConsultationsList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllConsultations() {
        mReference.child(Constants.DataBase.Consultation_NODE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Consultation consultation = dataSnapshot.getValue(Consultation.class);
                consultation.setId(dataSnapshot.getKey());
                view.addConsultation(consultation);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Consultation consultation = dataSnapshot.getValue(Consultation.class);
                consultation.setId(dataSnapshot.getKey());
                view.updateConsultation(consultation);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Consultation consultation = dataSnapshot.getValue(Consultation.class);
                consultation.setId(dataSnapshot.getKey());
                view.deleteConsultations(consultation);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
