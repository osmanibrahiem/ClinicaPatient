package com.clinica.patient.Activities.Notifications;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.NotificationModel;

import java.util.List;

interface NotificationsView extends BaseView {

    void initNotifications(List<NotificationModel> notifications);

    void showMessage(int message);
}
