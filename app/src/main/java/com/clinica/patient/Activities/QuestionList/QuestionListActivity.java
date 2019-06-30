package com.clinica.patient.Activities.QuestionList;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Adapters.ConsultationAdapter;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuestionListActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RecyclerView consultationRecycler;
    @BindView(R.id.empty)
    AppCompatTextView empty;

    @Override
    protected int setLayoutView() {
        return R.layout.layout_recycler;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.bank_of_questions);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initActions() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.Intents.DOCTOR_ID)) {
            getDoctorQuestions(intent.getStringExtra(Constants.Intents.DOCTOR_ID));
        } else if (intent != null && intent.hasExtra(Constants.Intents.QUESTIONS_USER)) {
            getUserQuestions(intent.getStringExtra(Constants.Intents.QUESTIONS_USER));
        } else {
            getAllQuestion();
        }
    }

    private void getAllQuestion() {
        showLoading();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DataBase.Consultation_NODE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Consultation> consultationList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Consultation consultation = snapshot.getValue(Consultation.class);
                    if (consultation != null && !TextUtils.isEmpty(consultation.getAnswerPublisherID())) {
                        consultation.setId(snapshot.getKey());
                        consultationList.add(consultation);
                    }
                }

                if (consultationList.size() > 0) {
                    initConsultations(consultationList);
                }
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoading();
                showError(R.string.data_not_found);
            }
        });
    }

    private void getUserQuestions(final String userID) {
        showLoading();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DataBase.Consultation_NODE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Consultation> consultationList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Consultation consultation = snapshot.getValue(Consultation.class);
                    if (consultation != null && !TextUtils.isEmpty(consultation.getQuestionPublisherID())
                            && consultation.getQuestionPublisherID().equals(userID)) {
                        consultation.setId(snapshot.getKey());
                        consultationList.add(consultation);
                    }
                }

                if (consultationList.size() > 0) {
                    initConsultations(consultationList);
                }
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hideLoading();
                showError(R.string.data_not_found);
            }
        });
    }

    private void getDoctorQuestions(final String doctorID) {
        showLoading();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DataBase.Consultation_NODE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Consultation> consultationList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Consultation consultation = snapshot.getValue(Consultation.class);
                    if (consultation != null && !TextUtils.isEmpty(consultation.getAnswerPublisherID())
                            && consultation.getAnswerPublisherID().equals(doctorID)) {
                        consultation.setId(snapshot.getKey());
                        consultationList.add(consultation);
                    }
                }
                if (consultationList.size() > 0) {
                    initConsultations(consultationList);
                }
                hideLoading();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showError(R.string.data_not_found);
                hideLoading();
            }
        });
    }

    private void initConsultations(List<Consultation> consultationList) {
        empty.setVisibility(View.GONE);
        consultationRecycler.setVisibility(View.VISIBLE);
        ConsultationAdapter consultationAdapter = new ConsultationAdapter(this, Constants.LIST_CONSULTATIONS_VIEW);
        consultationAdapter.appendData(consultationList);
        consultationRecycler.setHasFixedSize(false);
        consultationRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        consultationRecycler.setNestedScrollingEnabled(false);
        consultationRecycler.setItemAnimator(new DefaultItemAnimator());
        consultationRecycler.setAdapter(consultationAdapter);
    }

    private void showError(int message) {
        consultationRecycler.setVisibility(View.GONE);
        empty.setText(message);
        empty.setVisibility(View.VISIBLE);
    }
}
