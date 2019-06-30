package com.clinica.patient.Tools;

import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.News;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public final class Constants {

    public static final int PROFILE_IMAGE_RATIO_WIDTH = 1;
    public static final int PROFILE_IMAGE_RATIO_HIGHT = 1;
    public static final int MAXIMUM_PROFILE_IMAGE_WIDTH = 2000;
    public static final int MAXIMUM_PROFILE_IMAGE_HIGHT = 2000;
    public static final int LAST_NEWS_COUNT = 5;
    public static final int LAST_QUESTIONS_COUNT = 5;
    public static final String ACTIVE_STATUS = "Active";
    public static final int HOME_NEWS_VIEW = 909;
    public static final int LIST_NEWS_VIEW = 806;
    public static final int HOME_CONSULTATIONS_VIEW = 708;
    public static final int LIST_CONSULTATIONS_VIEW = 401;
    public static final int NO_ACTION = -1;
    public static final int DOCTOR_ACTION = 1;
    public static final int DISEASE_ACTION = 2;
    public static final int SYMPTOM_ACTION = 3;


    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";

    public final class DataBase {

        public static final String USERS_NODE = "Users";
        public static final String CITIES_NODE = "Cities";
        public static final String ADS_NODE = "Ads";
        public static final String Doctors_NODE = "Doctors";
        public static final String Diseases_NODE = "Diseases";
        public static final String Symptoms_NODE = "Symptoms";
        public static final String News_NODE = "News";
        public static final String News_publisherID_NODE = "publisherID";
        public static final String News_specializationID_NODE = "specializationID";
        public static final String News_Date_NODE = "date";
        public static final String Specialization_NODE = "Specialization";
        public static final String Consultation_NODE = "Consultations";
        public static final String Answer_Date_NODE = "answerDate";
        public static final String Bookings_NODE = "Bookings";
        public static final String Notifications_NODE = "Notifications";
    }

    public final class Intents {

        public static final String SELECTION_KEY = "_selection";
        public static final String SELECT_CITY = "_city";
        public static final String SELECT_SPECIALIZATION = "_specialization";
        public static final String SELECT_DISEASES = "_diseases";

        public static final String CITY_DATA = "_city_data";
        public static final String SPECIALIZATION_DATA = "_specialization_data";
        public static final String LOCATION_DATA = "_location_data";
        public static final String DISEASES_ID = "_diseases_id";
        public static final String DOCTOR_ID = "_doctor_id";

        public static final String ADDRESS_AR = "_address_ar";
        public static final String ADDRESS_EN = "_address_en";

        public static final String BOOKING_ID = "_booking_id";
        public static final String BOOKING_APPOINTMENTS = "_booking_appointments";
        public static final String QUESTION_ID = "_question_id";
        public static final String QUESTIONS_USER = "_questions_user";
    }

    public final class Storage {

        public static final String PROFILE_IMAGES_FOLDER = "profile_images";
        public static final String QUESTIONS_IMAGES_FOLDER = "questions_images";
    }
}
