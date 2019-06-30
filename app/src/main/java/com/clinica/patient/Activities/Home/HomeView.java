package com.clinica.patient.Activities.Home;

import com.clinica.patient.Activities.Base.BaseView;
import com.clinica.patient.Models.Ads;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.News;

public interface HomeView extends BaseView {

    void initAdsRecycler(long adsCount);

    void hideAdsPager();

    void addAds(Ads ads);

    void updateAds(Ads ads);

    void deleteAds(Ads ads);

    void setDoctorsCount(long count);

    void setDiseasesCount(long count);

    void setSymptomsCount(long count);

    void initNewsSection();

    void hideNewsSection();

    void addNews(News news);

    void updateNews(News news);

    void deleteNews(News news);

    void initConsultationsSection();

    void hideConsultationsList();

    void addConsultation(Consultation consultation);

    void updateConsultation(Consultation consultation);

    void deleteConsultations(Consultation consultation);
}
