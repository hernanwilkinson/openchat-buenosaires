package bsas.org.openchat;

import java.util.function.Supplier;

public abstract class Environment {
    public static Environment current() {
        if(DevelopmentEnvironment.isCurrent()) return new DevelopmentEnvironment();
        return new IntegrationEnvironment();
    }

    public abstract OpenChatSystem createSystem(Clock clock);

    public abstract Supplier<OpenChatSystem> systemFactory();
}
