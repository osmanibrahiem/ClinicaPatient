package com.clinica.patient.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clinica.patient.Models.Doctor.Appointment;
import com.clinica.patient.Models.Doctor.DayAppointments;
import com.clinica.patient.R;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppointmentsChildAdapter extends RecyclerView.Adapter<AppointmentsChildAdapter.AppointmentsChildViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private DatabaseReference mReference;
    private List<Appointment> appointmentList;
    private CallBack callBack;
    private DayAppointments dayAppointments;

    public AppointmentsChildAdapter(Context context, DayAppointments dayAppointments, List<Appointment> appointmentList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mReference = FirebaseDatabase.getInstance().getReference();
        this.dayAppointments = dayAppointments;
        this.appointmentList = new ArrayList<>();
        addtoList(appointmentList);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private void addtoList(List<Appointment> appointmentList) {
        for (Appointment appointment : appointmentList) {

            Calendar bookingDate = GregorianCalendar.getInstance();
            bookingDate.setTimeInMillis(dayAppointments.getTitle());
            Calendar bookingTime = GregorianCalendar.getInstance();
            bookingTime.setTimeInMillis(appointment.getTime());

            final Calendar bookingDateTime = GregorianCalendar.getInstance();
            bookingDateTime.set(Calendar.YEAR, bookingDate.get(Calendar.YEAR));
            bookingDateTime.set(Calendar.MONTH, bookingDate.get(Calendar.MONTH));
            bookingDateTime.set(Calendar.DATE, bookingDate.get(Calendar.DATE));
            bookingDateTime.set(Calendar.HOUR, bookingTime.get(Calendar.HOUR));
            bookingDateTime.set(Calendar.HOUR_OF_DAY, bookingTime.get(Calendar.HOUR_OF_DAY));
            bookingDateTime.set(Calendar.MINUTE, bookingTime.get(Calendar.MINUTE));
            bookingDateTime.set(Calendar.SECOND, bookingTime.get(Calendar.SECOND));

            Date date = bookingDateTime.getTime();
            if ((!date.before(new Date())) && (TextUtils.isEmpty(appointment.getUserId()))) {
                this.appointmentList.add(appointment);
            }
        }
        notifyItemRangeInserted(0, this.appointmentList.size());
        sortList();
    }

    private void sortList() {
        Collections.sort(appointmentList, new Comparator<Appointment>() {
            @Override
            public int compare(Appointment appointment1, Appointment appointment2) {
                return Long.compare(appointment1.getTime(), appointment2.getTime());
            }
        });
        notifyItemRangeChanged(0, appointmentList.size());
    }

    @NonNull
    @Override
    public AppointmentsChildViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_appointments_child_item, viewGroup, false);
        return new AppointmentsChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AppointmentsChildViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    class AppointmentsChildViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.appointment)
        AppCompatButton appointmentBtn;

        AppointmentsChildViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(int position) {
            final Appointment appointment = appointmentList.get(position);

            Locale locale = new Locale(UserData.getLocalizationString(context));
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", locale);
            appointmentBtn.setText(sdf.format(new Date(appointment.getTime())));

            appointmentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null)
                        callBack.onAppointmentClicked(appointment);
                }
            });
        }

    }

    public interface CallBack {

        void onAppointmentClicked(Appointment appointment);
    }
}
