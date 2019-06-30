package com.clinica.patient.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.Models.News;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private OnNewsClicked onNewsClicked;
    private DatabaseReference mReference;
    private int type;
    private List<News> newsList;

    public NewsAdapter(Context context, int type) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mReference = FirebaseDatabase.getInstance().getReference();
        this.type = type;
        this.newsList = new ArrayList<>();
    }

    public void setOnNewsClicked(OnNewsClicked onNewsClicked) {
        this.onNewsClicked = onNewsClicked;
    }

    public void addNews(News news) {
        if (news.getStatus().equals(Constants.ACTIVE_STATUS)) {
            newsList.add(news);
            sortList();
        }
    }

    public void removeNews(News news) {
        for (int i = 0; i < newsList.size(); i++) {
            if (newsList.get(i).getId().equals(news.getId())) {
                newsList.remove(i);
                sortList();
            }
        }
    }

    public void updateNews(News news) {
        if (news.getStatus().equals(Constants.ACTIVE_STATUS)) {
            boolean isInList = false;
            for (int i = 0; i < newsList.size(); i++) {
                if (newsList.get(i).getId().equals(news.getId())) {
                    isInList = true;
                    newsList.set(i, news);
                    sortList();
                }
            }
            if (!isInList) {
                addNews(news);
            }
        } else {
            removeNews(news);
        }
    }

    private void sortList() {
        Collections.sort(newsList, new Comparator<News>() {
            @Override
            public int compare(News news1, News news2) {
                return Long.compare(news2.getDate(), news1.getDate());
            }
        });
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View itemView;
        if (type == Constants.HOME_NEWS_VIEW)
            itemView = inflater.inflate(R.layout.home_news_item, viewGroup, false);
        else
            itemView = inflater.inflate(R.layout.row_news_item, viewGroup, false);
        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsViewHolder holder, int position) {
        final News news = newsList.get(position);
        mReference.child(Constants.DataBase.Doctors_NODE).child(news.getPublisherID())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final Doctor doctor = dataSnapshot.getValue(Doctor.class);

                        mReference.child(Constants.DataBase.Specialization_NODE).child(news.getSpecializationID())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final Specialization specialization = dataSnapshot.getValue(Specialization.class);

                                        holder.bind(news, doctor, specialization);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        if (type == Constants.LIST_NEWS_VIEW)
            return newsList.size();
        else if (type == Constants.HOME_NEWS_VIEW) {
            return (newsList.size() >= Constants.LAST_NEWS_COUNT ? Constants.LAST_NEWS_COUNT : newsList.size());
        }
        return 0;
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.news_img)
        RoundedImageView newsImg;
        @BindView(R.id.news_title)
        TextView newsTitleTV;
        @BindView(R.id.news_publisher)
        TextView newsPublisherTV;
        @BindView(R.id.news_specialization)
        TextView newsSpecializationTV;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final News news, final Doctor newsPublisher, final Specialization newsSpecialization) {
            Picasso.get()
                    .load(news.getImageURL())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_placeholder)
                    .into(newsImg);
            newsTitleTV.setText(news.getTitle());
            if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                newsPublisherTV.setText(newsPublisher.getBasicInformationAr().getDisplayName());
                newsSpecializationTV.setText(newsSpecialization.getTitleAr());
            } else {
                newsPublisherTV.setText(newsPublisher.getBasicInformationEN().getDisplayName());
                newsSpecializationTV.setText(newsSpecialization.getTitleEn());
            }


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onNewsClicked != null)
                        onNewsClicked.onClick(news, newsPublisher, newsSpecialization);
                }
            });
        }
    }

    public interface OnNewsClicked {
        void onClick(News news, Doctor newsPublisher, Specialization newsSpecialization);
    }
}
