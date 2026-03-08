package com.flower.fxutils;

public interface ProgressCallback {
    void updateProgress(String message, double progress, boolean isDone);
}
