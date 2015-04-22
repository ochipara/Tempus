package com.example.ProducerConsumer;

import android.app.Activity;
import android.os.Bundle;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MyActivity extends Activity {
    protected final BlockingDeque<Item> blockingQueue = new LinkedBlockingDeque<>();
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Producer producer = new Producer(blockingQueue);
        Consumer consumer = new Consumer(blockingQueue);
        new Thread(producer).start();
        new Thread(consumer).start();
    }
}
