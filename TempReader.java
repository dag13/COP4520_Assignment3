// Deron Gentles
// Programming Assignment #3
// Problem 2: Atmospheric Temperature Reading Module (50 points)
// COP4520 Spring 2022

import java.util.*;

public class TempReader {
	public static void main(String[] args)  throws InterruptedException{
		Thread[] threads = new Thread[8];
		System.out.println("24 Hour Report: ");
		System.out.println();

		for (int hour = 0; hour < 12; hour++) {
			for (int minute = 0; minute < 60; minute++) {
				for (int i = 0; i < 8; i++) {
					threads[i] = new Thread(new Sensor(i, minute));
					threads[i].start();
				}
			}
			System.out.println("Hour " + hour + " Results:");
			System.out.println();
			Sensor.printTemps();

			//intervals();
			System.out.println();

		}
	}
}

class Sensor implements Runnable {
	static final int MAX_TEMP = 70;
	static final int MIN_TEMP = -100;
	static int[][] readings = new int[8][60];
	static TreeSet<Integer> temps = new TreeSet<Integer>(); // A TreeSet is used to exclude duplicates and get the lowest and highest values
	Random rand = new Random();

	int sensNum, minute;

	public Sensor(int sensNum, int minute) {
		this.sensNum = sensNum;
		this.minute = minute;
	}

	@Override
	public void run() {
		readings[sensNum][minute] = rand.nextInt(MAX_TEMP - MIN_TEMP) + MIN_TEMP;
	}

	public static void printTemps() {
		for (int i = 0; i < 8; i++) {
			for (int minute = 0; minute < 60; minute++)
				temps.add(readings[i][minute]);

			System.out.print("Top 5 highest temperatures:");
			//System.out.println();
			for (int temp = 0; temp < 5; temp++)
				System.out.print(temps.pollLast() + " ");

			System.out.println();

			System.out.print("Top 5 lowest temperatures: ");
			//System.out.println();
			for (int temp = 0; temp < 5; temp++)
				System.out.print(temps.pollFirst() + " ");

			System.out.println();
		}
	}
}
