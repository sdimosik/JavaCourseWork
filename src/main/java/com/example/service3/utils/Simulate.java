package com.example.service3.utils;

import com.example.service3.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Phaser;

import static com.example.service3.utils.Utils.generateInt;

public class Simulate {
    public enum TYPES_EXIT {
        OUT_OF_TIME(-2),
        GOOD(-1),
        ADD_CRANE_LOOSE(0),
        ADD_CRANE_LIQUID(1),
        ADD_CRANE_CONTAINER(2);

        private final int value;

        private TYPES_EXIT(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum TYPES_RETURN {
        ERROR(0),
        GOOD(1);

        private final int value;

        private TYPES_RETURN(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Schedule scheduleStock;
    private final List<Ship> report = new LinkedList<>();
    private final int CRANE_COAST = 30000;

    private final ArrayList<Integer> tax = new ArrayList<>(3);
    private double sumLengthQueue = 0;
    private int countSumLengthQueue = 0;
    private int maxLengthQueue = 0;
    private ArrayList<Integer> lenQueueOutOfTime = new ArrayList<>(3);

    private final ArrayList<List<Crane>> pool = new ArrayList<>(3);
    private final ArrayList<Queue<Ship>> queue = new ArrayList<>(3);
    private final ArrayList<LinkedList<Ship>> shipWorking = new ArrayList<>(3);
    private Phaser phaser;

    public Report run(Schedule schedule) {
        scheduleStock = new Schedule(schedule.getList());
        generateAndSetData();
        scheduleStock.sortByArrivalTime(true);
        return simulator();
    }

    private void fillField() {
        for (int i = 0; i < 3; i++) {
            pool.add(new LinkedList<>());
            queue.add(new LinkedList<>());
            shipWorking.add(new LinkedList<>());
            tax.add(0);
            lenQueueOutOfTime.add(0);
        }
    }

    private Report simulator() {
        fillField();
        ArrayList<Integer> countCrane = new ArrayList<>(3);
        for (int type = 0; type < 3; type++) {
            countCrane.add(1);
        }

        while (true) {
            fillSetting(countCrane);
            Schedule schedule = new Schedule(scheduleStock.getList());
            TYPES_EXIT result = calculate(schedule);
            System.out.println(result);

            switch (result) {
                case OUT_OF_TIME:
                    int type = getNeededType().getValue();
                    countCrane.set(type, countCrane.get(type) + 1);
                case GOOD:
                    return calculateAndGetReport(countCrane);
                case ADD_CRANE_LIQUID:
                case ADD_CRANE_LOOSE:
                case ADD_CRANE_CONTAINER:
                    countCrane.set(result.getValue(), countCrane.get(result.getValue()) + 1);
            }
        }
    }

    private Ship.TYPES_CARGO getNeededType() {
        int type = 0;
        int max = Integer.MIN_VALUE;
        for (int typeI = 0; typeI < 3; typeI++) {
            if (max < lenQueueOutOfTime.get(typeI)) {
                max = lenQueueOutOfTime.get(typeI);
                type = typeI;
            }
        }

        Ship.TYPES_CARGO res;
        if (type == 0) {
            res = Ship.TYPES_CARGO.LOOSE;
        } else if (type == 1) {
            res = Ship.TYPES_CARGO.LIQUID;
        } else {
            res = Ship.TYPES_CARGO.CONTAINER;
        }
        return res;
    }

    Report calculateAndGetReport(ArrayList<Integer> countCrane) {
        Report r = new Report();

        int sumDelayQueue = report.get(0).getTax().getMinutes();
        int maxDelay = report.get(0).getTax().getMinutes();
        for (Ship ship : report) {
            Report.Item item = new Report.Item(
                ship.getName(),
                new DateDetail(ship.getArrivalTime()),
                new DateDetail(ship.getTax().getMinutes()),
                ship.getUploadStart(),
                ship.getUploadTime()
            );
            r.getShips().add(item);

            sumDelayQueue += ship.getTax().getMinutes();
            if (ship.getTax().getMinutes() > maxDelay) {
                maxDelay = ship.getTax().getMinutes();
            }
        }

        r.setCountUnload(report.size());
        r.setSumTax(tax.get(0) + tax.get(1) + tax.get(2));
        r.setLengthUnloadQueueAvg(sumLengthQueue / countSumLengthQueue);
        r.setAvgTimeUnloadQueue((double) (sumDelayQueue / report.size()));
        r.setAvgDelayUnload(sumLengthQueue / countSumLengthQueue);
        r.setMaxDelayUnload(maxLengthQueue);
        r.setCountCraneLOOSE(countCrane.get(0));
        r.setCountCraneLIQUID(countCrane.get(1));
        r.setCountCraneCONTAINER(countCrane.get(2));

        return r;
    }

    TYPES_EXIT calculate(Schedule schedule) {
        for (int daysLeft = -8; daysLeft < 40; daysLeft++) {
            for (int minutesLeft = 0; minutesLeft < 1440; minutesLeft++) {
                if (calculateTax(Ship.TYPES_CARGO.LOOSE)) {
                    System.out.print("DAY:" + daysLeft + "  MIN:" + minutesLeft + "  ");
                    clear(true);
                    return TYPES_EXIT.ADD_CRANE_LOOSE;
                } else if (calculateTax(Ship.TYPES_CARGO.LIQUID)) {
                    System.out.print("DAY:" + daysLeft + "  MIN:" + minutesLeft + "  ");
                    clear(true);
                    return TYPES_EXIT.ADD_CRANE_LIQUID;
                } else if (calculateTax(Ship.TYPES_CARGO.CONTAINER)) {
                    System.out.print("DAY:" + daysLeft + "  MIN:" + minutesLeft + "  ");
                    clear(true);
                    return TYPES_EXIT.ADD_CRANE_CONTAINER;
                }


                if (zeroingShip(schedule, daysLeft, minutesLeft)) {
                    System.out.print("DAY:" + daysLeft + "  MIN:" + minutesLeft + "  ");
                    clear(false);
                    return TYPES_EXIT.GOOD;
                }

                addShipQueue(schedule, daysLeft, minutesLeft);
                clearNullQueueWorking();
                distributionShip(daysLeft, minutesLeft);
                calculateSomeStatistic();

                phaser.arriveAndAwaitAdvance();
            }
        }
        clear(true);
        return TYPES_EXIT.OUT_OF_TIME;
    }

    private void calculateSomeStatistic() {
        for (int type = 0; type < 3; type++) {
            if (queue.get(type).size() > maxLengthQueue) {
                maxLengthQueue = queue.get(type).size();
            }
            lenQueueOutOfTime.set(type, lenQueueOutOfTime.get(type) + queue.get(type).size());
            sumLengthQueue += queue.size();
            countSumLengthQueue++;
        }
    }

    private void distributionShip(int daysLeft, int minutesLeft) {
        for (int type = 0; type < 3; type++) {
            for (Crane crane : pool.get(type)) {
                if (crane.isFree()) {
                    if (!shipWorking.get(type).isEmpty()) {
                        synchronized (shipWorking.get(type).peek()) {
                            Ship ship = shipWorking.get(type).getLast();
                            shipWorking.get(type).remove(shipWorking.get(type).size() - 1);
                            crane.setItem(ship, daysLeft, minutesLeft);
                        }
                    } else if (queue.get(type).peek() != null) {
                        Ship ship = queue.get(type).poll();
                        shipWorking.get(type).offerFirst(ship);
                        crane.setItem(ship, daysLeft, minutesLeft);
                    }
                }
            }
        }
    }

    private void clearNullQueueWorking() {
        for (int type = 0; type < 3; type++) {
            for (int i = shipWorking.get(type).size() - 1; i >= 0; i--) {
                if (shipWorking.get(type).get(i) == null) {
                    shipWorking.get(type).remove(i);
                }
            }
        }
    }

    private void clear(boolean isAllClear) {
        for (int type = 0; type < 3; type++) {
            for (int i = 0; i < pool.get(type).size(); i++) {
                synchronized (pool.get(type).get(i)) {
                    pool.get(type).get(i).kill();
                }
            }
        }
        phaser.arriveAndAwaitAdvance();


        for (int type = 0; type < 3; type++) {
            pool.get(type).clear();
            queue.get(type).clear();
            shipWorking.get(type).clear();

            if (isAllClear) {
                tax.set(type, 0);
                countSumLengthQueue = 0;
                sumLengthQueue = 0;
                maxLengthQueue = 0;
                lenQueueOutOfTime.set(type, 0);
                report.clear();
            }
        }
    }

    private boolean calculateTax(Ship.TYPES_CARGO type) {
        final int pos = type.getValue();
        for (Ship ship : queue.get(pos)) {
            if (ship.checkAndCalculateTax()) {
                tax.set(pos, tax.get(pos) + 100);
            }
        }
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            sum += tax.get(i);
        }
        return tax.get(pos) >= CRANE_COAST || sum >= CRANE_COAST;
    }

    private boolean zeroingShip(Schedule schedule, int daysLeft, int minutesLeft) {
        for (int i = 0; i < schedule.getList().size(); i++) {
            synchronized (schedule.getList().get(i)) {
                if (schedule.getList().get(i) != null) {
                    if (schedule.getList().get(i).isEmpty()) {
                        if (schedule.getList().get(i).getQuantity() < 0) {
                            int a = 3;
                        }
                        Date startTime = schedule.getList().get(i).getUploadStart();
                        int d = Math.abs(daysLeft - startTime.getDays());
                        int m = minutesLeft - startTime.getMinutes();

                        if (d > 0) {
                            m += 1440;
                        }

                        schedule.getList().get(i).setUploadTime(new Date(d, m));
                        report.add(schedule.getList().get(i));
                        schedule.getList().remove(i);
                    }
                }
            }
        }
        return schedule.getList().isEmpty();
    }

    private void generateAndSetData() {
        for (Ship item : scheduleStock.getList()) {
            int type = item.getType().getValue();
            int weight = item.getQuantity();
            int k = generateInt(0, 9);
            int unloadDelayMinutes = k * type * weight; // max 1440
            int arrivalErrorDays = generateInt(-7, 7);

            item.setArrivalTime(new Date(item.getArrivalTime().getDays()
                - arrivalErrorDays, item.getArrivalTime().getMinutes()));

            int minutesToUnload = unloadDelayMinutes + item.getParkingTime();
            item.setParkingTime(minutesToUnload);
        }
    }

    private void fillSetting(ArrayList<Integer> countCrane) {
        phaser = new Phaser(1);
        for (int type = 0; type < 3; type++) {
            for (int i = 0; i < countCrane.get(type); i++) {
                Crane w = new Crane(phaser, Ship.TYPES_CARGO.getType(type).orElse(Ship.TYPES_CARGO.LOOSE));
                pool.get(type).add(w);
            }
        }
    }

    private void addShipQueue(Schedule schedule, int daysLeft, int minutesLeft) {
        for (int i = 0; i < schedule.getList().size(); i++) {
            synchronized (schedule.getList().get(i)) {
                if (schedule.getList().get(i).getArrivalTime().equals(new Date(daysLeft, minutesLeft))) {
                    Ship.TYPES_CARGO type = schedule.getList().get(i).getType();
                    queue.get(type.getValue()).offer(schedule.getList().get(i));
                }
            }
        }
    }
}
