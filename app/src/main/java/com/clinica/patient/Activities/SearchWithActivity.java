package com.clinica.patient.Activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.clinica.patient.Activities.Selector.SelectorActivity;
import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Doctor.DoctorsList.DoctorListActivity;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchWithActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final int SELECT_SPECIALIZATION_RC = 85;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_search_with;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initActions() {
    }

    @OnClick(R.id.search_by_specialization)
    void searchBySpecialization() {
        Intent intent = new Intent(this, SelectorActivity.class);
        intent.putExtra(Constants.Intents.SELECTION_KEY, Constants.Intents.SELECT_SPECIALIZATION);
        startActivityForResult(intent, SELECT_SPECIALIZATION_RC);
    }

    @OnClick(R.id.search_by_name)
    void searchByName() {
        Intent doctorIntent = new Intent(this, DoctorListActivity.class);
        startActivity(doctorIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_SPECIALIZATION_RC && resultCode == RESULT_OK && data != null) {
            Intent doctorIntent = new Intent(this, DoctorListActivity.class);
            doctorIntent.putExtra(Constants.Intents.SPECIALIZATION_DATA, data.getParcelableExtra(Constants.Intents.SPECIALIZATION_DATA));
            startActivity(doctorIntent);
        }
    }
}
