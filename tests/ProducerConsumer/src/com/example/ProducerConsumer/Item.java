package com.example.ProducerConsumer;
/**
 * Created by ochipara on 4/20/15.
 */
public class Item {
    private final Integer count;
    Object data;

    public Item(Integer count) {
        this.data = new Object();
        this.count = count;
    }
}
