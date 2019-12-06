package com.pocketapps.pockalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by chandrima on 09/04/18.
 */

public class GoogleFitApi implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback {
    private static final String TAG = GoogleFitApi.class.getSimpleName();
    private static GoogleFitApi sGoogleFitApi;
    private GoogleApiClient mGoogleApiClient;
    private int mSteps = 0;
    private static final DataType mDataType = DataType.TYPE_STEP_COUNT_DELTA;

    public interface GoogleFitListening {
        void onConnectionFailure(ConnectionResult connectionResult);
        void onStepCountAvailable(String stepCount);
    }

    private GoogleFitListening mGoogleFitListening;

    private GoogleFitApi(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    // Singleton
    public static GoogleFitApi getInstance(Context context) {
        if (sGoogleFitApi == null) {
            sGoogleFitApi = new GoogleFitApi(context);
        }
        return sGoogleFitApi;
    }

    public void connect(GoogleFitListening googleFitListening) {
        mGoogleFitListening = googleFitListening;
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mSteps = 0;
                mGoogleFitListening = null;
                unsubscribeRecordingApi();
                mGoogleApiClient.disconnect();
            }
        }
    }

    //---------------------------------------------------event handlers--------------------------------------------------------//

    @Override
    public void onResult(@NonNull Result result) {
        if (result instanceof DataReadResult) {
            DataReadResult dataReadResult = (DataReadResult) result;
            if (dataReadResult.getBuckets().size() > 0) {
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            for (Field field : dataPoint.getDataType().getFields()) {
                                mSteps += dataPoint.getValue(field).asInt();
                            }
                        }
                    }
                }
            }
            mGoogleFitListening.onStepCountAvailable(String.valueOf(mSteps));
        }

        if (result.getStatus().isSuccess()) {
            Log.i(TAG, "Subscribed/unsubscribed to Recording Api!");
        } else {
            Log.i(TAG, "Failed to subscribe/unsubscribe to Recording Api.");
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleApiClient connected");
        subscribeRecordingApi();
        readFromHistoryApi();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleFitListening.onConnectionFailure(connectionResult);
        Log.d(TAG, "GoogleApiClient connected fail");
    }

    //----------------------------------------------------private methods-----------------------------------------------------//

    private void subscribeRecordingApi() {
        Fitness.RecordingApi.subscribe(mGoogleApiClient, mDataType).setResultCallback(this);
    }

    private void unsubscribeRecordingApi() {
        Fitness.RecordingApi.unsubscribe(mGoogleApiClient, mDataType).setResultCallback(this);
    }


    private void readFromHistoryApi() {
        Calendar midnightCalendar = Calendar.getInstance();
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
        midnightCalendar.set(Calendar.MINUTE, 0);
        midnightCalendar.set(Calendar.SECOND, 0);

        long midnightTimeInMillis = midnightCalendar.getTimeInMillis();

        Date date = new Date();
        long currentTimeInMillis = date.getTime();

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(mDataType, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.MINUTES)
                .setTimeRange(midnightTimeInMillis, currentTimeInMillis, TimeUnit.MILLISECONDS)
                .build();
        Fitness.HistoryApi.readData(mGoogleApiClient, dataReadRequest).setResultCallback(this);
    }
}
