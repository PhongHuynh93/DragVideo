package com.example.cpu11112_local.testdragvideo.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.cpu11112_local.testdragvideo.DragVideoView;
import com.example.cpu11112_local.testdragvideo.R;

public class MainActivity extends AppCompatActivity {
    private DragVideoView mDragVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.mainRcv);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new TestDataAdapter(new TestDataAdapter.OnAdapterInteract() {
            @Override
            public void onClickVideo(int adapterPosition) {
                mDragVideoView.show();
            }
        }));

        mDragVideoView = (DragVideoView) findViewById(R.id.dragVideo);
        mDragVideoView.setCallback(new DragVideoView.Callback() {
            @Override
            public void onDisappear(int direct) {

            }
        });
    }
}
