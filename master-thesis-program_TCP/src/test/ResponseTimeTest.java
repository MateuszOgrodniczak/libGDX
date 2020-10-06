package test;

import java.io.IOException;

public class ResponseTimeTest {
    public static int clientsCount = 5;

    public static void main(String... args) {
        for (int i = 0; i < clientsCount; i++) {
            Thread t = new Thread(() -> {
                try {
                    JavaProcess.exec(Launcher.class, null);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    }
}
