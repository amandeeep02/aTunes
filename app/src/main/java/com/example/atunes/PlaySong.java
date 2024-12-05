package com.example.atunes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlaySong extends AppCompatActivity {

    TextView textView, firstDuration, secondDuration;
    ImageView play, previous, next;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    SeekBar seekBar;
    Thread updateSeek;
    boolean stopThread = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopThread = true; // Stop the thread when activity is destroyed
        if (updateSeek != null) {
            updateSeek.interrupt();
        }
    }

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
        position = intent.getIntExtra("position", 0);

        textView.setText(textContent);
        textView.setSelected(true);

        playSong();

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Play/pause button
        play.setOnClickListener(view -> {
            if (mediaPlayer.isPlaying()) {
                play.setImageResource(R.drawable.play);
                mediaPlayer.pause();
            } else {
                play.setImageResource(R.drawable.pause);
                mediaPlayer.start();
            }
        });

        // Previous button
        previous.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            position = (position - 1 + songs.size()) % songs.size();
            playSong();
        });

        // Next button
        next.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            position = (position + 1) % songs.size();
            playSong();
        });
    }

    private void playSong() {
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();

        // Update UI
        play.setImageResource(R.drawable.pause);
        textContent = songs.get(position).getName();
        textView.setText(textContent);

        // Set seek bar max and duration
        seekBar.setMax(mediaPlayer.getDuration());
        long duration = mediaPlayer.getDuration();
        secondDuration.setText(String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        ));

        // Handle seek bar updates
        startSeekBarUpdateThread();

        // On completion, move to the next song
        mediaPlayer.setOnCompletionListener(mp -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            position = (position + 1) % songs.size();
            playSong();
        });
    }

    private void startSeekBarUpdateThread() {
        updateSeek = new Thread(() -> {
            while (!stopThread) {
                try {
                    if (mediaPlayer != null) {
                        synchronized (mediaPlayer) {
                            if (mediaPlayer.isPlaying()) {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentPosition);
                                runOnUiThread(() -> {
                                    firstDuration.setText(formatDuration(currentPosition));
                                });
                            }
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (IllegalStateException e) {
                    Log.e("PlaySong", "MediaPlayer is in an illegal state", e);
                } catch (InterruptedException e) {
                    Log.e("PlaySong", "SeekBar update thread interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }
        });
        updateSeek.start();
    }

    private String formatDuration(int duration) {
        int minutes = (duration / 1000) / 60;
        int seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
