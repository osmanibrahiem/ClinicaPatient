package com.clinica.patient.Activities.Splash;

import android.content.Intent;
import android.widget.ImageView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Auth.Login.LoginActivity;
import com.clinica.patient.Activities.Main.MainActivity;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Localization;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends BaseActivity implements SplashView {

    private SplashPresenter presenter;

    @BindView(R.id.splash_image_view)
    ImageView splashImageView;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        presenter = new SplashPresenter(this, this);
        presenter.setLanguage(Localization.ARABIC_VALUE);
    }

    @Override
    protected void initActions() {
        presenter.animateLogo(splashImageView);
    }

    @Override
    public void openNextActivity() {
/*
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Appointment appointment1 = new Appointment();
        appointment1.setStatus(Constants.ACTIVE_STATUS);
        appointment1.setTime(1560679200000L);

        Appointment appointment2 = new Appointment();
        appointment2.setStatus(Constants.ACTIVE_STATUS);
        appointment2.setTime(1560681000000L);

        Appointment appointment3 = new Appointment();
        appointment3.setStatus(Constants.ACTIVE_STATUS);
        appointment3.setTime(1560682800000L);

        Appointment appointment4 = new Appointment();
        appointment4.setStatus(Constants.ACTIVE_STATUS);
        appointment4.setTime(1560684600000L);

        Appointment appointment5 = new Appointment();
        appointment5.setStatus(Constants.ACTIVE_STATUS);
        appointment5.setTime(1560686400000L);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment1);
        appointmentList.add(appointment2);
        appointmentList.add(appointment3);
        appointmentList.add(appointment4);
        appointmentList.add(appointment5);

        DayAppointments dayAppoinments1 = new DayAppointments();
        dayAppoinments1.setAppointments(appointmentList);
        dayAppoinments1.setTitle(1560688200000L);

        DayAppointments dayAppoinments2 = new DayAppointments();
        dayAppoinments2.setAppointments(appointmentList);
        dayAppoinments2.setTitle(1560722400000L);

        DayAppointments dayAppoinments3 = new DayAppointments();
        dayAppoinments3.setAppointments(appointmentList);
        dayAppoinments3.setTitle(1560722400000L);

        DayAppointments dayAppoinments4 = new DayAppointments();
        dayAppoinments4.setAppointments(appointmentList);
        dayAppoinments4.setTitle(1560808800000L);

        DayAppointments dayAppoinments5 = new DayAppointments();
        dayAppoinments5.setAppointments(appointmentList);
        dayAppoinments5.setTitle(1560895200000L);

        DayAppointments dayAppoinments6 = new DayAppointments();
        dayAppoinments6.setAppointments(appointmentList);
        dayAppoinments6.setTitle(1560895200000L);

        DayAppointments dayAppoinments7 = new DayAppointments();
        dayAppoinments7.setAppointments(appointmentList);
        dayAppoinments7.setTitle(1560981600000L);

        List<DayAppointments> dayAppoinmentsList = new ArrayList<>();
        dayAppoinmentsList.add(dayAppoinments1);
        dayAppoinmentsList.add(dayAppoinments2);
        dayAppoinmentsList.add(dayAppoinments3);
        dayAppoinmentsList.add(dayAppoinments4);
        dayAppoinmentsList.add(dayAppoinments5);
        dayAppoinmentsList.add(dayAppoinments6);
        dayAppoinmentsList.add(dayAppoinments7);

        VisitorRate rate1 = new VisitorRate();
        rate1.setComment("Good Doctor");
        rate1.setDate(1560070800000L);
        rate1.setDoctorID("9xDjQHGlS0WlAIzk0v1UySnIU0F3");
        rate1.setRate(4.5);

        VisitorRate rate2 = new VisitorRate();
        rate2.setComment("Good Doctor");
        rate2.setDate(1560074400000L);
        rate2.setDoctorID("9xDjQHGlS0WlAIzk0v1UySnIU0F3");
        rate2.setRate(4);

        VisitorRate rate3 = new VisitorRate();
        rate3.setComment("Bad Doctor");
        rate3.setDate(1560078000000L);
        rate3.setDoctorID("9xDjQHGlS0WlAIzk0v1UySnIU0F3");
        rate3.setRate(1.5);

        List<VisitorRate> visitorRates = new ArrayList<>();
        visitorRates.add(rate1);
        visitorRates.add(rate2);
        visitorRates.add(rate3);

        Doctor doctor1 = new Doctor();
        doctor1.setNationalID("12345678901234");
        doctor1.setPrice(60);
        doctor1.setSpecializationID("-ertyuikmnbvcxsdfvg");
        doctor1.setStringAddress("طنطا - شارع حسان بن ثابت");
        doctor1.setVisitorRates(visitorRates);
        doctor1.setAccountStatus(Constants.ACTIVE_STATUS);
        doctor1.setBirthdayTimestamp(850341600000L);
        doctor1.setCityID(2);
        doctor1.setCreationTimestamp(1560117600000L);
        doctor1.setDisplayName("عثمان ابراهيم");
        doctor1.setEmail("oelfannan@gmail.com");
        doctor1.setGender("Male");
        doctor1.setPhoneNumber("01126024180");
        doctor1.setPhotoUrl("https://firebasestorage.googleapis.com/v0/b/clinica-3edfd.appspot.com/o/profile_images?alt=media&token=76b519c8-2827-4e94-b264-07e387f5c194");
        doctor1.setDayAppoinments(dayAppoinmentsList);

        Doctor doctor2 = new Doctor();
        doctor2.setNationalID("12345678901234");
        doctor2.setPrice(100);
        doctor2.setSpecializationID("-kjh85hgfd2ytrewsx");
        doctor2.setStringAddress("طنطا - شارع سعيد مع محب");
        doctor2.setVisitorRates(visitorRates);
        doctor2.setAccountStatus(Constants.ACTIVE_STATUS);
        doctor2.setBirthdayTimestamp(850341600000L);
        doctor2.setCityID(1);
        doctor2.setCreationTimestamp(1560117600000L);
        doctor2.setDisplayName("أحمد محمد");
        doctor2.setEmail("oelfannan@gmail.com");
        doctor2.setGender("Male");
        doctor2.setPhoneNumber("01126024180");
        doctor2.setPhotoUrl("https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=2350268385040114&width=2000&ext=1562425708&hash=AeSMPs5ykP4PtUug");
        doctor2.setDayAppoinments(dayAppoinmentsList);

        reference.child(Constants.DataBase.Doctors_NODE).child("9xDjQHGlS0WlAIzk0v1UySnIU0F3").setValue(doctor1);
        reference.child(Constants.DataBase.Doctors_NODE).child("Q1NIyUJQlXSyE2qac5X8hxKRKNX2").setValue(doctor2);
*/
        Intent open;
        if (presenter.checkLogin()) {
            open = new Intent(this, LoginActivity.class);
        } else {
            open = new Intent(this, MainActivity.class);
        }
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(open);
    }
}
