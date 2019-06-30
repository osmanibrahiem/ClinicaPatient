package com.clinica.patient.Activities.Doctor.Booking;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.Booking;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.WorkingDayHours;
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class BookingPresenter extends BasePresenter {

    private BookingView view;
    private BookingActivity activity;
    private long appointmentDate;
    private DatabaseReference mReference;
    private Doctor thisDoctor;
    private Booking thisBooking;


    BookingPresenter(BookingView view, BookingActivity activity) {
        super(view, activity);
        this.view = view;
        this.activity = activity;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    void newBooking(long appointmentDate, String doctorID) {
        this.appointmentDate = appointmentDate;
        if (isNetworkAvailable()) {
            view.showLoading();
            getDoctor(doctorID);
        } else {
            view.showMessage(R.string.message_no_internet);
        }
    }

    private void getDoctor(String doctorID) {
        mReference.child(Constants.DataBase.Doctors_NODE).child(doctorID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        thisDoctor = dataSnapshot.getValue(Doctor.class);
                        if (thisDoctor != null) {
                            view.initDoctor(thisDoctor);
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

    }

    String getExaminationDuration() {
        Calendar bookingDateTime = GregorianCalendar.getInstance();
        bookingDateTime.setTimeInMillis(appointmentDate);
        WorkingDayHours dayHours = getWorkingDay(bookingDateTime.get(Calendar.DAY_OF_WEEK));
        int duration = 0;
        if (dayHours != null) {
            duration = dayHours.getDuration();
        }
        return activity.getString(R.string.minute, duration);
    }

    private WorkingDayHours getWorkingDay(int day) {
        for (WorkingDayHours dayHours : thisDoctor.getClinicInformation().getWorkingHours()) {
            if (dayHours.getDayOfWeek() == day)
                return dayHours;
        }
        return null;
    }

    void getBookingDetails(String bookingID) {
        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Bookings_NODE).child(bookingID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            thisBooking = dataSnapshot.getValue(Booking.class);
                            if (thisBooking != null) {
                                thisBooking.setId(dataSnapshot.getKey());
                                String doctorID = thisBooking.getDoctorId();
                                getDoctor(doctorID);
                                view.initBooking(thisBooking);
                            } else {
                                view.showMessage(R.string.data_not_found);
                                view.hideLoading();
                            }
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

    void confirmBooking(long appointmentDate, final String doctorID) {
        this.appointmentDate = appointmentDate;
        if (isNetworkAvailable()) {
            final Booking booking = new Booking();
            booking.setDate(appointmentDate);
            booking.setDoctorId(doctorID);
            booking.setMessage(activity.notesEt.getText().toString().trim());
            booking.setUserId(FirebaseAuth.getInstance().getUid());
            booking.setStatus(Booking.REQUESTED_STATUS);

            final String bookingID = mReference.child(Constants.DataBase.Bookings_NODE).push().getKey();
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
                            view.toastError(R.string.booking_saved_success);
                            sendNotificationToDoctor(doctorID, NotificationModel.USER_REQUSETED_BOOKING,
                                    Constants.DataBase.Bookings_NODE, bookingID);
                            view.viewBookingDetails(bookingID);
                        }
                    });
        } else {
            view.toastError(R.string.message_no_internet);
        }
    }

    void cancelRequestedBooking(final String bookingID) {
        thisBooking.setId(null);
        thisBooking.setDoctor(null);
        thisBooking.setPatient(null);
        thisBooking.setStatus(Booking.CANCELED_STATUS);

        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Bookings_NODE).child(bookingID).setValue(thisBooking)
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
                            getBookingDetails(bookingID);
                        }
                    });
        } else {
            view.toastError(R.string.message_no_internet);
        }
    }

    void cancelPendingBooking(final String bookingID) {
        thisBooking.setId(null);
        thisBooking.setDoctor(null);
        thisBooking.setPatient(null);
        thisBooking.setStatus(Booking.CANCELED_STATUS);

        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Bookings_NODE).child(bookingID).setValue(thisBooking)
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
                            sendNotificationToDoctor(thisBooking.getDoctorId(), NotificationModel.USER_CANCELED_BOOKING,
                                    Constants.DataBase.Bookings_NODE, bookingID);
                            clearAppointment(thisBooking.getDate(), bookingID);
                        }
                    });
        } else {
            view.toastError(R.string.message_no_internet);
        }
    }

    private void clearAppointment(long date, String bookingID) {
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

        for (int i = 0; i < thisDoctor.getDayAppointments().size(); i++) {
            if (DateUtils.isSameDay(new Date(thisDoctor.getDayAppointments().get(i).getTitle()),
                    new Date(bookingDate.getTimeInMillis()))) {
                for (int j = 0; j < thisDoctor.getDayAppointments().get(i).getAppointments().size(); j++) {
                    if (DateUtils.isSameTime(new Date(thisDoctor.getDayAppointments().get(i).getAppointments().get(j).getTime()),
                            new Date(bookingTime.getTimeInMillis()))) {
                        thisDoctor.getDayAppointments().get(i).getAppointments().get(j).setStatus(Constants.ACTIVE_STATUS);
                        thisDoctor.getDayAppointments().get(i).getAppointments().get(j).setUserId(null);
                        updateDoctor(date, bookingID);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void updateDoctor(final long day, final String bookingID) {
        thisDoctor.setUid(null);
        mReference.child(Constants.DataBase.Doctors_NODE).child(thisBooking.getDoctorId()).setValue(thisDoctor)
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
                        getBookingDetails(bookingID);
                    }
                });
    }

}
