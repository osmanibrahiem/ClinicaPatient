package com.clinica.patient.Activities.BookingList;

import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.clinica.patient.Activities.Base.BaseActivity;
import com.clinica.patient.Activities.Base.BaseFragment;
import com.clinica.patient.Activities.Doctor.Booking.BookingActivity;
import com.clinica.patient.Adapters.BookingAdapter;
import com.clinica.patient.Models.Booking;
import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;
import com.clinica.patient.Tools.ToastTool;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppointmentsFragment extends BaseFragment implements AppointmentsView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.empty)
    AppCompatTextView empty;

    private AppointmentsPresenter presenter;
    private BookingAdapter adapter;

    public AppointmentsFragment() {
    }

    @Override
    protected int setLayoutView() {
        return R.layout.layout_recycler;
    }

    @Override
    protected void initViews(View view) {
        ButterKnife.bind(this, view);
        if (getActivity() != null) {
            toolbar.setTitle(R.string.appointments_title);
            ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
            ((BaseActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        presenter = new AppointmentsPresenter(this, this);
    }

    @Override
    protected void initActions() {
        presenter.getBookings();
    }

    @Override
    public void initBookings(List<Booking> bookings) {
        recycler.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        adapter = new BookingAdapter(getActivity(), bookings);
        recycler.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(llm);
        recycler.setItemAnimator(new DefaultItemAnimator());
        recycler.setAdapter(adapter);
        adapter.setCallBack(new BookingAdapter.BookingsCallBack() {
            @Override
            public void onBookingClicked(Booking booking) {
                Intent intent = new Intent(getActivity(), BookingActivity.class);
                intent.putExtra(Constants.Intents.BOOKING_ID, booking.getId());
                startActivity(intent);
            }

            @Override
            public void onCancelRequestedBooking(Booking booking) {
                presenter.cancelRequestedBooking(booking);
            }

            @Override
            public void onCancelPendingBooking(Booking booking) {
                presenter.cancelPendingBooking(booking);
            }

        });

    }

    @Override
    public void showMessage(int message) {
        empty.setText(message);
        recycler.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);
    }

    @Override
    public void toastError(int message) {
        ToastTool.with(getActivity(), message).show();
    }
}
