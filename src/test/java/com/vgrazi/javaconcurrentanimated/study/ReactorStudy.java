package com.vgrazi.javaconcurrentanimated.study;

import rx.Observable;
import rx.Scheduler;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class ReactorStudy {
    public static void main(String[] args) {

        System.out.println("Main thread:" + Thread.currentThread());
        Scheduler scheduler = Schedulers.trampoline();
//        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(2));
//        Scheduler scheduler = Schedulers.immediate();
        try {

            for(int i = 0; i < 10; i++) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        Scheduler scheduler = Schedulers.from(command -> new Thread(() -> {
//                    System.out.println(Thread.currentThread());
//                    command.run();
//                }).start());

        Observable<String> test = Observable.from(new String[]{"this", "is", "a", "test", "one", "two", "three"})
                .subscribeOn(scheduler);
        Observable<String> gettys = Observable.from(new String[]{"four", "score", "and", "seven", "years", "ago"})
                .subscribeOn(scheduler);
//        if (true)
        {
            System.out.println("1111111111111publish");
            ConnectableObservable<String> testPublish = test.publish();
            ConnectableObservable<String> gettysPublish = gettys.publish();
            testPublish.subscribe(x1 -> System.out.println(x1 + " ... " + Thread.currentThread()));
            gettysPublish.subscribe(x1 -> System.out.println(x1 + " ... " + Thread.currentThread()));
            testPublish.zipWith(gettysPublish,
                    (x, y) -> x + "-" + y)
                    .subscribe(x1 -> {
                        System.out.println(x1 + "..." + Thread.currentThread());
                    });
            testPublish.connect();
            gettysPublish.connect();

        }
//        else
        {
            System.out.println("22222222222222222 non publish");
            test.subscribe(x1 -> System.out.println(x1 + " ... " + Thread.currentThread()));
            gettys.subscribe(x1 -> System.out.println(x1 + " ... " + Thread.currentThread()));
            test.zipWith(gettys,
                    (x, y) -> x + "-" + y)
                    .subscribe(x1 -> {
                        System.out.println(x1 + "..." + Thread.currentThread());
                    });
        }
    }
}
