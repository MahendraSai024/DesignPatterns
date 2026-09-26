package com.designpatterns.proxy;

// Proxy — same interface as RealImage; defers creation until display() is first called
public class ProxyImage implements Image {
    private final String fileName;
    private RealImage realImage;   // null until first access

    public ProxyImage(String fileName) {
        this.fileName = fileName;
        // RealImage is NOT created here — that's the point
    }

    @Override
    public void display() {
        if (realImage == null) {
            System.out.println("[PROXY] First access — creating RealImage for: " + fileName);
            realImage = new RealImage(fileName);   // lazy initialisation
        } else {
            System.out.println("[PROXY] Cache hit — reusing RealImage for: " + fileName);
        }
        realImage.display();
    }
}
