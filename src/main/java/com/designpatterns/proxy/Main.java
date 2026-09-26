package com.designpatterns.proxy;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Proxy Pattern — Virtual Proxy (Lazy Image Loading) ===\n");

        // Client holds the proxy — RealImage is not yet created
        Image img1 = new ProxyImage("photo_4k.jpg");
        Image img2 = new ProxyImage("wallpaper.png");

        System.out.println("-- First display() call --");
        img1.display();   // triggers load + display

        System.out.println();
        System.out.println("-- Second display() call (same object) --");
        img1.display();   // cache hit — no reload

        System.out.println();
        System.out.println("-- img2: never called display() yet --");
        // img2.display() was never called — RealImage was never created
        System.out.println("[INFO]  wallpaper.png was never loaded (display never called)");

        System.out.println();
        System.out.println("-- Now calling img2.display() --");
        img2.display();
    }
}
