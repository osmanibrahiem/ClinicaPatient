package com.clinica.patient.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clinica.patient.Models.Doctor.Appointment;
import com.clinica.patient.Models.Doctor.DayAppointments;
import com.clinica.patient.R;
import com.clinica.patient.Tools.DateUtils;
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

public class AppointmentsMainAdapter extends RecyclerView.Adapter<AppointmentsMainAdapter.AppointmentsMainViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private DatabaseReference mReference;
    private List<DayAppointments> dayAppointmentsList;
    private RecyclerView recyclerView;
    private CallBack callBack;

    public AppointmentsMainAdapter(Context context, List<DayAppointments> dayAppointmentsList, RecyclerView recyclerView) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mReference = FirebaseDatabase.getInstance().getReference();
        this.dayAppointmentsList = new ArrayList<>();
        this.recyclerView = recyclerView;
        addToList(dayAppointmentsList);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private void addToList(List<DayAppointments> dayAppointmentsList) {
        for (DayAppointments dayAppointments : dayAppointmentsList) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dayAppointments.getTitle());
            if ((!DateUtils.isBeforeDay(cal, Calendar.getInstance()))) {

                boolean isAvailable = false;
                for (Appointment appointment : dayAppointments.getAppointments()) {

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
                        isAvailable = true;
                    }
                }

                if (isAvailable)
                    this.dayAppointmentsList.add(dayAppointments);
            }
        }
        notifyItemRangeInserted(0, this.dayAppointmentsList.size());
        sortList();
    }

    private void sortList() {
        Collections.sort(dayAppointmentsList, new Comparator<DayAppointments>() {
            @Override
            public int compare(DayAppointments dayAppointments1, DayAppointments dayAppointments2) {
                return Long.compare(dayAppointments1.getTitle(), dayAppointments2.getTitle());
            }
        });
        notifyItemRangeChanged(0, dayAppointmentsList.size());
    }

    @NonNull
    @Override
    public AppointmentsMainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_appointments_main_item, viewGroup, false);
        return new AppointmentsMainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AppointmentsMainViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return dayAppointmentsList.size() >= 7 ? 7 : dayAppointmentsList.size();
    }

    class AppointmentsMainViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.lastDay)
        ImageView lastDay;
        @BindView(R.id.day_title)
        TextView dayTitle;
        @BindView(R.id.nextDay)
        ImageView nextDay;
        @BindView(R.id.appointments_recycler)
        RecyclerView appointmentsRecycler;

        private AppointmentsChildAdapter adapter;

        AppointmentsMainViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        void bind(final int position) {
            final DayAppointments dayAppointments = dayAppointmentsList.get(position);
            nextDay.setVisibility(((position < dayAppointmentsList.size() - 1) && (position < 6)) ? View.VISIBLE : View.INVISIBLE);
            lastDay.setVisibility((position > 0) ? View.VISIBLE : View.INVISIBLE);

            nextDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.smoothScrollToPosition(position + 1);
                }
            });
            lastDay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerView.smoothScrollToPosition(position - 1);
                }
            });

            Locale locale = new Locale(UserData.getLocalizationString(context));
            SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM", locale);
            dayTitle.setText(sdf.format(new Date(dayAppointments.getTitle())));

            adapter = new AppointmentsChildAdapter(context, dayAppointments, dayAppointments.getAppointments());
            appointmentsRecycler.setHasFixedSize(true);
            appointmentsRecycler.setLayoutManager(new GridLayoutManager(context, 3));
            appointmentsRecycler.setItemAnimator(new DefaultItemAnimator());
            appointmentsRecycler.setAdapter(adapter);

            adapter.setCallBack(new AppointmentsChildAdapter.CallBack() {
                @Override
                public void onAppointmentClicked(Appointment appointment) {
                    if (callBack != null)
                        callBack.onAppointmentClicked(appointment, dayAppointments);
                }
            });
        }

    }

    public interface CallBack {

        void onAppointmentClicked(Appointment appointment, DayAppointments dayAppointments);
    }
}
