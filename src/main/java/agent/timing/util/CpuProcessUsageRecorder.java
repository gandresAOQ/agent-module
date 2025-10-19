package agent.timing.util;

import agent.timing.mongo.MongoReporter;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CpuProcessUsageRecorder {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    private static final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static double start() {
        return CpuProcessUsageRecorder.getProcessCpuLoad();
    }

    public static void end(String threadName, String className, String method, double cpuBefore, String application, String platform) {
        double cpuAfter = CpuProcessUsageRecorder.getProcessCpuLoad();
        double avgCpu = (cpuBefore + cpuAfter) / 2;
        System.out.printf(
                "Thread Name: %s, Method: %s.%s | CPU usage: %.2f%%",
                threadName,
                className,
                method,
                avgCpu
        );
        EXECUTOR.submit(() -> CpuProcessUsageRecorder.logCpuExecution(className, method, avgCpu, application, platform));
    }

    private static void logCpuExecution(String className, String methodName, Double value, String application, String platform) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("methodName", methodName);
            data.put("className", className);
            data.put("fullName", className + "." + methodName);
            MongoReporter.report(data, "cpu_process", value, application, platform);
        } catch (Exception e) {
            System.err.println("Failed to log cpu execution: " + e.getMessage());
        }
    }

    private static double getProcessCpuLoad() {
        // Returns a double in [0.0,1.0]; multiply by 100 for %
        return osBean.getProcessCpuLoad() * 100;
    }

    private static double getSystemCpuLoad() {
        return osBean.getSystemCpuLoad() * 100;
    }

    private static long getProcessCpuTime() {
        return osBean.getProcessCpuTime(); // in nanoseconds
    }

}
