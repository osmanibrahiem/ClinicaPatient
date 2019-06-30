package com.clinica.patient.Activities.BookingList;

import android.support.annotation.NonNull;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.Booking;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.NotificationModel;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.DateUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

class AppointmentsPresenter extends BasePresenter {

    private AppointmentsFragment fragment;
    private AppointmentsView view;
    private DatabaseReference mReference;

    AppointmentsPresenter(AppointmentsView view, AppointmentsFragment fragment) {
        super(view, fragment);
        this.fragment = fragment;
        this.view = view;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    private String getID() {
        return FirebaseAuth.getInstance().getUid();
    }

    void getBookings() {
        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Bookings_NODE)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Booking> bookings = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Booking booking = snapshot.getValue(Booking.class);
                                if (booking != null && booking.getUserId().equals(getID())) {
                                    booking.setId(snapshot.getKey());
                                    bookings.add(booking);
                                }
                            }

                            if (bookings.size() > 0) {
                                view.initBookings(bookings);
                            } else {
                                view.showMessage(R.string.data_not_found);
                            }
                            view.hideLoading();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            view.showMessage(R.string.data_not_found);
                            view.hideLoading();
                        }
                    });
        } else {
            view.showMessage(R.string.message_no_internet);
        }
    }

    void cancelRequestedBooking(Booking booking) {
        String bookingID = booking.getId();
        booking.setId(null);
        booking.setDoctor(null);
        booking.setPatient(null);
        booking.setStatus(Booking.CANCELED_STATUS);

        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Bookings_NODE).child(bookingID).setValue(booking)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            view.toastError(R.string.error_happened_2);
                            view.hideLoading();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            view.hideLoading();
                            getBookings();
                        }
                    });
        } else {
            view.toastError(R.string.message_no_internet);
        }
    }

    void cancelPendingBooking(final Booking booking) {
        final String bookingID = booking.getId();
        final Doctor bookingDoctor = booking.getDoctor();
        booking.setId(null);
        booking.setDoctor(null);
        booking.setPatient(null);
        booking.setStatus(Booking.CANCELED_STATUS);

        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Bookings_NODE).child(bookingID).setValue(booking)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            view.toastError(R.string.error_happened_2);
                            view.hideLoading();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            view.hideLoading();
                            sendNotificationToDoctor(bookingDoctor.getUid(), NotificationModel.USER_CANCELED_BOOKING,
                                    Constants.DataBase.Bookings_NODE, bookingID);
                            clearAppointment(booking.getDate(), bookingDoctor);
                        }
                    });
        } else {
            view.toastError(R.string.message_no_internet);
        }
    }

    private void clearAppointment(long date, Doctor doctor) {
        Calendar bookingDate = GregorianCalendar.getInstance();
        Calendar bookingTime = GregorianCalendar.getInstance();
        Calendar bookingDateTime = GregorianCalendar.getInstance();
        bookingDateTime.setTimeInMillis(date);

        bookingDate.set(Calendar.YEAR, bookingDateTime.get(Calendar.YEAR));
        bookingDate.set(Calendar.MONTH, bookingDateTime.get(Calendar.MONTH));
        bookingDate.set(Calendar.DATE, bookingDateTime.get(Calendar.DATE));
        bookingTime.set(Calendar.HOUR, bookingDateTime.get(Calendar.HOUR));
        bookingTime.set(Calendar.HOUR_OF_DAY, bookingDateTime.get(Calendar.HOUR_OF_DAY));
        bookingTime.set(Calendar.MINUTE, bookingDateTime.get(Calendar.MINUTE));
        bookingTime.set(Calendar.SECOND, bookingDateTime.get(Calendar.SECOND));

        for (int i = 0; i < doctor.getDayAppointments().size(); i++) {
            if (DateUtils.isSameDay(new Date(doctor.getDayAppointments().get(i).getTitle()),
                    new Date(bookingDate.getTimeInMillis()))) {
                for (int j = 0; j < doctor.getDayAppointments().get(i).getAppointments().size(); j++) {
                    if (DateUtils.isSameTime(new Date(doctor.getDayAppointments().get(i).getAppointments().get(j).getTime()),
                            new Date(bookingTime.getTimeInMillis()))) {
                        doctor.getDayAppointments().get(i).getAppointments().get(j).setStatus(Constants.ACTIVE_STATUS);
                        doctor.getDayAppointments().get(i).getAppointments().get(j).setUserId(null);
                        updateDoctor(doctor);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void updateDoctor(Doctor doctor) {
        String doctorID = doctor.getUid();
        doctor.setUid(null);
        mReference.child(Constants.DataBase.Doctors_NODE).child(doctorID).setValue(doctor)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        view.showMessage(R.string.error_happened_2);
                        view.hideLoading();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        view.hideLoading();
                        getBookings();
                    }
                });
    }

}
