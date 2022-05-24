package com.example.centernetTest.utils;

import java.util.Timer;
import java.util.TimerTask;

public class STimer extends Timer {
    boolean throwException;
    private boolean running = true;

    public STimer(boolean isDeamon, boolean throwException) {
        super(isDeamon);
        this.throwException = throwException;
    }

    public STimer(boolean isDeamon) {
        this(isDeamon, false);
    }

    public TimerTask schedule(final Runnable r, long delay) {
        if (!running) {
            if (throwException)
                throw new IllegalStateException("Timer already canceled"); //todo lets see if this revoles it!!! check back again pose-scan!
            else return null;
        }
        TimerTask timerTask = new TimerTask() {
            public void run() {
                r.run();
            }
        };
        super.schedule(timerTask, delay);
        return timerTask;
    }


    public void scheduleAtFixedRate(final Runnable r, long delay, long period) {
        if (!running) {
            if (throwException)
                throw new IllegalStateException("Timer already canceled"); //todo lets see if this revoles it!!! check back again pose-scan!
            else return;
        }

        super.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                r.run();
            }
        }, delay, period);
    }


    public TimerTask scheduleInfinite(final Runnable r, long delay) {
        if (!running) {
            if (throwException) throw new IllegalStateException("Timer already canceled");
            else return null;
        }

        TimerTask task = new TimerTask() {
            public void run() {
                r.run();
            }
        };
        super.scheduleAtFixedRate(task, delay, delay);
        return task;
    }

    @Override
    public void cancel() {
        super.cancel();
        running = false;
    }


}
