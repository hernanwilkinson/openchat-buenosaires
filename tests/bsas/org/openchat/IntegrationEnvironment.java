package bsas.org.openchat;

public class IntegrationEnvironment extends Environment {
    public static boolean isCurrent() {
        return !DevelopmentEnvironment.isCurrent();
    }

    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new PersistentOpenChatSystem(clock);
    }
}
