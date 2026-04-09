package com.example.audioplayerandvideostreamingnew;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ViewPager2 Adapter
 *
 * Manages fragments for tabbed navigation
 * - Tab 0: AudioPlayerFragment
 * - Tab 1: VideoPlayerFragment
 */
public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AudioPlayerFragment();
            case 1:
                return new VideoPlayerFragment();
            default:
                return new AudioPlayerFragment();
        }
    }
}
