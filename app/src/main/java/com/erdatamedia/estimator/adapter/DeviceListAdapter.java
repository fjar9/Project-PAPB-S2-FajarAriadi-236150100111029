package com.erdatamedia.estimator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.erdatamedia.estimator.model.DeviceInfoModel;
import com.erdatamedia.estimator.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<DeviceInfoModel> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, DeviceInfoModel obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textAddress;
        LinearLayout lyt_parent;

        public ViewHolder(View v) {
            super(v);
            textName = v.findViewById(R.id.textViewDeviceName);
            textAddress = v.findViewById(R.id.textViewDeviceAddress);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }

    public DeviceListAdapter(Context context, List<DeviceInfoModel> items) {
        this.context = context;
        this.items = items;

    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bt, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        DeviceInfoModel deviceInfoModel = items.get(position);
        itemHolder.textName.setText(deviceInfoModel.getDeviceName());
        itemHolder.textAddress.setText(deviceInfoModel.getDeviceHardwareAddress());
        itemHolder.lyt_parent.setOnClickListener(view1 -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view1, items.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        int dataCount = items.size();
        return dataCount;
    }
}
