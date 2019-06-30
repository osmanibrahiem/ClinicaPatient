package com.clinica.patient.Activities.Base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.clinica.patient.Models.NotificationModel;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class BasePresenter {

    private BaseView view;
    private BaseActivity activity;
    private BaseFragment fragment;

    public BasePresenter(BaseView view, BaseActivity activity) {
        this.view = view;
        this.activity = activity;
    }

    public BasePresenter(BaseView view, BaseFragment fragment) {
        this(view, (BaseActivity) fragment.getActivity());
        this.view = view;
        this.fragment = fragment;
    }

    public void hideKeypad() {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.setFocusableInTouchMode(false);
            view.setFocusable(false);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager mConnectivity =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivity.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            view.showNetWorkError(R.string.message_no_internet);
            return false;
        }
    }

    public void sendNotificationToDoctor(String doctorID, String type, String catID, String destinationID) {
        NotificationModel notification = new NotificationModel();
        notification.setCatID(catID);
        notification.setDestinationID(destinationID);
        notification.setFrom(FirebaseAuth.getInstance().getUid());
        notification.setTo(doctorID);
        notification.setDate(new Date().getTime());
        notification.setType(type);

        FirebaseDatabase.getInstance().getReference().child(Constants.DataBase.Notifications_NODE)
                .push().setValue(notification);
    }
}
