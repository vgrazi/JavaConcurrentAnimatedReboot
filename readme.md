This is a revised and much simplified version of Java Concurrent Animated

In the original version too much control was taken by the canvas, and all of the interactions were dependent on the slide program.

In this reboot version, there is a single runnable per slide that carries all of the threads through the desired states. The renderer iterates through all of the threads, gets their state, current position, and shape, and renders them.
