package com.example.audioplayerandvideostreamingnew;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Video Player Fragment
 *
 * Updated with Material 3 components and better loading state handling.
 */
public class VideoPlayerFragment extends Fragment {

    private TextInputEditText videoUrlInput;
    private MaterialButton videoPlayBtn;
    private VideoView videoView;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupVideoView();
        setupClickListener();
    }

    private void initializeViews(View view) {
        videoUrlInput = view.findViewById(R.id.videoUrlInput);
        videoPlayBtn = view.findViewById(R.id.videoPlayBtn);
        videoView = view.findViewById(R.id.videoView);
        progressBar = view.findViewById(R.id.videoProgressBar);
    }

    private void setupClickListener() {
        videoPlayBtn.setOnClickListener(v -> playVideo());
    }

    private void setupVideoView() {
        try {
            MediaController mediaController = new MediaController(requireContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                videoView.start();
                Toast.makeText(requireContext(), "Video Started", Toast.LENGTH_SHORT).show();
            });

            videoView.setOnErrorListener((mp, what, extra) -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: Video format not supported or link broken", 
                        Toast.LENGTH_LONG).show();
                return true;
            });

            videoView.setOnCompletionListener(mp -> {
                Toast.makeText(requireContext(), "Playback Completed", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Setup Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playVideo() {
        String url = videoUrlInput.getText().toString().trim();

        if (url.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a video URL", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(requireContext(), "URL must start with http:// or https://",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            progressBar.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(url);
            videoView.setVideoURI(videoUri);
            // videoView.start() is called in onPreparedListener
            
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}