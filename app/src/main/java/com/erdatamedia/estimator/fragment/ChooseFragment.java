package com.erdatamedia.estimator.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erdatamedia.estimator.R;
import com.erdatamedia.estimator.adapter.SnapCapturedAdapter;
import com.erdatamedia.estimator.tools.StartSnapHelper;
import com.erdatamedia.estimator.tools.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseFragment extends DialogFragment {

    private ImageView imageView;

    public CallbackResult callbackResult;
    private Dialog loading;
    private Activity activity;

    private String name = "";
    private String path = "";
    private String no = "";
    private String distance = "";
    private String result = "";

    public ChooseFragment() {
    }

    public ChooseFragment(Activity activity, String name, String path, String no, String distance, String result) {
        this.activity = activity;
        this.name = name;
        this.path = path;
        this.no = no;
        this.distance = distance;
        this.result = result;
    }

    public void setOnCallbackResult(final CallbackResult callbackResult) {
        this.callbackResult = callbackResult;
    }

    private int request_code = 0;
    private View root_view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_choose, container, false);

        imageView = root_view.findViewById(R.id.preview);
        root_view.findViewById(R.id.close).setOnClickListener(v -> {
            activity.onBackPressed();
            dismiss();
        });
        Button choseBtn = root_view.findViewById(R.id.chose);
        result = result.replace(".jpg", "");
        result = String.format(Locale.getDefault(), "%.02f", Float.parseFloat(result));
        String s = "Pilih gambar ke " + no + " dgn jarak " + distance + "cm dan hasilnya " + result;
        choseBtn.setText(s);
        choseBtn.setOnClickListener(v -> {
            File file = new File(activity.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES + "/" + "CHOSEN"), name);

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            Tools.createFile(file, drawable.getBitmap());
            sendDataResult("");
        });

        imageView.setImageBitmap(BitmapFactory.decodeFile(path));

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
