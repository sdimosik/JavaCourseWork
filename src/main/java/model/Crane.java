package model;

import utils.Constants;

import java.util.concurrent.Phaser;

public class Crane extends Thread {
    private boolean isWorking = true;
    private Ship ship = null;
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
        this.ship = item;
        this.days = day;
        this.minutes = minutes;
        startUnload = true;
    }

    public void kill() {
        isWorking = false;
    }

    public Ship getShip() {
        return ship;
    }

    public boolean isFree() {
        return ship == null;
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

            if (ship != null) {
                synchronized (ship) {
                    if (ship.isEmpty()) {
                        ship = null;
                    } else {
                        if (startUnload) {
                            startUnload = false;
                            ship.setUploadStart(new Constants.Date(days, minutes));
                        }

                        if (ship.getQuantity() - getSpeed() <= 0) {
                            ship.setQuantity(0);
                        } else {
                            ship.setQuantity(ship.getQuantity() - getSpeed());
                        }

                        if (ship.isEmpty()) {
                            ship = null;
                        }
                    }
                }
            }
        }
    }
}
