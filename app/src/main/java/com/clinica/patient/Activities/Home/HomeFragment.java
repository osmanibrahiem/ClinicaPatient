package com.clinica.patient.Activities.Home;

import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Base.BaseFragment;
import com.clinica.patient.Activities.Doctor.DoctorsList.DoctorListActivity;
import com.clinica.patient.Activities.QuestionList.QuestionListActivity;
import com.clinica.patient.Activities.SearchWithActivity;
import com.clinica.patient.Activities.Selector.SelectorActivity;
import com.clinica.patient.Adapters.AdsAdapter;
import com.clinica.patient.Adapters.ConsultationAdapter;
import com.clinica.patient.Adapters.NewsAdapter;
import com.clinica.patient.Models.Ads;
import com.clinica.patient.Models.Consultation;
import com.clinica.patient.Models.News;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.rd.PageIndicatorView;
import com.rd.draw.data.RtlMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends BaseFragment implements HomeView {

    public HomeFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.ads_recycler)
    RecyclerView adsRecycler;
    @BindView(R.id.pageIndicatorView)
    PageIndicatorView indicator;
    @BindView(R.id.logo)
    AppCompatImageView logo;
    @BindView(R.id.doctors_btn)
    LinearLayout doctorsBtn;
    @BindView(R.id.doctors_count)
    TextView doctorsCountTV;
    @BindView(R.id.diseases_btn)
    LinearLayout diseasesBtn;
    @BindView(R.id.diseases_count)
    TextView diseasesCountTV;
    @BindView(R.id.symptoms_btn)
    LinearLayout symptomsBtn;
    @BindView(R.id.symptoms_count)
    TextView symptomsCountTV;
    @BindView(R.id.news_section)
    ConstraintLayout newsSection;
    @BindView(R.id.see_all_news_btn)
    AppCompatButton seeAllNewsBtn;
    @BindView(R.id.news_recycler)
    RecyclerView newsRecycler;
    @BindView(R.id.consultation_section)
    ConstraintLayout consultationSection;
    @BindView(R.id.see_all_consultations_btn)
    AppCompatButton seeAllConsultationsBtn;
    @BindView(R.id.consultation_recycler)
    RecyclerView consultationRecycler;

    private HomePresenter presenter;

    private AdsAdapter adsAdapter;
    private NewsAdapter newsAdapter;
    private ConsultationAdapter consultationAdapter;

    private int mCurrentPage = 0;
    private Handler handler;
    private int delay = 5000; //milliseconds
    private Runnable runnable = new Runnable() {
        public void run() {
            if (adsRecycler.getAdapter() != null && adsRecycler.getAdapter().getItemCount() != 0) {
                mCurrentPage++;
                mCurrentPage = mCurrentPage % adsRecycler.getAdapter().getItemCount();
                adsRecycler.smoothScrollToPosition(mCurrentPage);
                handler.postDelayed(this, delay);
            }
        }
    };

    @Override
    protected int setLayoutView() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initViews(View view) {
        ButterKnife.bind(this, view);
        presenter = new HomePresenter(this, this);
        handler = new Handler();
        if (UserData.getLocalization(getActivity()) == Localization.ARABIC_VALUE) {
            indicator.setRtlMode(RtlMode.On);
        }

        if (presenter.isNetworkAvailable()) {
            showLoading();
        }
        presenter.checkAds();
        presenter.getDoctorsCount();
        presenter.getDiseasesCount();
        presenter.getSymptomsCount();
        presenter.checkNews();
        presenter.checkConsultations();
    }

    @Override
    protected void initActions() {

    }

    @Override
    public void initAdsRecycler(long adsCount) {
        hideLoading();
        adsAdapter = new AdsAdapter(getActivity());
        adsRecycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        adsRecycler.setLayoutManager(llm);
        adsRecycler.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        adsRecycler.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(adsRecycler);
        adsRecycler.setAdapter(adsAdapter);
        indicator.setCount(1);
        adsRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = ((LinearLayoutManager) recyclerView.getLayoutManager())
                            .findFirstVisibleItemPosition();
                    indicator.setCount(recyclerView.getAdapter().getItemCount());
                    indicator.setSelection(position);
                    mCurrentPage = position;
                }
            }
        });
        showAdsPager();
    }

    private void showAdsPager() {
        adsRecycler.setVisibility(View.VISIBLE);
        indicator.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
        handler.postDelayed(runnable, delay);
    }

    @Override
    public void hideAdsPager() {
        adsRecycler.setVisibility(View.GONE);
        indicator.setVisibility(View.GONE);
        logo.setVisibility(View.VISIBLE);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void addAds(Ads ads) {
        adsAdapter.addAds(ads);
        indicator.setCount(adsAdapter.getItemCount());
    }

    @Override
    public void updateAds(Ads ads) {
        adsAdapter.updateAds(ads);
        indicator.setCount(adsAdapter.getItemCount());
    }

    @Override
    public void deleteAds(Ads ads) {
        adsAdapter.removeAds(ads);
        indicator.setCount(adsAdapter.getItemCount());
    }

    @Override
    public void setDoctorsCount(long count) {
        if (isAdded()) {
            doctorsCountTV.setVisibility(View.VISIBLE);
            doctorsCountTV.setText(getString(R.string.num_doctors, count));
        }
    }

    @Override
    public void setDiseasesCount(long count) {
        if (isAdded()) {
            diseasesCountTV.setVisibility(View.VISIBLE);
            diseasesCountTV.setText(getString(R.string.num_diseases, count));
        }
    }

    @Override
    public void setSymptomsCount(long count) {
        if (isAdded()) {
            symptomsCountTV.setVisibility(View.VISIBLE);
            symptomsCountTV.setText(getString(R.string.num_symptoms, count));
        }
    }

    @Override
    public void initNewsSection() {
        newsAdapter = new NewsAdapter(getActivity(), Constants.HOME_NEWS_VIEW);
        newsRecycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        newsRecycler.setLayoutManager(llm);
        newsRecycler.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(newsRecycler);
        newsRecycler.setAdapter(newsAdapter);
        showNewsList();
    }

    private void showNewsList() {
        newsSection.setVisibility(View.VISIBLE);
        newsRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNewsSection() {
        newsSection.setVisibility(View.GONE);
        newsRecycler.setVisibility(View.GONE);
    }

    @Override
    public void addNews(News news) {
        newsAdapter.addNews(news);
    }

    @Override
    public void updateNews(News news) {
        newsAdapter.updateNews(news);
    }

    @Override
    public void deleteNews(News news) {
        newsAdapter.removeNews(news);
    }

    @Override
    public void initConsultationsSection() {
        consultationAdapter = new ConsultationAdapter(getActivity(), Constants.HOME_CONSULTATIONS_VIEW);
        consultationRecycler.setHasFixedSize(false);
        consultationRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        consultationRecycler.setNestedScrollingEnabled(false);
        consultationRecycler.setItemAnimator(new DefaultItemAnimator());
        consultationRecycler.setAdapter(consultationAdapter);
        showConsultationsList();
    }

    private void showConsultationsList() {
        consultationSection.setVisibility(View.VISIBLE);
        consultationRecycler.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideConsultationsList() {
        consultationSection.setVisibility(View.GONE);
        consultationRecycler.setVisibility(View.GONE);
    }

    @Override
    public void addConsultation(Consultation consultation) {
        consultationAdapter.addConsultation(consultation);
    }

    @Override
    public void updateConsultation(Consultation consultation) {
        consultationAdapter.updateConsultation(consultation);
    }

    @Override
    public void deleteConsultations(Consultation consultation) {
        consultationAdapter.removeConsultation(consultation);
    }

    @OnClick(R.id.see_all_consultations_btn)
    void seeAllConsultations() {
        Intent intent = new Intent(getActivity(), QuestionListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.see_all_news_btn)
    void seeAllNews() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private static final int SELECT_SPECIALIZATION_RC = 85;

    @OnClick(R.id.doctors_btn)
    void openSearchForDoctor() {
        Intent intent = new Intent(getActivity(), SelectorActivity.class);
        intent.putExtra(Constants.Intents.SELECTION_KEY, Constants.Intents.SELECT_SPECIALIZATION);
        startActivityForResult(intent, SELECT_SPECIALIZATION_RC);
    }

    @OnClick(R.id.diseases_btn)
    void openDiseasesSelector() {
        Intent intent = new Intent(getActivity(), SelectorActivity.class);
        intent.putExtra(Constants.Intents.SELECTION_KEY, Constants.Intents.SELECT_DISEASES);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_SPECIALIZATION_RC && resultCode == RESULT_OK && data != null) {
            Intent doctorIntent = new Intent(getActivity(), DoctorListActivity.class);
            doctorIntent.putExtra(Constants.Intents.SPECIALIZATION_DATA, data.getParcelableExtra(Constants.Intents.SPECIALIZATION_DATA));
            startActivity(doctorIntent);
        }
    }
}
