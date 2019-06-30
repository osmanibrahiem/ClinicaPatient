package com.clinica.patient.Activities.BookingList;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.Booking;

import java.util.List;

interface AppointmentsView extends BaseView {

    void initBookings(List<Booking> bookings);

    void showMessage(int message);

    void toastError(int message);
}
