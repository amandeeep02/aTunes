package com.example.atunes;


import static android.telephony.TelephonyCallback.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.WebView;


import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlaySong extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        updateSeek.interrupt();

    }

    TextView textView, firstDuration, secondDuration;
    ImageView play, previous, next, pause;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Thread updateSeek;
    boolean stopThread;


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        textView = findViewById(R.id.textView);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        firstDuration = findViewById(R.id.firstDuration);
        secondDuration = findViewById(R.id.secondDuration);


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        textContent = intent.getStringExtra("currentSong");
        textView.setText(textContent);
        textView.setSelected(true);
        position = intent.getIntExtra("position", 0);
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        this.updateSeek = new Thread() {
            public void run() {
                while (!PlaySong.this.stopThread) {
                    try {
                        if (PlaySong.this.mediaPlayer != null) {
                            long currentPosition = (long) PlaySong.this.mediaPlayer.getCurrentPosition();
                            final String format = String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(currentPosition)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPosition)))});
                            PlaySong.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    PlaySong.this.firstDuration.setText(format);
                                }
                            });
                            PlaySong.this.seekBar.setProgress(PlaySong.this.mediaPlayer.getCurrentPosition());
                            sleep(200);
                            Log.d("threadCode", "Updating Success");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("threadCode", "Updating Failed");
                    }
                }
            }
        };

        this.play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (PlaySong.this.mediaPlayer.isPlaying()) {
                    PlaySong.this.play.setImageResource(R.drawable.play);
                    PlaySong.this.mediaPlayer.pause();
                    return;
                }
                PlaySong.this.play.setImageResource(R.drawable.pause);
                PlaySong.this.mediaPlayer.start();
            }
        });
        this.previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PlaySong.this.mediaPlayer.stop();
                PlaySong.this.mediaPlayer.release();
                if (PlaySong.this.position != 0) {
                    PlaySong.this.position--;
                } else {
                    PlaySong playSong = PlaySong.this;
                    playSong.position = playSong.songs.size() - 1;
                }
                Uri parse = Uri.parse(PlaySong.this.songs.get(PlaySong.this.position).toString());
                PlaySong playSong2 = PlaySong.this;
                playSong2.mediaPlayer = MediaPlayer.create(playSong2.getApplicationContext(), parse);
                PlaySong.this.mediaPlayer.start();
                PlaySong.this.play.setImageResource(R.drawable.pause);
                PlaySong.this.seekBar.setMax(PlaySong.this.mediaPlayer.getDuration());
                PlaySong playSong3 = PlaySong.this;
                playSong3.textContent = playSong3.songs.get(PlaySong.this.position).getName().toString();
                PlaySong.this.textView.setText(PlaySong.this.textContent);
                long duration = (long) PlaySong.this.mediaPlayer.getDuration();
                PlaySong.this.secondDuration.setText(String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))}));
            }
        });
        this.next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PlaySong.this.mediaPlayer.stop();
                PlaySong.this.mediaPlayer.release();
                if (PlaySong.this.position != PlaySong.this.songs.size() - 1) {
                    PlaySong.this.position++;
                } else {
                    PlaySong.this.position = 0;
                }
                Uri parse = Uri.parse(PlaySong.this.songs.get(PlaySong.this.position).toString());
                PlaySong playSong = PlaySong.this;
                playSong.mediaPlayer = MediaPlayer.create(playSong.getApplicationContext(), parse);
                PlaySong.this.mediaPlayer.start();
                PlaySong.this.play.setImageResource(R.drawable.pause);
                PlaySong.this.seekBar.setMax(PlaySong.this.mediaPlayer.getDuration());
                PlaySong playSong2 = PlaySong.this;
                playSong2.textContent = playSong2.songs.get(PlaySong.this.position).getName().toString();
                PlaySong.this.textView.setText(PlaySong.this.textContent);
                long duration = (long) PlaySong.this.mediaPlayer.getDuration();
                PlaySong.this.secondDuration.setText(String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))}));
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                PlaySong.this.mediaPlayer.stop();
                PlaySong.this.mediaPlayer.release();
                if (PlaySong.this.position != PlaySong.this.songs.size() - 1) {
                    PlaySong.this.position++;
                } else {
                    PlaySong.this.position = 0;
                }
                Uri parse = Uri.parse(PlaySong.this.songs.get(PlaySong.this.position).toString());
                PlaySong playSong = PlaySong.this;
                playSong.mediaPlayer = MediaPlayer.create(playSong.getApplicationContext(), parse);
                PlaySong.this.mediaPlayer.start();
                PlaySong.this.play.setImageResource(R.drawable.pause);
                PlaySong.this.seekBar.setMax(PlaySong.this.mediaPlayer.getDuration());
                PlaySong playSong2 = PlaySong.this;
                playSong2.textContent = playSong2.songs.get(PlaySong.this.position).getName().toString();
                PlaySong.this.textView.setText(PlaySong.this.textContent);
                long duration = (long) PlaySong.this.mediaPlayer.getDuration();
                PlaySong.this.secondDuration.setText(String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))}));
            }
        });
        long duration = (long) this.mediaPlayer.getDuration();
        this.secondDuration.setText(String.format("%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(duration)), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))}));
        this.updateSeek.start();


    }





}