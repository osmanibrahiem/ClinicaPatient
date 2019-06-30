package com.clinica.patient.Activities.Doctor.DoctorProfile;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;

import java.util.List;

public interface DoctorProfileView extends BaseView {

    void initViews(Doctor doctor);

    void showErrorMessage(int message);

    void initConsultations(List<Consultation> consultationList);
}
