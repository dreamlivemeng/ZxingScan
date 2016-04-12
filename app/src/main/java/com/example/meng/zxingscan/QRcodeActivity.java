package com.example.meng.zxingscan;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meng.zxingscan.qrcode.CameraManager;
import com.example.meng.zxingscan.qrcode.CaptureActivityHandler;
import com.example.meng.zxingscan.qrcode.InactivityTimer;
import com.example.meng.zxingscan.widget.ErcodeScanView;
/*import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;*/
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;


import java.io.IOException;
import java.util.Vector;

/**
 * 一定要给权限才行。
 */
public class QRcodeActivity extends AppCompatActivity implements Callback {
    private Context mContext;
    // 这个类是解码与avtivity中介。解码成功，失败都用回调
    private CaptureActivityHandler handler;
    // 我们看到的扫描框,根据需要自己修改
    private ErcodeScanView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private SurfaceView surfaceView;
    private ImageView mBack;
    private View mDialogView;
    private Button mCancle;
    private Button mSure;
    private TextView mUrl;
    private Dialog mDialog;

    private String resultString = "";
    private int screenWidth;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
/*    private GoogleApiClient client;*/

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_news_code_scan);
        mContext = this;
        // 摄像头管理类。打开，关闭
        CameraManager.init(getApplication());
        initControl();

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
/*        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();*/
    }

    private void initControl() {
        // 扫描窗口
        viewfinderView = (ErcodeScanView) findViewById(R.id.viewfinder_view);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        mBack = (ImageView) findViewById(R.id.back);
        // 返回按钮
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 获取屏幕宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;

    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    /**
     * 处理扫描结果
     *
     * @param result  扫描到的结果
     * @param barcode 二维码的bitmap
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
//        playBeepSoundAndVibrate();

        resultString = result.getText();
        Toast.makeText(QRcodeActivity.this, "扫描到的结果是：" + resultString, Toast.LENGTH_LONG).show();
        if (resultString.equals("")) {
            Toast.makeText(QRcodeActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent(QRcodeActivity.this, ResultScanActivity.class);
            resultIntent.putExtra("result", "" + resultString);
            startActivity(resultIntent);
        }
    }

    /**
     * 开始扫描
     */
    private void start() {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        // initBeepSound();
        vibrate = true;
    }

    /**
     * 停止扫描
     */
    private void stop() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public ErcodeScanView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    // /**
    // * 扫描正确后的震动声音,如果感觉apk大了,可以删除
    // */
    // private void initBeepSound() {
    // if (playBeep && mediaPlayer == null) {
    // setVolumeControlStream(AudioManager.STREAM_MUSIC);
    // mediaPlayer = new MediaPlayer();
    // mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    // mediaPlayer.setOnCompletionListener(beepListener);
    //
    // AssetFileDescriptor file = getResources().openRawResourceFd(
    // R.raw.beep);
    // try {
    // mediaPlayer.setDataSource(file.getFileDescriptor(),
    // file.getStartOffset(), file.getLength());
    // file.close();
    // mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
    // mediaPlayer.prepare();
    // } catch (IOException e) {
    // mediaPlayer = null;
    // }
    // }
    // }

    private static final long VIBRATE_DURATION = 200L;

    /**
     * 振动
     */
    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "QRcode Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.example.meng.zxingscan/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "QRcode Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.example.meng.zxingscan/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
    }
}
