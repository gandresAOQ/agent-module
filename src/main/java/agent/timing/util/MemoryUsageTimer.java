package agent.timing.util;

import agent.timing.mongo.MongoReporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoryUsageTimer {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    public static void start(String threadName, String method) {
        long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println(">>> [START - MEMORY] " + threadName + " - " + method + " - Used Memory: " + usedMemoryBefore + " bytes");
    }

    public static void end(String threadName, long usedMemoryBefore, String className, String method, String platform) {
        long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.println("<<< [END - MEMORY] " + threadName + " - " + method + " - Used memory before: " + usedMemoryBefore
                + " - Used memory after: " + usedMemoryAfter + " bytes");
        EXECUTOR.submit(() -> MemoryUsageTimer.logMemoryUsage(className, method, Double.valueOf(usedMemoryAfter), platform));
    }

    private static void logMemoryUsage(String className, String methodName, Double value, String platform) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("methodName", methodName);
            data.put("className", className);
            data.put("fullName", className + "." + methodName);
            MongoReporter.report(data, "memory", value, platform);
        } catch (Exception e) {
            System.err.println("Failed to log memory execution: " + e.getMessage());
        }
    }
}
