package com.vgrazi.jca.engine;

import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.stereotype.Component;

@Component
public class AnimationEngine {

    public void render(ThreadCanvas canvas) {
        Thread thread = new Thread(() -> {
            while (true) {
                canvas.repaint(canvas.getBounds());
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        });
        thread.start();
    }
}
