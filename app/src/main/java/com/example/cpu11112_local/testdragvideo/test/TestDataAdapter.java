package com.example.cpu11112_local.testdragvideo.test;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cpu11112_local.testdragvideo.R;

/**
 * Created by Phong Huynh on 10/17/2018.
 */
public class TestDataAdapter extends RecyclerView.Adapter<TestDataAdapter.ViewHolder> {
    private final OnAdapterInteract mCallback;

    public TestDataAdapter(OnAdapterInteract callback) {
        mCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, parent,
                false));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClickVideo(viewHolder.getAdapterPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 100;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnAdapterInteract {
        void onClickVideo(int adapterPosition);
    }
}
