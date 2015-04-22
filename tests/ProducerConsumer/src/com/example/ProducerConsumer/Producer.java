package com.example.ProducerConsumer;
import edu.uiowa.annotations.Annotations;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by ochipara on 4/20/15.
 */
public class Producer implements Runnable {
    private final BlockingQueue<Item> queue;
    Integer count = 0;

    public Producer(BlockingQueue<Item> queue) {
        this.queue = queue;
    }

    public void run() {
       try {
            while (true) {
                Item val = produce();
                Annotations.delaybudget(val, 0);
                queue.put(val);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    protected Item produce() {
        return new Item(count);
    }
}
