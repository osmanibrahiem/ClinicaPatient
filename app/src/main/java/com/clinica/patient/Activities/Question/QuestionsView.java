package com.clinica.patient.Activities.Question;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.User;

interface QuestionsView extends BaseView {

    void showMessage(int message);

    void initConsultation(Consultation consultation);

    void initUser(User user);

    void initDoctor(Doctor doctor);
}
