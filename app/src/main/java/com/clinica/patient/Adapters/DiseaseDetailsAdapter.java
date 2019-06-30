package com.clinica.patient.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clinica.patient.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DiseaseDetailsAdapter extends RecyclerView.Adapter<DiseaseDetailsAdapter.DiseaseDetailsViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private DatabaseReference mReference;
    private int mExpandedPosition = 0;

    public DiseaseDetailsAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public DiseaseDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_category_detalies_item, viewGroup, false);
        return new DiseaseDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DiseaseDetailsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    class DiseaseDetailsViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.header)
        LinearLayout header;
        @BindView(R.id.arrow)
        ImageView arrow;
        @BindView(R.id.details)
        TextView details;

        DiseaseDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        void bind(final int position) {
            final boolean isExpanded = position == mExpandedPosition;
            details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            arrow.setImageResource(isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
            header.setActivated(isExpanded);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExpandedPosition = isExpanded ? -1 : position;
                    notifyDataSetChanged();
                }
            });

        }

    }

}
