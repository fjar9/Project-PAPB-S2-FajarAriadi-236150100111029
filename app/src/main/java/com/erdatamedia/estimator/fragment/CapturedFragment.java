package com.erdatamedia.estimator.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erdatamedia.estimator.R;
import com.erdatamedia.estimator.adapter.SnapCapturedAdapter;
import com.erdatamedia.estimator.tools.StartSnapHelper;
import com.erdatamedia.estimator.tools.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CapturedFragment extends DialogFragment {

    private RecyclerView recyclerView;

    public CallbackResult callbackResult;
    private Dialog loading;
    private Activity activity;

    private String path = "";
    private List<String> list = new ArrayList<>();

    public CapturedFragment() {
    }

    public CapturedFragment(Activity activity, String path, List<String> list) {
        this.activity = activity;
        this.path = path;
        this.list = list;
    }

    public void setOnCallbackResult(final CallbackResult callbackResult) {
        this.callbackResult = callbackResult;
    }

    private int request_code = 0;
    private View root_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_captured, container, false);
        root_view.findViewById(R.id.bt_close).setOnClickListener(v -> sendDataResult(""));

        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));
        SnapCapturedAdapter adapter = new SnapCapturedAdapter(activity, path, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setOnFlingListener(null);
        new StartSnapHelper().attachToRecyclerView(recyclerView);
        adapter.setOnItemClickListener((view, obj, position) ->
                showDialogFullscreen(obj)
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

    private void showDialogFullscreen(String name) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        String[] meta = name.split("_");
        ChooseFragment newFragment = new ChooseFragment(activity, name, path + name,
                meta[1], meta[2], meta[3]);
        newFragment.setRequestCode(1000);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
        newFragment.setOnCallbackResult((requestCode, msg) -> {
            if (requestCode == 1000) {
                activity.finish();
            }
        });
    }
}
