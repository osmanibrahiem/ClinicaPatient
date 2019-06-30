package com.clinica.patient.Activities.Notifications;

import android.app.Notification;
import android.support.annotation.NonNull;

import com.clinica.patient.Activities.Base.BasePresenter;
import com.clinica.patient.Models.NotificationModel;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

class NotificationsPresenter extends BasePresenter {

    private NotificationsFragment fragment;
    private NotificationsView view;
    private DatabaseReference mReference;

    NotificationsPresenter(NotificationsView view, NotificationsFragment fragment) {
        super(view, fragment);
        this.fragment = fragment;
        this.view = view;
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    private String getID() {
        return FirebaseAuth.getInstance().getUid();
    }

    void getNotifications() {
        if (isNetworkAvailable()) {
            view.showLoading();
            mReference.child(Constants.DataBase.Notifications_NODE)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<NotificationModel> notifications = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                NotificationModel notification = snapshot.getValue(NotificationModel.class);
                                if (notification != null && notification.getTo().equals(getID())) {
                                    notification.setId(snapshot.getKey());
                                    notifications.add(notification);
                                }
                            }

                            if (notifications.size() > 0) {
                                view.initNotifications(notifications);
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
        } else {
            view.showMessage(R.string.message_no_internet);
        }
    }
}
