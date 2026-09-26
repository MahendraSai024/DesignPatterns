package com.designpatterns.adapter;

// Client — uses only the Target interface; unaware of the Adaptee's existence
public class AudioPlayer implements MediaPlayer {

    @Override
    public void play(String fileName) {
        if (fileName.endsWith(".mp3")) {
            System.out.println("[MP3]  Playing natively:  " + fileName);
        } else if (fileName.endsWith(".mp4")) {
            new MediaAdapter("mp4").play(fileName);
        } else if (fileName.endsWith(".vlc")) {
            new MediaAdapter("vlc").play(fileName);
        } else {
            System.out.println("[ERR]  Unsupported format: " + fileName);
        }
    }
}
