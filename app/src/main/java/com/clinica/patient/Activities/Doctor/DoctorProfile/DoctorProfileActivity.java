package com.clinica.patient.Activities.Doctor.DoctorProfile;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Doctor.Booking.BookingActivity;
import com.clinica.patient.Activities.QuestionList.QuestionListActivity;
import com.clinica.patient.Adapters.AppointmentsMainAdapter;
import com.clinica.patient.Adapters.ConsultationAdapter;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.Doctor.Appointment;
import com.clinica.patient.Models.Doctor.DayAppointments;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.VisitorRate;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.willy.ratingbar.BaseRatingBar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DoctorProfileActivity extends BaseActivity implements DoctorProfileView {

    private DoctorProfilePresenter presenter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_container)
    NestedScrollView mainContainer;
    @BindView(R.id.empty)
    TextView errorMessageTV;
    @BindView(R.id.img_doctor)
    CircularImageView imgDoctor;
    @BindView(R.id.doctor_name)
    TextView doctorName;
    @BindView(R.id.doctor_specialization)
    TextView doctorSpecialization;
    @BindView(R.id.user_rating)
    BaseRatingBar userRating;
    @BindView(R.id.user_rating_count)
    TextView userRatingCount;
    @BindView(R.id.doctor_price)
    TextView doctorPrice;
    @BindView(R.id.doctor_address)
    TextView doctorAddress;
    @BindView(R.id.appointments_section)
    ConstraintLayout appointmentsSection;
    @BindView(R.id.appointments_title)
    TextView appointmentsTitle;
    @BindView(R.id.appointments_recycler)
    RecyclerView appointmentsRecycler;
    @BindView(R.id.consultation_section)
    ConstraintLayout consultationSection;
    @BindView(R.id.consultation_title)
    TextView consultationTitle;
    @BindView(R.id.see_all_consultations_btn)
    AppCompatButton seeAllConsultationsBtn;
    @BindView(R.id.consultation_recycler)
    RecyclerView consultationRecycler;

    private AppointmentsMainAdapter appointmentsMainAdapter;
    private ConsultationAdapter consultationAdapter;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_doctor_profile;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new DoctorProfilePresenter(this, this);
    }

    @Override
    protected void initActions() {

        Log.i("DoctorProfile", "initActions: ");
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.Intents.DOCTOR_ID)) {
            String id = intent.getStringExtra(Constants.Intents.DOCTOR_ID);
            Log.i("DoctorProfile", "ID: " + id);
            presenter.checkDoctor(id);
        } else {
            Log.i("DoctorProfile", "Empty Intent ");
            showErrorMessage(R.string.message_doctor_not_found);
        }
    }

    @Override
    public void initViews(Doctor doctor) {
        mainContainer.setVisibility(View.VISIBLE);
        errorMessageTV.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(doctor.getProfilePhotoUrl()))
            Picasso.get()
                    .load(doctor.getProfilePhotoUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_placeholder)
                    .into(imgDoctor);

        if (UserData.getLocalization(this) == Localization.ARABIC_VALUE) {
            doctorName.setText(doctor.getBasicInformationAr().getDisplayName());
            toolbar.setTitle(doctor.getBasicInformationAr().getDisplayName());
            doctorSpecialization.setText(doctor.getBasicInformationAr().getProfessionalTitle());
            toolbar.setSubtitle(doctor.getSpecialization().getTitleAr());
        } else {
            doctorName.setText(doctor.getBasicInformationEN().getDisplayName());
            toolbar.setTitle(doctor.getBasicInformationEN().getDisplayName());
            doctorSpecialization.setText(doctor.getBasicInformationEN().getProfessionalTitle());
            toolbar.setSubtitle(doctor.getSpecialization().getTitleEn());
        }
        if (doctor.getVisitorRates() != null && doctor.getVisitorRates().size() > 0) {
            double rate = 0;
            for (VisitorRate vr : doctor.getVisitorRates())
                rate = rate + vr.getRate();
            userRating.setRating((float) (rate / doctor.getVisitorRates().size()));
            userRatingCount.setText(getString(R.string.visitors_rates_count, doctor.getVisitorRates().size()));
            userRating.setVisibility(View.VISIBLE);
            userRatingCount.setVisibility(View.VISIBLE);
        }
        doctorPrice.setText(getString(R.string.price, doctor.getClinicInformation().getExamination()));
        if (UserData.getLocalization(this) == Localization.ARABIC_VALUE)
            doctorAddress.setText(doctor.getClinicInformation().getAddressAr().getAddress(this, Localization.ARABIC_VALUE));
        else
            doctorAddress.setText(doctor.getClinicInformation().getAddressEn().getAddress(this, Localization.ENGLISH_VALUE));

        if (doctor.getDayAppointments() != null && doctor.getDayAppointments().size() > 0) {
            initAppointmentsSection(doctor.getDayAppointments(), doctor.getUid());
        } else {
            appointmentsSection.setVisibility(View.GONE);
            appointmentsRecycler.setVisibility(View.GONE);
        }
    }

    private void initAppointmentsSection(List<DayAppointments> dayAppointments, final String doctorID) {
        appointmentsSection.setVisibility(View.VISIBLE);
        appointmentsRecycler.setVisibility(View.VISIBLE);
        appointmentsRecycler.setHasFixedSize(true);
        appointmentsRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        appointmentsRecycler.setLayoutManager(llm);
        appointmentsRecycler.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        appointmentsRecycler.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(appointmentsRecycler);
        appointmentsMainAdapter = new AppointmentsMainAdapter(this, dayAppointments, appointmentsRecycler);
        appointmentsRecycler.setAdapter(appointmentsMainAdapter);
        appointmentsMainAdapter.setCallBack(new AppointmentsMainAdapter.CallBack() {
            @Override
            public void onAppointmentClicked(Appointment appointment, DayAppointments dayAppointments) {
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

                Intent intent = new Intent(DoctorProfileActivity.this, BookingActivity.class);
                intent.putExtra(Constants.Intents.DOCTOR_ID, doctorID);
                intent.putExtra(Constants.Intents.BOOKING_APPOINTMENTS, bookingDateTime.getTimeInMillis());
                startActivity(intent);
            }
        });
    }

    @Override
    public void showErrorMessage(int message) {
        errorMessageTV.setText(message);
        mainContainer.setVisibility(View.GONE);
        errorMessageTV.setVisibility(View.VISIBLE);
    }

    @Override
    public void initConsultations(final List<Consultation> consultationList) {
        consultationSection.setVisibility(View.VISIBLE);
        consultationRecycler.setVisibility(View.VISIBLE);
        consultationAdapter = new ConsultationAdapter(this, Constants.HOME_CONSULTATIONS_VIEW);
        consultationAdapter.appendData(consultationList);
        consultationRecycler.setHasFixedSize(false);
        consultationRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        consultationRecycler.setNestedScrollingEnabled(false);
        consultationRecycler.setItemAnimator(new DefaultItemAnimator());
        consultationRecycler.setAdapter(consultationAdapter);

        seeAllConsultationsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorProfileActivity.this, QuestionListActivity.class);
                intent.putExtra(Constants.Intents.DOCTOR_ID, consultationList.get(0).getAnswerPublisherID());
                startActivity(intent);
            }
        });
    }
}
