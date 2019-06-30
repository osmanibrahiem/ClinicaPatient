package com.clinica.patient.Activities.Main;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.clinica.patient.Activities.AskFragment;
import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.BookingList.AppointmentsFragment;
import com.clinica.patient.Activities.Home.HomeFragment;
import com.clinica.patient.Activities.Notifications.NotificationsFragment;
import com.clinica.patient.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    private Fragment active = null;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void initActions() {

    }

    @OnClick(R.id.ask_btn)
    void onAskSelected() {
        navigation.setSelectedItemId(R.id.nav_ask);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    openHome();
                    return true;
                case R.id.nav_appoints:
                    openAppoints();
                    return true;
                case R.id.nav_ask:
                    openAsk();
                    return true;
                case R.id.nav_notifications:
                    openNotifications();
                    return true;
                case R.id.nav_profile:
//                    openProfile();
                    return true;
            }
            return false;
        }
    };

    private void openHome() {
        if (active == null || (!active.getClass().getSimpleName().equals(HomeFragment.class.getSimpleName()))) {
            active = new HomeFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame_container, active).commit();
        }
    }

    private void openAppoints() {
        if (active != null && (!active.getClass().getSimpleName().equals(AppointmentsFragment.class.getSimpleName()))) {
            active = new AppointmentsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame_container, active).commit();
        }
    }

    private void openAsk() {
        if (active != null && (!active.getClass().getSimpleName().equals(AskFragment.class.getSimpleName()))) {
            active = new AskFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame_container, active).commit();
        }
    }

    private void openNotifications() {
        if (active != null && (!active.getClass().getSimpleName().equals(NotificationsFragment.class.getSimpleName()))) {
            active = new NotificationsFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.home_frame_container, active).commit();
        }
    }

    private void openProfile() {

    }

    @Override
    public void onBackPressed() {
        if (active == null || (!active.getClass().getSimpleName().equals(HomeFragment.class.getSimpleName())))
            navigation.setSelectedItemId(R.id.nav_home);
        else
            super.onBackPressed();

    }
}
