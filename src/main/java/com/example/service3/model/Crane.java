package com.example.service3.model;

import java.util.concurrent.Phaser;

public class Crane extends Thread {
    private boolean isWorking = true;
    private Ship item = null;
    private int days;
    private int minutes;
    private boolean startUnload = false;
    private final Phaser phaser;
    private final Ship.TYPES_CARGO type;

    public Crane(Phaser phaser, Ship.TYPES_CARGO type) {
        this.phaser = phaser;
        this.type = type;
        phaser.register();
        this.start();
    }

    public void setItem(Ship item, int day, int minutes) {
        this.item = item;
        this.days = day;
        this.minutes = minutes;
        startUnload = true;
    }

    public void kill() {
        isWorking = false;
    }

    public Ship getItem() {
        return item;
    }

    public boolean isFree() {
        return item == null;
    }

    private int getSpeed() {
        return (type.getValue() + 1);
    }

    @Override
    public void run() {
        while (true) {
            if (isWorking) {
                phaser.arriveAndAwaitAdvance();
            } else {
                phaser.arriveAndDeregister();
                break;
            }

            if (item != null) {
                synchronized (item) {
                    if (item.isEmpty()) {
                        item = null;
                    } else {
                        if (startUnload) {
                            startUnload = false;
                            item.setUploadStart(new Date(days, minutes));
                        }

                        if (item.getQuantity() - getSpeed() <= 0) {
                            item.setQuantity(0);
                        } else {
                            item.setQuantity(item.getQuantity() - getSpeed());
                        }

                        if (item.isEmpty()) {
                            item = null;
                        }
                    }
                }
            }
        }
    }
}
