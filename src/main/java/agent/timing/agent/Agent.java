package agent.timing.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {

        String[] args = agentArgs.split(",");
        String platform = args[0];
        String route = args[1];

        inst.addTransformer(new Transformer(platform, route), true);
    }
}
