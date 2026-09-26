package com.designpatterns.adapter;

// Concrete Adaptee — legacy class that only understands MP4
public class Mp4Player implements AdvancedMediaPlayer {

    @Override
    public void playMp4(String fileName) {
        System.out.println("[MP4]  Playing mp4 file: " + fileName);
    }

    @Override
    public void playVlc(String fileName) {
        // Mp4Player cannot play VLC — intentionally a no-op
    }
}
