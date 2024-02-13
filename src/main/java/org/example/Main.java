package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

    final static String LETTERS = "RLRFR";
    final static int ROUTE_LENGTH = 100;
    final static int THREAD_COUNT = 1_000;

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) {

        Thread maxCounter = new Thread(() -> {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                        Map.Entry<Integer, Integer> max = sizeToFreq
                                .entrySet()
                                .stream()
                                .max(Map.Entry.comparingByValue())
                                .get();
                        System.out.println("Текущий лидер: " + max.getKey() + " (встретился " + max.getValue() + " раз)");
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        maxCounter.start();

        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(() -> {
                String route = generateRoute(LETTERS, ROUTE_LENGTH);
                int count = (int) route.chars().filter(ch -> ch == 'R').count();
                synchronized (sizeToFreq) {
                    if (sizeToFreq.containsKey(count)) {
                        sizeToFreq.put(count, sizeToFreq.get(count) + 1);
                    } else {
                        sizeToFreq.put(count, 1);
                    }
                    sizeToFreq.notify();
                }
            }).start();
        }

        maxCounter.interrupt();

        Map.Entry<Integer, Integer> max = sizeToFreq
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .get();

        System.out.println("Самое частое количество повторений "
                + max.getKey() + " (встретилось " + max.getValue() + " раз)");

        sizeToFreq.remove(max.getKey());

        System.out.println("Другие размеры :");
        sizeToFreq.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> System.out.println(" - " + entry.getKey() + " (" + entry.getValue() + " раз)"));
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

}