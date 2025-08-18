package agent.timing.util;

import agent.timing.mongo.MongoReporter;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CpuSystemUsageRecorder {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    private static final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static double start() {
        double cpu = CpuSystemUsageRecorder.getSystemCpuLoad();
        if (Double.isNaN(cpu)) {
            return -1;
        }
        System.out.printf("This is the CPU: %f", cpu);
        return cpu;
    }

    public static void end(String threadName, String className, String method, double cpuBefore, String application, String platform) {
        if (cpuBefore > 0) {
            double cpuAfter = CpuSystemUsageRecorder.getSystemCpuLoad();
            double avgCpu = (cpuBefore + cpuAfter) / 2;
            System.out.printf(
                    "Thread Name: %s, Method: %s.%s | CPU usage: %.2f%%",
                    threadName,
                    className,
                    method,
                    avgCpu
            );
            EXECUTOR.submit(() -> CpuSystemUsageRecorder.logCpuExecution(className, method, avgCpu, application, platform));
        }
    }

    private static void logCpuExecution(String className, String methodName, Double value, String application, String platform) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("methodName", methodName);
            data.put("className", className);
            data.put("fullName", className + "." + methodName);
            MongoReporter.report(data, "cpu_system", value, application, platform);
        } catch (Exception e) {
            System.err.println("Failed to log cpu execution: " + e.getMessage());
        }
    }

    private static double getSystemCpuLoad() {
        return osBean.getSystemCpuLoad() * 100;
    }

    private static long getProcessCpuTime() {
        return osBean.getProcessCpuTime(); // in nanoseconds
    }

}
