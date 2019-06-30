package com.clinica.patient.Activities.Doctor.DoctorsList;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.Doctor.Doctor;

import java.util.List;

public interface DoctorListView extends BaseView {

    void setSearchError(String message);

    void showEmptyMessage(String message);

    void onGetDoctors(List<Doctor> doctors);

}
