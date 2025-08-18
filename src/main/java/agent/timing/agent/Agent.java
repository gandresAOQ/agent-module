package agent.timing.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {

        String[] args = agentArgs.split(",");
        String route = args[0];

        inst.addTransformer(new Transformer(route), true);
    }
}
