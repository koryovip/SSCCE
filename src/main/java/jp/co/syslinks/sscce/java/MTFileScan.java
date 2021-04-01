package jp.co.syslinks.sscce.java;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MTFileScan {

    private static final Logger logger = LoggerFactory.getLogger(MTFileScan.class);

    private static final int TIME_OUT_SEC = 5;
    private static final int DIR_MAX = 64;
    private static final int ThreadPoolSize = 2;
    private static final AtomicLong dirTotal = new AtomicLong(0L);
    private static final AtomicLong dirComplete = new AtomicLong(0L);

    public static final class NamedThreadFactory implements ThreadFactory {
        private final String name;
        private final AtomicInteger tag = new AtomicInteger(0);

        public NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, this.name + tag.getAndIncrement());
        }
    }

    private static final BlockingQueue<File> dirs = new LinkedBlockingQueue<>(DIR_MAX);
    private static final ExecutorService POOL = Executors.newFixedThreadPool(ThreadPoolSize, new NamedThreadFactory("POOL-"));
    private static final ScheduledExecutorService progress = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("progress-"));

    public static void main(String[] args) throws Exception {
        File root = Paths.get("E:/").toFile();
        new Thread(() -> {
            try {
                travel(root);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        progress.scheduleWithFixedDelay(() -> {
            logger.debug("{}/{}", dirComplete.longValue(), dirTotal.longValue());
        }, 1, 3, TimeUnit.SECONDS);

        File dir = null;
        while ((dir = dirs.poll(TIME_OUT_SEC, TimeUnit.SECONDS)) != null) {
            dirComplete.incrementAndGet();
            //TimeUnit.MILLISECONDS.sleep(100);
            final File dir$ = dir;
            POOL.execute(() -> {
                try {
                    for (File file : dir$.listFiles(_file -> {
                        return _file.isFile();
                    })) {
                        logger.trace("{}", file.getPath());
                    }
                } catch (Exception e) {
                    logger.error(dir$.getPath());
                    throw new RuntimeException(e);
                }
            });
        }

        logger.info("~~~ END ~~~");
        progress.shutdownNow();
        POOL.shutdown();
        //        while (POOL.awaitTermination(5, TimeUnit.SECONDS)) {
        //            System.out.println("~~~~~~~~~");
        //        }
    }

    public static void travel(File root) throws InterruptedException {
        if (root.isDirectory()) {
            // logger.debug("{}", root.getPath());
            String name = root.getName();
            if ("$RECYCLE.BIN".equals(name)) {
                return;
            }
            if (".Trashes".equals(name)) {
                return;
            }
            if ("System Volume Information".equals(name)) {
                return;
            }
            if (dirs.offer(root, TIME_OUT_SEC, TimeUnit.SECONDS)) {
                dirTotal.incrementAndGet();
            }
            for (File child : root.listFiles(_file -> {
                return _file.isDirectory();
            })) {
                travel(child);
            }
        }
    }

}
