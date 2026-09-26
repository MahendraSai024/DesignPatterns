package com.designpatterns.adapter;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Adapter Pattern — Media Player ===");

        // Client only knows MediaPlayer — same play() call for every format
        MediaPlayer player = new AudioPlayer();

        player.play("song.mp3");    // handled natively by AudioPlayer
        player.play("movie.mp4");   // delegated to MediaAdapter → Mp4Player
        player.play("clip.vlc");    // delegated to MediaAdapter → VlcPlayer
        player.play("video.avi");   // unsupported — no adapter exists
    }
}
