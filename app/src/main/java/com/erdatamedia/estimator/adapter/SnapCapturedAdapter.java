package com.erdatamedia.estimator.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.erdatamedia.estimator.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SnapCapturedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> items = new ArrayList<>();
    private Context ctx;
    private String path;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, String obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public SnapCapturedAdapter(Context context, String path, List<String> items) {
        ctx = context;
        this.path = path;
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snap_captured, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        String obj = items.get(position);
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;
            view.namaTv.setText(obj);
            view.coverIv.setImageBitmap(BitmapFactory.decodeFile(path + obj));
            view.lyt_parent.setOnClickListener(view1 -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view1, items.get(position), position);
                }
            });
            view.deleteBtn.setOnClickListener(v -> {
                File file = new File(path + obj);
                boolean deleted = file.delete();
                if (deleted) items.remove(position);
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public String getItem(int position) {
        return items.get(position);
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView deleteBtn;
        public TextView namaTv;
        public ImageView coverIv;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            deleteBtn = v.findViewById(R.id.item_delete);
            namaTv = v.findViewById(R.id.item_nama_tv);
            coverIv = v.findViewById(R.id.item_cover_iv);
            lyt_parent = v.findViewById(R.id.lyt_parent);
        }
    }
}
