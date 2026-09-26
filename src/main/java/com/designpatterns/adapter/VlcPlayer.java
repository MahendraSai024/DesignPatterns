package com.designpatterns.adapter;

// Concrete Adaptee — legacy class that only understands VLC
public class VlcPlayer implements AdvancedMediaPlayer {

    @Override
    public void playVlc(String fileName) {
        System.out.println("[VLC]  Playing vlc file: " + fileName);
    }

    @Override
    public void playMp4(String fileName) {
        // VlcPlayer cannot play MP4 — intentionally a no-op
    }
}
