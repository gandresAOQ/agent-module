package agent.timing.agent;

import agent.timing.util.CpuProcessUsageRecorder;
import agent.timing.util.CpuSystemUsageRecorder;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class Transformer implements ClassFileTransformer {

    private String route;

    public Transformer(String route) {
        this.route = route;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null || !className.startsWith(this.route)) {
            return null;
        }

        if (className.startsWith(this.route + "/measure")) {
            return null;
        }

        System.out.println("Class name: "+ className);
        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(className.replace('/', '.'));
            if (this.isNotAllowedClass(ctClass, loader)) return null;
            for (CtMethod method : ctClass.getDeclaredMethods()) {

                if (Arrays.toString(method.getAnnotations()).contains("MeasureTime")) {
                    return null;
                }

                if (this.isStartMethod(method)) {
                    return null;
                }
                System.out.println("This is the type: " + method.getReturnType().getName());
                if (method.getReturnType().getName().equals("io.smallrye.mutiny.Uni") || method.getReturnType().getName().equals("io.smallrye.mutiny.Multi")) {
                    System.out.println("The type is Uni or Multi: " + method.getName());
                    return null;
                }
                method.addLocalVariable("_className", ClassPool.getDefault().get("java.lang.String"));
                method.addLocalVariable("_start", CtClass.longType);
                method.addLocalVariable("_usedMemoryBefore", CtClass.longType);
                method.addLocalVariable("_usedCpuProcessBefore", CtClass.doubleType);
                method.addLocalVariable("_usedCpuSystemBefore", CtClass.doubleType);

                method.insertBefore("_usedCpuProcessBefore = " + CpuProcessUsageRecorder.start() + ";");
                method.insertBefore("_usedCpuSystemBefore = " + CpuSystemUsageRecorder.start() + ";");
                method.insertBefore("_className = \"" + ctClass.getName() + "\";");
                method.insertBefore("_usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();");
                method.insertBefore("_start=System.nanoTime();");

                method.insertBefore("agent.timing.util.ExecutionTimer.start(Thread.currentThread().getName(), \"" +
                            method.getName() + "\");");

                method.insertAfter("agent.timing.util.ExecutionTimer.end(Thread.currentThread().getName(), _className, \"" +
                        method.getName() + "\", System.nanoTime() - _start, " + "\"" + this.getApplication() + "\", " + "\"" + this.getPlatform() + "\");");

                method.insertAfter("agent.timing.util.MemoryUsageTimer.end(Thread.currentThread().getName(), _usedMemoryBefore, _className, \"" +
                        method.getName() + "\", \"" + this.getApplication() + "\", " + "\"" + this.getPlatform() + "\");");

                method.insertAfter("agent.timing.util.CpuProcessUsageRecorder.end(Thread.currentThread().getName(), _className, \"" +
                        method.getName() + "\", _usedCpuProcessBefore, \"" + this.getApplication() + "\", \"" + this.getPlatform() + "\");");

                method.insertAfter("agent.timing.util.CpuSystemUsageRecorder.end(Thread.currentThread().getName(), _className, \"" +
                        method.getName() + "\", _usedCpuSystemBefore, \"" + this.getApplication() + "\", \"" + this.getPlatform() + "\");");
            }

            return ctClass.toBytecode();
        } catch (Exception e) {
            System.out.printf("Error for class: %s\n", className);
            e.printStackTrace();
        }
        return null;
    }


    private boolean isNotAllowedClass(CtClass ctClass, ClassLoader loader) {
        String className = ctClass.getName();
        return ctClass.isInterface() ||
                ctClass.isAnnotation() ||
                ctClass.isEnum() ||
                className.startsWith("io.quarkus") ||
                className.startsWith("org.jboss") ||
                className.startsWith("javax.") ||
                className.startsWith("jakarta.") ||
                className.startsWith("sun.") ||
                className.startsWith("java.") ||
                className.startsWith("com.sun") ||
                className.toLowerCase().contains("dto") ||
                className.toLowerCase().contains("bean") ||
                className.toLowerCase().contains("proxy") ||
                className.contains("$") ||
                loader.getClass().getName().contains("Quarkus");
    }

    private boolean isStartMethod(CtMethod method) {
        return method.getName().contains("getTypes") || method.getName().contains("getPriority");
    }

    public String getPlatform() {
        return System.getenv("PLATFORM");
    }

    public String getApplication() {
        return System.getenv("APPLICATION");
    }
}
