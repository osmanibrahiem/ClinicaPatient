package com.clinica.patient.Activities.Doctor.DoctorsList;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Selector.SelectorActivity;
import com.clinica.patient.Adapters.DoctorsAdapter;
import com.clinica.patient.Models.City;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DoctorListActivity extends BaseActivity implements DoctorListView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title)
    TextView titleTV;
    @BindView(R.id.city)
    TextView cityTV;
    @BindView(R.id.search_btn)
    AppCompatImageView searchBtn;
    @BindView(R.id.search_input_layout)
    TextInputLayout inputSearch;
    @BindView(R.id.search_input)
    AppCompatEditText searchET;
    @BindView(R.id.close_btn)
    AppCompatImageView closeBtn;
    @BindView(R.id.recycler)
    RecyclerView recyclerView;
    @BindView(R.id.empty)
    AppCompatTextView emptyText;

    private DoctorListPresenter presenter;
    private DoctorsAdapter doctorsAdapter;
    private List<Doctor> doctorList;
    private int selectionCityRequestCode = 808;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_doctors_list;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        presenter = new DoctorListPresenter(this, this);
        if (getIntent().hasExtra(Constants.Intents.LOCATION_DATA)) {
            City city = getIntent().getParcelableExtra(Constants.Intents.LOCATION_DATA);
            if (UserData.getLocalization(this) == Localization.ARABIC_VALUE) {
                cityTV.setText(city.getTitleAr());
            } else {
                cityTV.setText(city.getTitleEn());
            }
        } else {
            cityTV.setText(R.string.all_governorates);
        }
        toolbar.setTitle("");
        titleTV.setText(R.string.doctor_list_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchET.setHint(getString(R.string.doctor_list_search_hint));
    }

    @OnClick(R.id.city)
    void onCityClicked() {
        Intent intent = new Intent(new Intent(DoctorListActivity.this, SelectorActivity.class));
        intent.putExtra(Constants.Intents.SELECTION_KEY, Constants.Intents.SELECT_CITY);
        startActivityForResult(intent, selectionCityRequestCode);
    }

    @Override
    protected void initActions() {
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    inputSearch.setError(null);
                    inputSearch.setErrorEnabled(false);
                }
                doctorsAdapter.filterByName(doctorList, s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearchClicked();
                    return true;
                }
                return false;
            }
        });

        doctorList = new ArrayList<>();
        doctorsAdapter = new DoctorsAdapter(this);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(doctorsAdapter);

        if (getIntent().hasExtra(Constants.Intents.SPECIALIZATION_DATA) && getIntent().hasExtra(Constants.Intents.LOCATION_DATA)) {
            Specialization specialization = getIntent().getParcelableExtra(Constants.Intents.SPECIALIZATION_DATA);
            City city = getIntent().getParcelableExtra(Constants.Intents.LOCATION_DATA);
            presenter.getAllDoctors(specialization, city);
        } else if (getIntent().hasExtra(Constants.Intents.SPECIALIZATION_DATA)) {
            Specialization specialization = getIntent().getParcelableExtra(Constants.Intents.SPECIALIZATION_DATA);
            presenter.getAllDoctors(specialization);
        } else if (getIntent().hasExtra(Constants.Intents.LOCATION_DATA)) {
            City city = getIntent().getParcelableExtra(Constants.Intents.LOCATION_DATA);
            presenter.getAllDoctors(city);
        } else presenter.getAllDoctors();
    }

    @OnClick(R.id.search_btn)
    void onSearchClicked() {
        String searchTxt = searchET.getText().toString().trim();
        doctorsAdapter.filterByName(doctorList, searchTxt);
        presenter.hideKeypad();
        recyclerView.requestFocus();
    }

    @OnClick(R.id.close_btn)
    void onCloseClicked() {
        presenter.hideKeypad();
        searchET.setText("");
        recyclerView.requestFocus();
    }

    @Override
    public void setSearchError(String message) {
        inputSearch.setError(message);
    }

    @Override
    public void showEmptyMessage(String message) {
        emptyText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setText(message);
    }

    @Override
    public void onGetDoctors(List<Doctor> doctors) {
        doctorsAdapter.appendData(doctors);
        doctorList = doctors;
    }

 /*
    @Override
    public void onDoctorAdded(@NonNull Doctor doctor) {
        doctorsAdapter.addData(doctor);
        doctorList.add(doctor);
    }

    @Override
    public void onDoctorUpdated(@NonNull Doctor doctor) {
        doctorsAdapter.updateData(doctor);
        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getUid().equals(doctor.getUid())) {
                doctorList.set(i, doctor);
            }
        }
    }

    @Override
    public void onDoctorRemoved(@NonNull Doctor doctor) {
        doctorsAdapter.removeData(doctor);
        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getUid().equals(doctor.getUid())) {
                doctorList.remove(i);
            }
        }
    }
*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == selectionCityRequestCode && resultCode == RESULT_OK) {
            City city = data.getParcelableExtra(Constants.Intents.CITY_DATA);
            if (getIntent().hasExtra(Constants.Intents.SPECIALIZATION_DATA)) {
                Specialization specialization = getIntent().getParcelableExtra(Constants.Intents.SPECIALIZATION_DATA);
                Intent reloadIntent = new Intent(this, DoctorListActivity.class);
                reloadIntent.putExtra(Constants.Intents.SPECIALIZATION_DATA, specialization);
                reloadIntent.putExtra(Constants.Intents.LOCATION_DATA, city);
                startActivity(reloadIntent);
                finish();
            }
        }
    }
}
