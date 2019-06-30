package com.clinica.patient.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clinica.patient.Models.Booking;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.DateUtils;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingsViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<Booking> bookingList;
    private DatabaseReference mReference;
    private BookingsCallBack callBack;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.bookingList = bookingList;
        mReference = FirebaseDatabase.getInstance().getReference();

        Collections.sort(this.bookingList, new Comparator<Booking>() {
            @Override
            public int compare(Booking o1, Booking o2) {
                return Long.compare(o1.getDate(), o2.getDate());
            }
        });
    }

    public void setCallBack(BookingsCallBack callBack) {
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public BookingsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_bookings_item, viewGroup, false);
        return new BookingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BookingsViewHolder bookingsViewHolder, int position) {
        final Booking booking = bookingList.get(position);
        if (!TextUtils.isEmpty(booking.getDoctorId())) {
            mReference.child(Constants.DataBase.Doctors_NODE).child(booking.getDoctorId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Doctor doctor = dataSnapshot.getValue(Doctor.class);
                            if (doctor != null) {
                                doctor.setUid(dataSnapshot.getKey());
                                booking.setDoctor(doctor);
                                bookingsViewHolder.bind(booking);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else bookingsViewHolder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class BookingsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_patient)
        CircularImageView imgPatient;
        @BindView(R.id.patient_name)
        TextView patientName;
        @BindView(R.id.professional_title)
        TextView professionalTitle;
        @BindView(R.id.examination_date)
        TextView examinationDate;
        @BindView(R.id.doctor_address)
        TextView doctorAddress;
        @BindView(R.id.cancel_btn)
        AppCompatButton cancelBtn;

        BookingsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Booking booking) {

            Doctor doctor = booking.getDoctor();
            if (doctor != null) {
                if (!TextUtils.isEmpty(doctor.getProfilePhotoUrl())) {
                    Picasso.get()
                            .load(doctor.getProfilePhotoUrl())
                            .placeholder(R.drawable.profile_picture_blank_square)
                            .error(R.drawable.error_placeholder)
                            .into(imgPatient);
                }

                if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                    patientName.setText(doctor.getBasicInformationAr().getDisplayName());
                    professionalTitle.setText(doctor.getBasicInformationAr().getProfessionalTitle());
                    doctorAddress.setText(doctor.getClinicInformation().getAddressAr().getFullAddress(context, Localization.ARABIC_VALUE));
                } else {
                    patientName.setText(doctor.getBasicInformationEN().getDisplayName());
                    professionalTitle.setText(doctor.getBasicInformationEN().getProfessionalTitle());
                    doctorAddress.setText(doctor.getClinicInformation().getAddressEn().getFullAddress(context, Localization.ENGLISH_VALUE));
                }
            }

            DateFormat dfEN = new SimpleDateFormat("EEEE, d MMMM 'at' hh:mm aa", new Locale("en"));
            DateFormat dfAR = new SimpleDateFormat("EEEE d MMMM 'في تمام الساعة' hh:mm aa", new Locale("ar"));
            DateFormat dfDayEN = new SimpleDateFormat(", d MMMM 'at' hh:mm aa", new Locale("en"));
            DateFormat dfDayAR = new SimpleDateFormat(" d MMMM 'في تمام الساعة' hh:mm aa", new Locale("ar"));
            if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                if (DateUtils.isToday(new Date(booking.getDate()))) {
                    examinationDate.setText(String.format("%1$s%2$s", context.getString(R.string.today), dfDayAR.format(new Date(booking.getDate()))));
                } else if (DateUtils.isTomorrow(new Date(booking.getDate()))) {
                    examinationDate.setText(String.format("%1$s%2$s", context.getString(R.string.tomorrow), dfDayAR.format(new Date(booking.getDate()))));
                } else {
                    examinationDate.setText(dfAR.format(new Date(booking.getDate())));
                }
            } else {
                if (DateUtils.isToday(new Date(booking.getDate()))) {
                    examinationDate.setText(String.format("%1$s%2$s", context.getString(R.string.today), dfDayEN.format(new Date(booking.getDate()))));
                } else if (DateUtils.isTomorrow(new Date(booking.getDate()))) {
                    examinationDate.setText(String.format("%1$s%2$s", context.getString(R.string.tomorrow), dfDayEN.format(new Date(booking.getDate()))));
                } else {
                    examinationDate.setText(dfEN.format(new Date(booking.getDate())));
                }
            }

            if (!TextUtils.isEmpty(booking.getStatus())) {
                switch (booking.getStatus()) {
                    case Booking.REQUESTED_STATUS:
                        cancelBtn.setText(R.string.cancel);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (callBack != null)
                                    callBack.onCancelRequestedBooking(booking);
                            }
                        });
                        break;
                    case Booking.PENDING_STATUS:
                        cancelBtn.setText(R.string.cancel);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (callBack != null)
                                    callBack.onCancelPendingBooking(booking);
                            }
                        });
                        break;
                    case Booking.CANCELED_STATUS:
                    case Booking.REFUSED_STATUS:
                        cancelBtn.setText(R.string.examination_canceled);
                        cancelBtn.setEnabled(false);
                        Log.i("mBooking", "bind: " + booking.getStatus());
                        break;
                    case Booking.SUCCESS_STATUS:
                        cancelBtn.setText(R.string.examination_done);
                        cancelBtn.setEnabled(false);
                        Log.i("mBooking", "bind: " + booking.getStatus());
                        break;
                }

            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null)
                        callBack.onBookingClicked(booking);
                }
            });

        }
    }

    private boolean checkAppointment(Booking booking) {
        Calendar bookingDate = GregorianCalendar.getInstance();
        Calendar bookingTime = GregorianCalendar.getInstance();
        Calendar bookingDateTime = GregorianCalendar.getInstance();
        bookingDateTime.setTimeInMillis(booking.getDate());

        bookingDate.set(Calendar.YEAR, bookingDateTime.get(Calendar.YEAR));
        bookingDate.set(Calendar.MONTH, bookingDateTime.get(Calendar.MONTH));
        bookingDate.set(Calendar.DATE, bookingDateTime.get(Calendar.DATE));
        bookingTime.set(Calendar.HOUR, bookingDateTime.get(Calendar.HOUR));
        bookingTime.set(Calendar.HOUR_OF_DAY, bookingDateTime.get(Calendar.HOUR_OF_DAY));
        bookingTime.set(Calendar.MINUTE, bookingDateTime.get(Calendar.MINUTE));
        bookingTime.set(Calendar.SECOND, bookingDateTime.get(Calendar.SECOND));

        for (int i = 0; i < booking.getDoctor().getDayAppointments().size(); i++) {
            if (DateUtils.isSameDay(new Date(booking.getDoctor().getDayAppointments().get(i).getTitle()),
                    new Date(bookingDate.getTimeInMillis()))) {
                for (int j = 0; j < booking.getDoctor().getDayAppointments().get(i).getAppointments().size(); j++) {
                    if (DateUtils.isSameTime(new Date(booking.getDoctor().getDayAppointments().get(i).getAppointments().get(j).getTime()),
                            new Date(bookingTime.getTimeInMillis()))) {
                        return TextUtils.isEmpty(booking.getDoctor().getDayAppointments().get(i).getAppointments().get(j).getUserId());
                    }
                }
                break;
            }
        }
        return false;
    }

    public interface BookingsCallBack {

        void onBookingClicked(Booking booking);

        void onCancelRequestedBooking(Booking booking);

        void onCancelPendingBooking(Booking booking);
    }
}
