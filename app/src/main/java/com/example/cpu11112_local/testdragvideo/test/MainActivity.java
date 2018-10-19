package com.example.cpu11112_local.testdragvideo.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.cpu11112_local.testdragvideo.R;
import com.example.cpu11112_local.testdragvideo.dragVideo.DragVideoYoutubeView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainRcv)
    RecyclerView mMainRcv;
    private DragVideoYoutubeView mDragVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMainRcv.setLayoutManager(new GridLayoutManager(this, 2));
        TestDataAdapter adapter = new TestDataAdapter(new TestDataAdapter.OnAdapterInteract() {
            @Override
            public void onClickVideo(int adapterPosition) {
                mDragVideoView.show();
            }
        });
        mMainRcv.setAdapter(adapter);

        mDragVideoView = (DragVideoYoutubeView) findViewById(R.id.dragVideo);
        mDragVideoView.setCallback(new DragVideoYoutubeView.Callback() {
            @Override
            public void onDisappear(int direct) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
