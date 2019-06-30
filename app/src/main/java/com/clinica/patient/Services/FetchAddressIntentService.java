package com.clinica.patient.Services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.clinica.patient.R;
import com.clinica.patient.Tools.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Asynchronously handles an intent using a worker thread. Receives a ResultReceiver object and a
 * location through an intent. Tries to fetch the address for the location using a Geocoder, and
 * sends the result to the ResultReceiver.
 */

public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddressIS";

    /**
     * The receiver where results are forwarded from this service.
     */
    private ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link android.os.ResultReceiver} in * MainActivity to process content
     * sent from this service.
     * <p>
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            Log.wtf(TAG, "No intent received. There is nowhere to send the results.");
            return;
        }
        String errorMessage = "";
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverErrorToReceiver(errorMessage);
            return;
        }

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoderAr = new Geocoder(this, new Locale("ar"));
        Geocoder geocoderEn = new Geocoder(this, new Locale("en"));
        List<Address> addressesAr = null, addressesEn = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addressesAr = geocoderAr.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            addressesEn = geocoderEn.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if ((addressesAr == null || addressesAr.size() == 0) ||
                (addressesEn == null || addressesEn.size() == 0)) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.location_error_happened);
                Log.e(TAG, errorMessage);
            }
            deliverErrorToReceiver(errorMessage);
        } else {
            String streetNameAr = addressesAr.get(0).getThoroughfare();
            String streetNameEn = addressesEn.get(0).getThoroughfare();
            String stateAr = addressesAr.get(0).getLocality();
            String stateEn = addressesEn.get(0).getLocality();
            String cityAr = addressesAr.get(0).getSubAdminArea();
            String cityEn = addressesEn.get(0).getSubAdminArea();
            String governorateAr = addressesAr.get(0).getAdminArea();
            String governorateEn = addressesEn.get(0).getAdminArea();
            String countryCode = addressesEn.get(0).getCountryCode();

            if (TextUtils.isEmpty(countryCode) || (!countryCode.equals("EG"))) {
                errorMessage = getString(R.string.location_error_happened);
                Log.e(TAG, errorMessage);
                deliverErrorToReceiver(errorMessage);
            } else {
                if (TextUtils.isEmpty(cityAr) || TextUtils.isEmpty(cityEn)) {
                    errorMessage = getString(R.string.location_error_happened);
                    Log.e(TAG, errorMessage);
                    deliverErrorToReceiver(errorMessage);
                } else {
                    com.clinica.patient.Models.Doctor.Address mAddressAr = new com.clinica.patient.Models.Doctor.Address();
                    com.clinica.patient.Models.Doctor.Address mAddressEn = new com.clinica.patient.Models.Doctor.Address();
                    mAddressAr.setStreetName(streetNameAr);
                    mAddressEn.setStreetName(streetNameEn);
                    mAddressAr.setState(stateAr);
                    mAddressEn.setState(stateEn);
                    mAddressAr.setCity(cityAr);
                    mAddressEn.setCity(cityEn);
                    mAddressAr.setGovernorate(governorateAr);
                    mAddressEn.setGovernorate(governorateEn);

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.Intents.ADDRESS_AR, mAddressAr);
                    bundle.putParcelable(Constants.Intents.ADDRESS_EN, mAddressEn);
                    mReceiver.send(Constants.SUCCESS_RESULT, bundle);
                }
            }
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverErrorToReceiver(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(Constants.FAILURE_RESULT, bundle);
    }

}
