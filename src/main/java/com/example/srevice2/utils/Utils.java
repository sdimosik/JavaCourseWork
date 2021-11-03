package com.example.srevice2.utils;

import com.example.srevice2.model.Date;
import com.example.srevice2.model.Schedule;
import com.example.srevice2.model.Ship;

import java.util.Scanner;

public class Utils {
    public static int generateInt(int low, int height) {
        return low + (int) (Math.random() * (height - low) - 1);
    }

    public static Schedule addingSomeRecords(Schedule schedule) {
        final Scanner in = new Scanner(System.in);
        System.out.print("\nEnter count of adding records: ");
        int count = in.nextInt();

        for (int i = 0; i < count; i++) {
            System.out.println("Enter records: ");
            System.out.print("ArrivalTime (days [-8; 30], minutes[0;1439]). ONLY INTEGER ");
            int dateD, dateM;
            do {
                System.out.println("Enter dateD: ");
                dateD = in.nextInt();
            } while (dateD < -8 || dateD > 30);
            do {
                System.out.println("Enter dateM: ");
                dateM = in.nextInt();
            } while (dateM < 0 || dateM > 1439);

            System.out.print("Name: ");
            String name = in.next();
            System.out.print("Type cargo (CONTAINER(0)/LIQUID(1)/LOOSE(2)). ONLY INTEGER");
            int pos;
            do {
                System.out.println("Enter type: ");
                pos = in.nextInt();
            } while (pos < 0 || pos > 2);


            Ship.TYPES_CARGO type;
            if (pos == 0) {
                type = Ship.TYPES_CARGO.CONTAINER;
            } else if (pos == 1) {
                type = Ship.TYPES_CARGO.LIQUID;
            } else {
                type = Ship.TYPES_CARGO.LOOSE;
            }
            System.out.print("Quantity [1; 2000]. ONLY INTEGER ");
            int quantity;

            do {
                System.out.println("Enter Quantity: ");
                quantity = in.nextInt();
            } while (quantity < 1 || quantity > 2000);

            int parkingTime = quantity;

            Ship ship = new Ship(
                new Date(dateD, dateM),
                name,
                type,
                quantity,
                parkingTime
            );

            schedule.add(ship);
        }
        return schedule;
    }
}
