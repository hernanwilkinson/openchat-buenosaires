package bsas.org.openchat;

public abstract class Environment {
    public static Environment current() {
        if(DevelopmentEnvironment.isCurrent()) return new DevelopmentEnvironment();
        return new IntegrationEnvironment();
    }

    public abstract OpenChatSystem createSystem(Clock clock);
}
