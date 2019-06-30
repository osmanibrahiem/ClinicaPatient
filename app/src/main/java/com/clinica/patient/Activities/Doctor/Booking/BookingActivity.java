package com.clinica.patient.Activities.Doctor.Booking;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Models.Booking;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.DateUtils;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.clinica.patient.Tools.ToastTool;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookingActivity extends BaseActivity implements BookingView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_container)
    NestedScrollView mainContainer;
    @BindView(R.id.img_patient)
    CircularImageView imgDoctor;
    @BindView(R.id.patient_name)
    TextView doctorName;
    @BindView(R.id.professional_title)
    TextView professionalTitle;
    @BindView(R.id.doctor_price)
    TextView doctorPrice;
    @BindView(R.id.examination_date)
    TextView examinationDate;
    @BindView(R.id.examination_duration)
    TextView examinationDuration;
    @BindView(R.id.input_notes)
    TextInputLayout inputNotes;
    @BindView(R.id.notes_et)
    AppCompatEditText notesEt;
    @BindView(R.id.confirm_btn)
    AppCompatButton confirmBtn;
    @BindView(R.id.empty)
    TextView empty;
    @BindView(R.id.call_btn)
    FloatingActionButton callBtn;

    private BookingPresenter presenter;
    private long appointmentDate;
    private Booking thisBooking;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_booking;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        toolbar.setTitle(R.string.booking_confirmation_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new BookingPresenter(this, this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.Intents.BOOKING_ID))
            presenter.getBookingDetails(intent.getStringExtra(Constants.Intents.BOOKING_ID));
        else if (intent != null && intent.hasExtra(Constants.Intents.BOOKING_APPOINTMENTS) &&
                intent.hasExtra(Constants.Intents.DOCTOR_ID)) {
            appointmentDate = intent.getLongExtra(Constants.Intents.BOOKING_APPOINTMENTS, 0);
            presenter.newBooking(appointmentDate,
                    intent.getStringExtra(Constants.Intents.DOCTOR_ID));
        }
    }

    @Override
    protected void initActions() {

    }

    @Override
    public void showMessage(int message) {
        empty.setText(message);
        mainContainer.setVisibility(View.GONE);
        callBtn.hide();
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void initDoctor(final Doctor doctor) {
        if (!TextUtils.isEmpty(doctor.getProfilePhotoUrl())) {
            Picasso.get()
                    .load(doctor.getProfilePhotoUrl())
                    .placeholder(R.drawable.profile_picture_blank_square)
                    .error(R.drawable.error_placeholder)
                    .into(imgDoctor);
        }
        if (UserData.getLocalization(this) == Localization.ARABIC_VALUE) {
            doctorName.setText(doctor.getBasicInformationAr().getDisplayName());
            professionalTitle.setText(doctor.getBasicInformationAr().getProfessionalTitle());
        } else {
            doctorName.setText(doctor.getBasicInformationEN().getDisplayName());
            professionalTitle.setText(doctor.getBasicInformationEN().getProfessionalTitle());
        }
        if (!TextUtils.isEmpty(doctor.getPhoneNumber())) {
            callBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + doctor.getPhoneNumber()));
                    startActivity(intent);
                }
            });
            callBtn.show();
        }

        examinationDuration.setText(presenter.getExaminationDuration());
        doctorPrice.setText(getString(R.string.booking_price, doctor.getClinicInformation().getExamination()));

        DateFormat dfEN = new SimpleDateFormat("EEEE, d MMMM 'at' hh:mm aa", new Locale("en"));
        DateFormat dfAR = new SimpleDateFormat("EEEE d MMMM 'في تمام الساعة' hh:mm aa", new Locale("ar"));
        DateFormat dfDayEN = new SimpleDateFormat(", d MMMM 'at' hh:mm aa", new Locale("en"));
        DateFormat dfDayAR = new SimpleDateFormat(" d MMMM 'في تمام الساعة' hh:mm aa", new Locale("ar"));
        if (UserData.getLocalization(this) == Localization.ARABIC_VALUE) {
            if (DateUtils.isToday(new Date(appointmentDate))) {
                examinationDate.setText(String.format("%1$s%2$s", getString(R.string.today), dfDayAR.format(new Date(appointmentDate))));
            } else if (DateUtils.isTomorrow(new Date(appointmentDate))) {
                examinationDate.setText(String.format("%1$s%2$s", getString(R.string.tomorrow), dfDayAR.format(new Date(appointmentDate))));
            } else {
                examinationDate.setText(dfAR.format(new Date(appointmentDate)));
            }
        } else {
            if (DateUtils.isToday(new Date(appointmentDate))) {
                examinationDate.setText(String.format("%1$s%2$s", getString(R.string.today), dfDayEN.format(new Date(appointmentDate))));
            } else if (DateUtils.isTomorrow(new Date(appointmentDate))) {
                examinationDate.setText(String.format("%1$s%2$s", getString(R.string.tomorrow), dfDayEN.format(new Date(appointmentDate))));
            } else {
                examinationDate.setText(dfEN.format(new Date(appointmentDate)));
            }
        }

        final Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.Intents.BOOKING_ID)) {
            if (thisBooking != null) {
                switch (thisBooking.getStatus()) {
                    case Booking.REQUESTED_STATUS:
                        confirmBtn.setText(R.string.cancel);
                        confirmBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                presenter.cancelRequestedBooking(intent.getStringExtra(Constants.Intents.BOOKING_ID));
                            }
                        });
                        break;
                    case Booking.PENDING_STATUS:
                        confirmBtn.setText(R.string.cancel);
                        confirmBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                presenter.cancelPendingBooking(intent.getStringExtra(Constants.Intents.BOOKING_ID));
                            }
                        });
                        break;
                    case Booking.CANCELED_STATUS:
                    case Booking.REFUSED_STATUS:
                        confirmBtn.setText(R.string.examination_canceled);
                        confirmBtn.setEnabled(false);
                        break;
                    case Booking.SUCCESS_STATUS:
                        confirmBtn.setText(R.string.examination_done);
                        confirmBtn.setEnabled(false);
                        break;
                }
            }
        } else if (intent != null && intent.hasExtra(Constants.Intents.BOOKING_APPOINTMENTS) &&
                intent.hasExtra(Constants.Intents.DOCTOR_ID)) {
            confirmBtn.setText(R.string.booking_confirm);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appointmentDate = intent.getLongExtra(Constants.Intents.BOOKING_APPOINTMENTS, 0);
                    presenter.confirmBooking(appointmentDate,
                            intent.getStringExtra(Constants.Intents.DOCTOR_ID));
                }
            });
        }

        mainContainer.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
    }

    @Override
    public void initBooking(Booking booking) {
        thisBooking = booking;
        appointmentDate = booking.getDate();
        notesEt.setText(booking.getMessage());
        notesEt.setEnabled(false);
        inputNotes.setEnabled(false);
    }

    @Override
    public void toastError(int message) {
        ToastTool.with(this, message).show();
    }

    @Override
    public void viewBookingDetails(String bookingID) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra(Constants.Intents.BOOKING_ID, bookingID);
        startActivity(intent);
        finish();
    }
}
