package com.zhi.videoplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.Editable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.zhi.service.VideoInterface;
import java.io.File;

public class MainActivity extends Activity implements View.OnClickListener{

    private SurfaceHolder surfaceHolder;
    private VideoInterface videoInterface;
    private ServiceConnection conn = new VideoServiceConnection();
    private Intent intent;
    private String filePath;

    private EditText mEtFilename;
    private Button mBtnPlay;
    private CheckBox mCbPause;
    private Button mBtnReplay;
    private Button mBtnStop;
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initEvents();
        intent =new Intent(MainActivity.this, VideoPlayService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);

        mEtFilename.setText("xiaopingguo.mp4");

        surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //  设置surfaceView 不保持缓存区，直接显示
        surfaceHolder.setFixedSize(480, 272); // 设置surfaceView的分辨率
    }

    public class SurfaceCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            videoInterface.play(filePath, surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    public class VideoServiceConnection implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            videoInterface = (VideoInterface) service;
            surfaceHolder.addCallback(new SurfaceCallback());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            videoInterface = null;
        }
    }

    private void initEvents() {
        mBtnPlay.setOnClickListener(this);
        mBtnReplay.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        mCbPause.setOnClickListener(this);
    }

    private void initViews() {
        mEtFilename = (EditText) findViewById(R.id.et_filename);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnReplay = (Button) findViewById(R.id.btn_replay);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mCbPause = (CheckBox) findViewById(R.id.cb_pause);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play:
                Editable editable = mEtFilename.getText();
                if(null == editable || "".equals(editable.toString().trim())){
                    Toast.makeText(MainActivity.this, R.string.str_file_not_exist, Toast.LENGTH_SHORT).show();
                    return;
                }
                String fileName = editable.toString();
                if(fileName.startsWith("http")){  //  文件必须是渐进式http或ftp，或者是实时流媒体.264,.263等
                    //  网络视频播放没有测试，因为电脑老，运行卡顿，不宜装太多软件（QuickTimePlayer  用它来格式转换一下普通mp4文件）
                    filePath = fileName;
                } else {
                    File file = new File(Environment.getExternalStorageDirectory(), fileName);
                    if (file.exists()) {
                        filePath = file.getAbsolutePath();
                    }
                }
                videoInterface.play(filePath, surfaceHolder);
                mCbPause.setChecked(false);
                break;
            case R.id.cb_pause:
                int state = videoInterface.pause();
                if(VideoPlayService.TYPE_STATE_PLAY == state){ // 正在播放
                    mCbPause.setChecked(false);
                } else if(VideoPlayService.TYPE_STATE_PAUSE == state){
                    mCbPause.setChecked(true);
                }
                break;
            case R.id.btn_replay:
                videoInterface.replay();
                mCbPause.setChecked(false);
                break;
            case R.id.btn_stop:
                videoInterface.stop();
                mCbPause.setChecked(false);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != intent){
            unbindService(conn);
            intent = null;
        }
    }
}