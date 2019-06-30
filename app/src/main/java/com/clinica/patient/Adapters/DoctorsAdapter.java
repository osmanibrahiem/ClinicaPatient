package com.clinica.patient.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clinica.patient.Activities.Doctor.DoctorProfile.DoctorProfileActivity;
import com.clinica.patient.Models.Doctor.Doctor;
import com.clinica.patient.Models.Doctor.Specialization;
import com.clinica.patient.Models.Doctor.VisitorRate;
import com.clinica.patient.Models.User;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.willy.ratingbar.BaseRatingBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DoctorsAdapter extends RecyclerView.Adapter<DoctorsAdapter.DoctorsViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private DatabaseReference mReference;
    private List<Doctor> doctorList;

    public DoctorsAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mReference = FirebaseDatabase.getInstance().getReference();
        this.doctorList = new ArrayList<>();
    }

    public void appendData(List<Doctor> doctors) {
        int start = doctorList.size();
        doctorList.addAll(doctors);
        notifyItemRangeInserted(start, doctors.size());
    }

   /* public void addData(Doctor doctor) {
        doctorList.add(doctor);
        sortList();
    }

    public void updateData(Doctor doctor) {
        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getUid().equals(doctor.getUid())) {
                doctorList.set(i, doctor);
                sortList();
            }
        }
    }

    private void sortList() {
        Collections.sort(doctorList, new Comparator<Doctor>() {
            @Override
            public int compare(Doctor doctor1, Doctor doctor2) {
                if (UserData.getLocalization(context) == Localization.ARABIC_VALUE)
                    return doctor1.getBasicInformationAr().getDisplayName().compareTo(doctor2.getBasicInformationAr().getDisplayName());
                else
                    return doctor1.getBasicInformationEN().getDisplayName().compareTo(doctor2.getBasicInformationEN().getDisplayName());
            }
        });
        notifyDataSetChanged();
    }

    public void removeData(Doctor doctor) {
        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).getUid().equals(doctor.getUid())) {
                doctorList.remove(i);
                sortList();
            }
        }
    }*/

    public boolean filterByName(List<Doctor> fullList, String q) {
        q = q.toLowerCase();
        doctorList.clear();
        if (TextUtils.isEmpty(q)) {
            doctorList.addAll(fullList);
        } else {
            for (int i = 0; i < fullList.size(); i++) {
                if (fullList.get(i).getBasicInformationAr().getDisplayName().contains(q) ||
                        fullList.get(i).getBasicInformationEN().getDisplayName().contains(q)) {
                    doctorList.add(fullList.get(i));
                }
            }
        }
//        sortList();
        notifyDataSetChanged();
        return doctorList.size() > 0;
    }

    @NonNull
    @Override
    public DoctorsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_doctor_booking_item, viewGroup, false);
        return new DoctorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DoctorsViewHolder holder, int position) {
        final Doctor doctor = doctorList.get(position);
        holder.bind(doctor);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    class DoctorsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_doctor)
        CircularImageView imgDoctor;
        @BindView(R.id.doctor_name)
        TextView doctorName;
        @BindView(R.id.doctor_specialization)
        TextView doctorSpecialization;
        @BindView(R.id.user_rating)
        BaseRatingBar userRating;
        @BindView(R.id.user_rating_count)
        TextView userRatingCount;
        @BindView(R.id.doctor_price)
        TextView doctorPrice;
        @BindView(R.id.doctor_address)
        TextView doctorAddress;
        @BindView(R.id.book_btn)
        AppCompatButton bookBtn;

        DoctorsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Doctor doctor) {
            if (!TextUtils.isEmpty(doctor.getProfilePhotoUrl()))
                Picasso.get()
                        .load(doctor.getProfilePhotoUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_placeholder)
                        .into(imgDoctor);

            if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                doctorName.setText(doctor.getBasicInformationAr().getDisplayName());
                doctorSpecialization.setText(doctor.getSpecialization().getTitleAr());
            } else {
                doctorName.setText(doctor.getBasicInformationEN().getDisplayName());
                doctorSpecialization.setText(doctor.getSpecialization().getTitleEn());
            }
            if (doctor.getVisitorRates() != null && doctor.getVisitorRates().size() > 0) {
                double rate = 0;
                for (VisitorRate vr : doctor.getVisitorRates())
                    rate = rate + vr.getRate();
                userRating.setRating((float) (rate / doctor.getVisitorRates().size()));
                userRatingCount.setText(context.getString(R.string.visitors_rates_count, doctor.getVisitorRates().size()));
                userRating.setVisibility(View.VISIBLE);
                userRatingCount.setVisibility(View.VISIBLE);
            }
            doctorPrice.setText(context.getString(R.string.price, doctor.getClinicInformation().getExamination()));

            if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                doctorAddress.setText(doctor.getClinicInformation().getAddressAr().getFullAddress(context, Localization.ARABIC_VALUE));
            } else {
                doctorAddress.setText(doctor.getClinicInformation().getAddressEn().getFullAddress(context, Localization.ENGLISH_VALUE));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DoctorProfileActivity.class);
                    intent.putExtra(Constants.Intents.DOCTOR_ID, doctor.getUid());
                    context.startActivity(intent);
                }
            });

            bookBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DoctorProfileActivity.class);
                    intent.putExtra(Constants.Intents.DOCTOR_ID, doctor.getUid());
                    context.startActivity(intent);
                }
            });
        }

    }

}
