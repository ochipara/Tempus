package com.example.ProducerConsumer;

import edu.uiowa.annotations.Annotations;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by ochipara on 4/20/15.
 */
public class Consumer implements Runnable {
    private final BlockingQueue<Item> queue;

    public Consumer(BlockingQueue<Item> queue) {
        this.queue = queue;
    }

    public void run() {
            while (true) {
                try {
                    Annotations.waituntil(null, 100);
                    Item val2 = null;

                    val2 = queue.take();
                    consume(val2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

    void consume(Item x) {
        System.out.println(x);
    }
}
