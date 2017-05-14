package ru.ifmo.ctddev.maltsev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class WebCrawler implements Crawler {

    private final int perHost;
    private final Downloader downloader;
    private final ExecutorService downloaderPool, extractorPool;
    private final ConcurrentMap<String, Integer> depthByURL;
    private final ConcurrentMap<String, HostState> hostStateMap;

    private class HostState {
        int currentDownloadings;
        Queue<Runnable> delayedTasks;
        ReentrantLock lock;

        HostState() {
            currentDownloadings = 0;
            delayedTasks = new ArrayDeque<>();
            lock = new ReentrantLock();
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaderPool = Executors.newFixedThreadPool(Math.min(downloaders, 50));
        this.extractorPool = Executors.newFixedThreadPool(Math.min(extractors, 50));
        this.perHost = perHost;
        this.depthByURL = new ConcurrentHashMap<>();
        this.hostStateMap = new ConcurrentHashMap<>();
    }

    @Override
    public Result download(String url, int depth) {
        List<String> downloaded = new ArrayList<>();
        Map<String, IOException> errors = new HashMap<>();

        Phaser phaser = new Phaser(1);

        downloadRecursive(url, depth, downloaded, errors, phaser);
        phaser.awaitAdvance(phaser.arrive());

        downloaded.removeAll(errors.keySet());
        return new Result(downloaded, errors);
    }

    void downloadRecursive(String url,
                           int depth,
                           List<String> downloaded,
                           Map<String, IOException> errors,
                           Phaser phaser) {
        try {
            if ((depthByURL.containsKey(url) && depthByURL.get(url) >= depth) || depth == 0)
                return;
            if (!depthByURL.containsKey(url))
                downloaded.add(url);
            depthByURL.put(url, depth);
            String host = URLUtils.getHost(url);
            hostStateMap.putIfAbsent(host, new HostState());
            HostState current = hostStateMap.get(host);
            phaser.register();
            Runnable task = () -> {
                try {
                    Document d = downloader.download(url);
                    if (depth == 1)
                        return;
                    phaser.register();
                    extractorPool.submit(() -> {
                        try {
                            d.extractLinks().forEach(link -> downloadRecursive
                                    (link, depth - 1, downloaded, errors, phaser));
                        } catch (IOException e) {
                            errors.put(url, e);
                            //e.printStackTrace();
                        } finally {
                            phaser.arrive();
                        }
                    });
                } catch (IOException e) {
                    errors.put(url, e);
                    //e.printStackTrace();
                } finally {
                    current.lock.lock();
                    try {
                        if (!current.delayedTasks.isEmpty()) {
                            downloaderPool.submit(current.delayedTasks.poll());
                        } else {
                            current.currentDownloadings--;
                        }
                        phaser.arrive();
                    } finally {
                        current.lock.unlock();
                    }
                }
            };
            current.lock.lock();
            try {
                if (current.currentDownloadings < perHost) {
                    downloaderPool.submit(task);
                    current.currentDownloadings++;
                } else {
                    current.delayedTasks.add(task);
                }
            } finally {
                current.lock.unlock();
            }
        } catch (MalformedURLException e) {
            errors.put(url, e);
            //e.printStackTrace();
        }

    }

    @Override
    public void close() {
        downloaderPool.shutdown();
        extractorPool.shutdown();
    }

    public static void main(String[] args) {
        try {
            String startURL = args[0];
            int p = Runtime.getRuntime().availableProcessors();
            List<Integer> intArgs = new ArrayList<>(Collections.nCopies(3, p));
            for (int i = 2; i < args.length; i++) {
                intArgs.set(i - 2, Integer.parseInt(args[i]));
            }
            try (WebCrawler webCrawler = new WebCrawler(
                    new CachingDownloader(), intArgs.get(0), intArgs.get(1), intArgs.get(2))) {
                Result r = webCrawler.download(startURL, 2);
                System.out.println("downloaded " + r.getDownloaded().size() + ", errors " + r.getErrors().size());
            } catch (IOException e) {
                System.out.println("An error occurred while creating an instance of CachingDownloader");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Missing starting link");
        } catch (NullPointerException e) {
            System.out.println("Invalid arguments, not expected null");
        }
    }
}
