package com.clinica.patient.Activities.Doctor.Booking;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.Booking;
import com.clinica.patient.Models.Doctor.Doctor;

interface BookingView extends BaseView {

    void showMessage(int message);

    void initDoctor(Doctor doctor);

    void initBooking(Booking booking);

    void toastError(int message);

    void viewBookingDetails(String bookingID);
}
