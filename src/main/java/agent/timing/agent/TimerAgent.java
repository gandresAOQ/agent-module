package agent.timing.agent;

import java.lang.instrument.Instrumentation;

public class TimerAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new TimerTransformer(), true);
    }
}
