package com.clinica.patient.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clinica.patient.Models.Booking;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.NotificationModel;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.clinica.patient.Tools.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<NotificationModel> notificationList;
    private DatabaseReference reference;
    private CallBack callBack;

    public NotificationsAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.notificationList = notificationList;
        this.reference = FirebaseDatabase.getInstance().getReference();

        Collections.sort(notificationList, new Comparator<NotificationModel>() {
            @Override
            public int compare(NotificationModel o1, NotificationModel o2) {
                return Long.compare(o2.getDate(), o1.getDate());
            }
        });
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_notifications_item, viewGroup, false);
        return new NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationsViewHolder notificationsViewHolder, final int i) {
        final NotificationModel model = notificationList.get(i);
        String doctorID = model.getFrom();
        reference.child(Constants.DataBase.Doctors_NODE).child(doctorID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Doctor doctor = dataSnapshot.getValue(Doctor.class);

                        reference.child(model.getCatID()).child(model.getDestinationID())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        switch (model.getType()) {
                                            case NotificationModel.DOCTOR_ACCEPTED_BOOKING:
                                            case NotificationModel.DOCTOR_CANCELED_BOOKING:
                                            case NotificationModel.DOCTOR_REFUSED_BOOKING:
                                                Booking booking = dataSnapshot.getValue(Booking.class);
                                                notificationsViewHolder.bind(i, doctor, booking, null);
                                                break;
                                            case NotificationModel.DOCTOR_ANSWERED_QUESTION:
                                                Consultation consultation = dataSnapshot.getValue(Consultation.class);
                                                notificationsViewHolder.bind(i, doctor, null, consultation);
                                                break;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    class NotificationsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img)
        CircularImageView img;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.date)
        TextView date;

        NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position, Doctor doctor, final Booking booking, final Consultation consultation) {
            final NotificationModel notification = notificationList.get(position);
            String bookingDate = "", doctorName = "";
            if (booking != null) {
                DateFormat dfEN = new SimpleDateFormat("EEEE, dd MMMM 'at' hh:mm aa", new Locale("en"));
                DateFormat dfAR = new SimpleDateFormat("EEEE dd MMMM 'في تمام الساعة' hh:mm aa", new Locale("ar"));
                if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                    bookingDate = dfAR.format(new Date(booking.getDate()));
                } else {
                    bookingDate = dfEN.format(new Date(booking.getDate()));
                }
            }
            if (doctor != null) {
                if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                    doctorName = doctor.getBasicInformationAr().getDisplayName();
                } else {
                    doctorName = doctor.getBasicInformationEN().getDisplayName();
                }
            }
            switch (notification.getType()) {
                case NotificationModel.DOCTOR_ACCEPTED_BOOKING:
                    title.setText(context.getString(R.string.booking_accepted_by_doctor, doctorName, bookingDate));
                    break;
                case NotificationModel.DOCTOR_CANCELED_BOOKING:
                    title.setText(context.getString(R.string.booking_canceled_by_doctor, doctorName));
                    break;
                case NotificationModel.DOCTOR_REFUSED_BOOKING:
                    title.setText(context.getString(R.string.booking_refused_by_doctor, doctorName));
                    break;
                case NotificationModel.DOCTOR_ANSWERED_QUESTION:
                    title.setText(context.getString(R.string.doctor_answered_question, doctorName));
                    break;
            }

            img.setImageResource(notification.getImage());
            date.setText(Utils.parceDateToStringAgo(notification.getDate(), context));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (notification.getType()) {
                        case NotificationModel.DOCTOR_ACCEPTED_BOOKING:
                        case NotificationModel.DOCTOR_CANCELED_BOOKING:
                        case NotificationModel.DOCTOR_REFUSED_BOOKING:
                            if (callBack != null && booking != null)
                                callBack.onBookingNotification(notification.getDestinationID());
                            break;
                        case NotificationModel.DOCTOR_ANSWERED_QUESTION:
                            if (callBack != null && consultation != null)
                                callBack.onQuestionNotification(notification.getDestinationID());
                            break;
                    }
                }
            });
        }
    }

    public interface CallBack {

        void onBookingNotification(String bookingID);

        void onQuestionNotification(String questionID);
    }
}
