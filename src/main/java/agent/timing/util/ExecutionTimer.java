package agent.timing.util;

import agent.timing.mongo.MongoReporter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutionTimer {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    public static void start(String threadName, String method) {
        System.out.println(">>> [START] " + threadName + " - " + method);
    }

    public static void end(String threadName, String className, String method, long durationNano) {
        System.out.println("<<< [END] " + threadName + " - " + method + " - Time: " + durationNano + " ns");
        EXECUTOR.submit(() -> logTimeExecution(className, method, (double) durationNano));
    }

    private static void logTimeExecution(String className, String methodName, Double value) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("methodName", methodName);
            data.put("className", className);
            data.put("fullName", className + "." + methodName);
            MongoReporter.report(data, "time", value);
        } catch (Exception e) {
            System.err.println("Failed to log time execution: " + e.getMessage());
        }
    }
}
