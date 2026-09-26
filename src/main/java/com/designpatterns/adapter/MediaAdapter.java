package com.designpatterns.adapter;

// Adapter — implements the Target interface, wraps an Adaptee, translates calls
public class MediaAdapter implements MediaPlayer {

    private final AdvancedMediaPlayer advancedPlayer;
    private final String format;

    public MediaAdapter(String format) {
        this.format = format;
        if (format.equals("mp4"))      advancedPlayer = new Mp4Player();
        else if (format.equals("vlc")) advancedPlayer = new VlcPlayer();
        else throw new IllegalArgumentException("No advanced player for format: " + format);
    }

    // Translates the Target's play() into the Adaptee's specific method
    @Override
    public void play(String fileName) {
        if (format.equals("mp4")) advancedPlayer.playMp4(fileName);
        else                      advancedPlayer.playVlc(fileName);
    }
}
