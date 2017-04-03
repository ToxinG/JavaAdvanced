package ru.ifmo.ctddev.maltsev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Антон on 27.03.2017.
 */

public class IterativeParallelism implements ListIP, ScalarIP {

    private ParallelMapper parallelMapper = null;

    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, R> R parallelize(int threads,
                                 List<? extends T> list,
                                 final Function<Stream<? extends T>, R> function,
                                 final Function<? super Stream<R>, R> merger) throws InterruptedException {
        List<Stream<? extends T>> sublists = split(threads, list);
        List<R> resultList;

        if (parallelMapper != null) {
            resultList = parallelMapper.map(function, sublists);
        } else {
            resultList = new ArrayList<>(Collections.nCopies(threads, null));
            List<Thread> threadList = new ArrayList<>();

            IntStream.range(0, sublists.size()).forEach(ind -> {
                Thread thread = new Thread(() -> resultList.set(ind, function.apply(sublists.get(ind))));
                threadList.add(thread);
                thread.start();
            });
            for (Thread t : threadList) {
                t.join();
            }
        }
        return merger.apply(resultList.stream());
    }

    private <T> List<Stream<? extends T>> split(int threads, List<? extends T> list) {
        List<Stream<? extends T>> sublists = new ArrayList<>();
        int length = (list.size()) / threads;
        int mod = list.size() % threads;
        int stage = 0;
        int sublistSize;
        for (int i = 0; i < list.size(); i += sublistSize) {
            sublistSize = length;
            if (stage < mod)
                sublistSize++;
            stage++;
            sublists.add(list.subList(i, i + sublistSize).stream());
        }
        return sublists;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> function = s -> s.max(comparator).get();
        return parallelize(threads, list, function, function);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        Function<Stream<? extends T>, T> function = s -> s.min(comparator).get();
        return parallelize(threads, list, function, function);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, Boolean> function = s -> s.allMatch(predicate);
        Function<Stream<Boolean>, Boolean> merger = s -> s.allMatch(val -> val);
        return parallelize(threads, list, function, merger);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, Boolean> function = s -> s.anyMatch(predicate);
        Function<Stream<Boolean>, Boolean> merger = s -> s.anyMatch(val -> val);
        return parallelize(threads, list, function, merger);
    }

    @Override
    public String join(int threads, List<?> list) throws InterruptedException {
        Function<Stream<?>, String> function = s -> s.map(Object::toString).collect(Collectors.joining());
        Function<Stream<String>, String> merger = s -> s.collect(Collectors.joining());
        return parallelize(threads, list, function, merger);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        Function<Stream<? extends T>, List<T>> function = s -> s.filter(predicate).collect(Collectors.toList());
        Function<Stream<List<T>>, List<T>> merger = s -> s.flatMap(List::stream).collect(Collectors.toList());
        return parallelize(threads, list, function, merger);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> mapper) throws InterruptedException {
        Function<Stream<? extends T>, List<U>> function = s -> s.map(mapper).collect(Collectors.toList());
        Function<Stream<List<U>>, List<U>> merger = s -> s.flatMap(List::stream).collect(Collectors.toList());
        return parallelize(threads, list, function, merger);
    }

}

