package com.example.audioplayerandvideostreamingnew;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

/**
 * Audio Player Fragment
 *
 * Fixed UI threading crash and updated to Material 3 Slider.
 */
public class AudioPlayerFragment extends Fragment {

    private FloatingActionButton playBtn, pauseBtn, stopBtn, restartBtn;
    private Slider slider;
    private TextView currentTimeText, totalTimeText, statusText;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupMediaPlayer();
        setupClickListeners();
        setupSliderListener();
    }

    private void initializeViews(View view) {
        playBtn = view.findViewById(R.id.audioPlayBtn);
        pauseBtn = view.findViewById(R.id.audioPauseBtn);
        stopBtn = view.findViewById(R.id.audioStopBtn);
        restartBtn = view.findViewById(R.id.audioRestartBtn);
        slider = view.findViewById(R.id.audioSlider);
        currentTimeText = view.findViewById(R.id.audioCurrentTime);
        totalTimeText = view.findViewById(R.id.audioTotalTime);
        statusText = view.findViewById(R.id.audioStatus);
    }

    private void setupMediaPlayer() {
        try {
            String audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            
            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mediaPlayer.getDuration();
                totalTimeText.setText(formatTime(duration));
                slider.setValueFrom(0f);
                slider.setValueTo(duration > 0 ? (float) duration : 1.0f);
                slider.setValue(0f);
                statusText.setText("Status: Ready (Online)");
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                statusText.setText("Status: Finished");
                slider.setValue(0);
                stopUpdatingSeekBar();
            });

            mediaPlayer.prepareAsync();
            statusText.setText("Status: Loading audio...");

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        playBtn.setOnClickListener(v -> playAudio());
        pauseBtn.setOnClickListener(v -> pauseAudio());
        stopBtn.setOnClickListener(v -> stopAudio());
        restartBtn.setOnClickListener(v -> restartAudio());
    }

    private void setupSliderListener() {
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                // Optionally stop updating while dragging
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo((int) slider.getValue());
                }
            }
        });
        
        slider.addOnChangeListener((slider, value, fromUser) -> {
            currentTimeText.setText(formatTime((int) value));
        });
    }

    private void playAudio() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            statusText.setText("Status: Playing");
            startUpdatingSeekBar();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            statusText.setText("Status: Paused");
            stopUpdatingSeekBar();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            isPlaying = false;
            slider.setValue(0);
            currentTimeText.setText("00:00");
            statusText.setText("Status: Stopped");
            stopUpdatingSeekBar();
        }
    }

    private void restartAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            isPlaying = true;
            statusText.setText("Status: Playing");
            startUpdatingSeekBar();
        }
    }

    private final Runnable updateSeekBarTask = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                try {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    slider.setValue((float) currentPosition);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException e) {
                    // Ignore if player is released
                }
            }
        }
    };

    private void startUpdatingSeekBar() {
        handler.removeCallbacks(updateSeekBarTask);
        handler.post(updateSeekBarTask);
    }

    private void stopUpdatingSeekBar() {
        handler.removeCallbacks(updateSeekBarTask);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / 1000) / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdatingSeekBar();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}