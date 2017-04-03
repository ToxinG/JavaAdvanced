package ru.ifmo.ctddev.maltsev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created by Антон on 27.03.2017.
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threadList;

    private final Queue<Runnable> taskQueue = new LinkedList<>();

    public ParallelMapperImpl(int threads) {
        threadList = new ArrayList<>(threads);
        Runnable runnable = () -> {
            try {
                Runnable task;
                while (!Thread.interrupted()) {
                    synchronized (taskQueue) {
                        while (taskQueue.isEmpty()) {
                            taskQueue.wait();
                        }
                        task = taskQueue.poll();
                    }
                    task.run();
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
            } finally {
                Thread.currentThread().interrupt();
            }
        };
        IntStream.range(0, threads).forEach(ind -> {
            threadList.add(new Thread(runnable));
            threadList.get(ind).start();
        });
    }

    public class MutableInteger {

        private int value;

        public void set(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {

        final MutableInteger counter = new MutableInteger();
        final List<R> results = new ArrayList<>(Collections.nCopies(list.size(), null));

        IntStream.range(0, list.size()).forEach(ind -> {
            synchronized (taskQueue) {
                taskQueue.add(() -> {
                    results.set(ind, function.apply(list.get(ind)));
                    synchronized (counter) {
                       if (counter.get() + 1 == list.size()) {
                           counter.set(counter.get() + 1);
                           counter.notify();
                       }
                    }
                }
                );
                taskQueue.notify();
            }
        });

        synchronized (counter) {
            if (counter.get() < list.size()) {
                counter.wait();
            }
        }

        return results;
        
    }

    @Override
    public void close() throws InterruptedException {
        threadList.forEach(Thread::interrupt);
        for (Thread t : threadList)
            t.join();
    }
}
