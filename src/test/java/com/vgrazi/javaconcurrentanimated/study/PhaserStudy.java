package com.vgrazi.javaconcurrentanimated.study;

import java.util.concurrent.Phaser;

public class PhaserStudy {
    public void test() {
        Phaser phaser = new Phaser(4) {
            @Override
            // Perform when all parties arrive
            protected boolean onAdvance(int phase, int registeredParties) {
                // return true if the phaser should
                // terminate on advance, else false;
                return false;
            }
        };

        int phase = phaser.arriveAndAwaitAdvance();

        phase = phaser.arrive();

        int in_phase = 1;
        phase = phaser.awaitAdvance(in_phase);

        phase = phaser.arriveAndDeregister();

        phase = phaser.register();

        int parties = 3;
        phase = phaser.bulkRegister(parties);
    }


}
