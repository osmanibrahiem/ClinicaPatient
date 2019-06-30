package com.clinica.patient.Activities.Doctor;

import com.clinica.patient.Models.Doctor.Doctor;

import java.util.List;

public interface DoctorCallBack {

    void onGetData(List<Doctor> doctors);

    void onError(String message);
}
