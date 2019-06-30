package com.clinica.patient.Activities.CategoryDetails;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Adapters.DiseaseDetailsAdapter;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryDetailsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.item_img)
    ImageView itemImg;
    @BindView(R.id.item_recycler)
    RecyclerView itemRecycler;

    private DiseaseDetailsAdapter diseaseDetailsAdapter;

    @Override
    protected int setLayoutView() {
        return R.layout.activity_category_details;
    }

    @Override
    protected void initViews() {
        ButterKnife.bind(this);
        switch (getIntent().getStringExtra(Constants.Intents.SELECTION_KEY)) {
            case Constants.Intents.SELECT_DISEASES:
                toolbar.setTitle(getString(R.string.diseases_details_title));
                break;
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void initActions() {
        diseaseDetailsAdapter = new DiseaseDetailsAdapter(this);
        itemRecycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        itemRecycler.setLayoutManager(llm);
        itemRecycler.setItemAnimator(new DefaultItemAnimator());
        itemRecycler.setAdapter(diseaseDetailsAdapter);
    }
}
