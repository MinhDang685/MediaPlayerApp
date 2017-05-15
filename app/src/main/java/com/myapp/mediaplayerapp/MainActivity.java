package com.myapp.mediaplayerapp;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private AudioVisualization audioVisualization;
    private MediaPlayer mediaPlayer;
    private VisualizerDbmHandler musicHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioVisualization = (AudioVisualization) findViewById(R.id.visualizer_view);
        //request permission to access external storage
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }

        //request permission to recording
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    2);
        }

        //media file path
        String filePath = "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg";
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        musicHandler = VisualizerDbmHandler.Factory.newVisualizerHandler(this, mediaPlayer);
        audioVisualization.linkTo(musicHandler);

        requestAudioFocusForMyApp(MainActivity.this);
        //mediaPlayer.start();
    }

    private final static String TAG = "AudioFocus";
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Log.i(TAG, "AUDIOFOCUS_GAIN");
                    //Play or restart your sound
                    mediaPlayer.start();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Log.e(TAG, "AUDIOFOCUS_LOSS");
                    //Loss of audio focus for a long time
                    //Mostly stop playing the sound
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT");
                    //Loss of audio focus for a short time
                    //Mostly pause playing the sound
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Log.e(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                    //Loss of audio focus for a short time.
                    //But one can duck .Mostly lower the volume of playing the sound
                    break;

                default:
                    //
            }
        }
    };

    private boolean requestAudioFocusForMyApp(final Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "Audio focus received");
            return true;
        } else {
            Log.d(TAG, "Audio focus NOT received");
            return false;
        }
    }

    void releaseAudioFocusForMyApp(final Context context) {
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(mOnAudioFocusChangeListener);
    }
}
