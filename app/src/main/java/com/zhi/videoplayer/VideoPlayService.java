package com.zhi.videoplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.view.SurfaceHolder;

import com.zhi.service.VideoInterface;
import java.io.IOException;

/**
 * Created by Administrator on 2016/11/3.
 */
public class VideoPlayService extends Service {

    public static final int TYPE_STATE_PLAY = 0x1;  // 继续
    public static final int TYPE_STATE_PAUSE = 0x2;  // 暂停

    private IBinder binder = new VideoPlayBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPause;
    private int position;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class VideoPlayBinder extends Binder implements VideoInterface {
        private String path;
        private SurfaceHolder surfaceHolder;

        @Override
        public void play(String path, SurfaceHolder surfaceHolder) {
            this.path = path;
            this.surfaceHolder = surfaceHolder;
            mediaPlayer.reset();
            videoPlay(0);
        }

        @Override
        public int pause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                position = mediaPlayer.getCurrentPosition();
                isPause = true;   // 暂停
                return TYPE_STATE_PAUSE;
            }
            if(isPause) {  // 当暂停键被按过之后
                if(position>0 && path !=null){
                    mediaPlayer.start();
                    isPause = false;
                }
                return TYPE_STATE_PLAY;
            }
            return -1;
        }

        @Override
        public void replay() {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
            } else {
                if(null != path){
                    path = null;
                }
            }
        }

        @Override
        public void stop() {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
        }

        private void videoPlay(int position) {
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.setDisplay(surfaceHolder);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MusicPreparedListener(position));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public class MusicPreparedListener implements MediaPlayer.OnPreparedListener {
            private int position;

            public MusicPreparedListener(int position) {
                this.position = position;
            }

            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                mediaPlayer.seekTo(position);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}