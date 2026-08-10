package com.vgrazi.jca.engine;

import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.stereotype.Component;

@Component
public class AnimationEngine {

    Thread thread;
    boolean running = true;
    public void render(ThreadCanvas canvas) {
        if (thread != null) {
            running = false;
            thread.interrupt();
        }
        running = true;
        thread = new Thread(() -> {
            while (running) {
                canvas.repaint(canvas.getBounds());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        });
        thread.setDaemon(true);
        thread.start();
    }
}
