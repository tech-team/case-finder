package util;

public abstract class Sleeper {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace(); // TODO
        }
    }
}
