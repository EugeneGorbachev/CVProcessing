package ru.vsu.cvprocessing.observer;

public interface Observer {
    void update(boolean isDetected, int x, int y);
}
