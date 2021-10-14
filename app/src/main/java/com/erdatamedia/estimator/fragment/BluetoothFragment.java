package com.erdatamedia.estimator.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erdatamedia.estimator.model.DeviceInfoModel;
import com.erdatamedia.estimator.adapter.DeviceListAdapter;
import com.erdatamedia.estimator.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothFragment extends DialogFragment {

    private RecyclerView recyclerView;

    public CallbackResult callbackResult;
    private Activity activity;

    private List<DeviceInfoModel> deviceList = new ArrayList<>();

    public BluetoothFragment() {
    }

    public BluetoothFragment(Activity activity, List<DeviceInfoModel> deviceList) {
        this.activity = activity;
        this.deviceList = deviceList;
    }

    public void setOnCallbackResult(final CallbackResult callbackResult) {
        this.callbackResult = callbackResult;
    }

    private int request_code = 0;
    private View root_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        root_view.findViewById(R.id.bt_close).setOnClickListener(v -> sendDataResult(""));

        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        DeviceListAdapter adapter = new DeviceListAdapter(activity, deviceList);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setOnItemClickListener((view, obj, position) ->
                sendDataResult(obj.getDeviceHardwareAddress())
        );


        return root_view;
    }

    public void setRequestCode(int request_code) {
        this.request_code = request_code;
    }

    private void sendDataResult(String msg) {
        activity.onBackPressed();
        if (callbackResult != null) {
            callbackResult.sendResult(request_code, msg);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public interface CallbackResult {
        void sendResult(int requestCode, String msg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
