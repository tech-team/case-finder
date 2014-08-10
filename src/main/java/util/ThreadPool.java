package util;

import java.util.concurrent.*;

public class ThreadPool {
    private int threadsCount = 2;

    private ExecutorService executor = null;

    public ThreadPool(int threadsCount) {
        this.threadsCount = threadsCount;
        executor = Executors.newFixedThreadPool(threadsCount);
    }

    public ThreadPool() {
        executor = Executors.newFixedThreadPool(threadsCount);
    }

    public void execute(Runnable command) throws InterruptedException {
        try {
            executor.execute(command);
        } catch (RejectedExecutionException e) {
            throw new InterruptedException("Execution rejected due to stopped thread pool");
        }
    }

    public <T> Future<T> submit(Callable<T> task) throws InterruptedException {
        try {
            return executor.submit(task);
        } catch (RejectedExecutionException e) {
            throw new InterruptedException("Execution rejected due to stopped thread pool");
        }
    }

    public void waitForFinish() throws InterruptedException {
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(100);
        }
    }


    public void stopExecution() {
        executor.shutdownNow();
    }
}
