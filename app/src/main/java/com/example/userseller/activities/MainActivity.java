package com.example.userseller.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.VideoView;

import com.example.userseller.R;

public class MainActivity extends AppCompatActivity {
    VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        videoView=(VideoView)findViewById(R.id.videoView);
//        String videoPath=new StringBuilder("android.resource://")
//                .append(getPackageName())
//                .append("/raw/logo")
//                .toString();
//        videoView.setVideoPath(videoPath);
//        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        startActivity(new Intent(MainActivity.this,Login.class));
//                        finish();
//                    }
//                },1000);
//            }
//        });
//        videoView.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i=new Intent(MainActivity.this, Login.class);
                startActivity(i);
            }
        },6000);
    }
}