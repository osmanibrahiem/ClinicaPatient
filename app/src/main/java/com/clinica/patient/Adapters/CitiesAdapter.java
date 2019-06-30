package com.clinica.patient.Adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.clinica.patient.Models.City;
import com.clinica.patient.R;
import com.clinica.patient.Tools.CustomTFSpan;
import com.clinica.patient.Tools.Localization;
import com.clinica.patient.Tools.SharedTool.UserData;
import com.clinica.patient.Tools.ToastTool;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CitiesAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<City> dataList;
    private LayoutInflater inflater;
    private OnCitySelected onCitySelected;

    private static int CITY_VIEW = 0;
    private static int LOCATION_VIEW = -1;

    protected GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient fusedLocationClient;


    public CitiesAdapter(Context context) {
        this.context = context;
        this.dataList = new ArrayList<>();
        this.inflater = LayoutInflater.from(context);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

    }

    public void setOnCitySelected(OnCitySelected onCitySelected) {
        this.onCitySelected = onCitySelected;
    }

    public void addData(City city) {
        dataList.add(city);
        sortList();
    }

    public void updateData(City city) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getId() == city.getId()) {
                dataList.set(i, city);
                sortList();
            }
        }
    }

    private void sortList() {
        Collections.sort(dataList, new Comparator<City>() {
            @Override
            public int compare(City ads1, City ads2) {
                if (UserData.getLocalization(context) == Localization.ARABIC_VALUE)
                    return ads1.getTitleAr().compareTo(ads2.getTitleAr());
                else return ads1.getTitleEn().compareTo(ads2.getTitleEn());
            }
        });
        notifyDataSetChanged();
    }

    public void removeData(City city) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getId() == city.getId()) {
                dataList.remove(i);
                sortList();
            }
        }
    }

    public boolean filterData(List<City> fullList, String q) {
        q = q.toLowerCase();
        dataList.clear();
        if (TextUtils.isEmpty(q)) {
            dataList.addAll(fullList);
        } else {
            for (int i = 0; i < fullList.size(); i++) {
                if (fullList.get(i).getTitleAr().toLowerCase().contains(q) ||
                        fullList.get(i).getTitleEn().toLowerCase().contains(q)) {
                    dataList.add(fullList.get(i));
                }
            }
        }
        sortList();
        return dataList.size() > 0;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? LOCATION_VIEW : CITY_VIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == CITY_VIEW) {
            View itemView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, viewGroup, false);
            return new CitiesViewHolder(itemView);
        } else {
            View itemView = inflater.inflate(R.layout.row_current_location, viewGroup, false);
            return new LocationViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position != 0) {
            final City city = dataList.get(position - 1);
            if (UserData.getLocalization(context) == Localization.ARABIC_VALUE) {
                ((CitiesViewHolder) holder).text.setText(city.getTitleAr());
            } else ((CitiesViewHolder) holder).text.setText(city.getTitleEn());
            ((CitiesViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCitySelected != null) {
                        onCitySelected.onSelect(city);
                    }
                }
            });
        } else {
            ((LocationViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dexter.withActivity((Activity) context)
                            .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            .withListener(new MultiplePermissionsListener() {
                                @Override
                                public void onPermissionsChecked(MultiplePermissionsReport report) {
                                    if (report.areAllPermissionsGranted()) {
                                        getLocation();
                                    }

                                    if (report.isAnyPermissionPermanentlyDenied()) {
                                        showSettingsDialog();
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();
                }
            });
        }
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Typeface tf = ResourcesCompat.getFont(context, R.font.typing);
        CustomTFSpan tfSpan = new CustomTFSpan(tf);
        SpannableString spannableString = new SpannableString(context.getString(R.string.dialog_permission_title));
        spannableString.setSpan(tfSpan, 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(spannableString);
        builder.setMessage(context.getString(R.string.dialog_permission_message));
        builder.setPositiveButton(context.getString(R.string.go_to_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton(context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }


    private void getLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastTool.with(context, R.string.location_error_happened).show();
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            getAddress(location);
                        }
                    });
        }
    }

    private void getAddress(Location location) {
        Geocoder geocoderAr = new Geocoder(context, new Locale("ar"));
        Geocoder geocoderEn = new Geocoder(context, new Locale("en"));
        List<Address> addressesAr, addressesEn;
        try {
            addressesAr = geocoderAr.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            addressesEn = geocoderEn.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if ((addressesAr == null || addressesAr.size() == 0) ||
                    (addressesEn == null || addressesEn.size() == 0)) {
                ToastTool.with(context, R.string.location_error_happened).show();
            } else {
                String cityAr = addressesAr.get(0).getSubAdminArea();
                String cityEn = addressesEn.get(0).getSubAdminArea();
                String countryCode = addressesEn.get(0).getCountryCode();
                if (TextUtils.isEmpty(countryCode) || (!countryCode.equals("EG"))) {
                    ToastTool.with(context, R.string.location_error_happened).show();
                } else {
                    if (TextUtils.isEmpty(cityAr) || TextUtils.isEmpty(cityEn)) {
                        ToastTool.with(context, R.string.location_error_happened).show();
                    } else {
                        City myCity = new City();
                        myCity.setTitleAr(cityAr);
                        myCity.setTitleEn(cityEn);
                        myCity.setLat(location.getLatitude());
                        myCity.setLng(location.getLongitude());

                        if (onCitySelected != null)
                            onCitySelected.onSelect(myCity);
                    }
                }
            }
        } catch (IOException e) {
            ToastTool.with(context, R.string.location_error_happened).show();
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size() + 1;
    }

    class CitiesViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        CheckedTextView text;

        CitiesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            text.setPadding(text.getPaddingLeft() + 50, text.getPaddingTop(), text.getPaddingRight() + 50, text.getPaddingBottom());
        }
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnCitySelected {
        void onSelect(City city);
    }
}
