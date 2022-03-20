package com.crypto.moneymachine.util;

import java.util.ArrayDeque;
import java.util.Queue;

public class AvgQueue {
    Queue<Double> queue;
    int maxSize;
    double avg;

    public AvgQueue(int maxSize) {
        this.maxSize = maxSize;
        queue = new ArrayDeque<>();
        avg = 0d;
    }

    public void add(double number) {
        queue.add(number);
        double aux = number / (double)maxSize;
        avg = avg + aux;
        if (queue.size() > maxSize) {
            double aux2 = queue.poll();
            avg = avg - aux2 / maxSize;
        }
    }

    public double getAverage() {
        return avg;
    }
}
