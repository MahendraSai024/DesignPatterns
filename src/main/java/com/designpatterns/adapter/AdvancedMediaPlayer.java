package com.designpatterns.adapter;

// Adaptee interface — incompatible API that the Adapter will translate
public interface AdvancedMediaPlayer {
    void playMp4(String fileName);
    void playVlc(String fileName);
}
