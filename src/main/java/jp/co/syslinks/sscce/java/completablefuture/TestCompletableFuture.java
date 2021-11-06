package jp.co.syslinks.sscce.java.completablefuture;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://www.jianshu.com/p/1b04e9c62bff
 */
public class TestCompletableFuture {

    private static final Random random = new Random();
    static {
        random.setSeed(System.currentTimeMillis());
    }

    public static void main(String[] args) throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(4);

        long start = System.currentTimeMillis();
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> {
            final int millis = 1000 + random.nextInt(1000);
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return String.format("商品详情(%s)", millis);
        }, executorService);

        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> {
            final int millis = 1000 + random.nextInt(1000);
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return String.format("卖家信息(%s)", millis);
        }, executorService);

        CompletableFuture<String> futureC = CompletableFuture.supplyAsync(() -> {
            final int millis = 1000 + random.nextInt(1000);
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return String.format("库存信息(%s)", millis);
        }, executorService);

        CompletableFuture<String> futureD = CompletableFuture.supplyAsync(() -> {
            final int millis = 1000 + random.nextInt(1000);
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return String.format("订单信息(%s)", millis);
        }, executorService);

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futureA, futureB, futureC, futureD);
        allFuture.join();

        System.out.println(futureA.join() + "," + futureB.join() + "," + futureC.join() + "," + futureD.join());
        System.out.println("总耗时:" + (System.currentTimeMillis() - start));
    }

}
