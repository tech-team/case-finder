package caseloader;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool {
    private static final int WAIT_TIMEOUT = 5 * 60;
    private int threadsCount = 2;

    private ExecutorService executor = null;

    public ThreadPool(int threadsCount) {
        this.threadsCount = threadsCount;
        executor = Executors.newFixedThreadPool(threadsCount);
    }

    public ThreadPool() {
        executor = Executors.newFixedThreadPool(threadsCount);
    }

    public void execute(Runnable command) {
        executor.execute(command);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void waitForFinish() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void stopExecution() {
        executor.shutdownNow();
    }
}
