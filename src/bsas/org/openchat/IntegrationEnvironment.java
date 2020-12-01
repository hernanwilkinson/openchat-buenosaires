package bsas.org.openchat;

public class IntegrationEnvironment extends Environment {
    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new PersistentOpenChatSystem(clock);
    }
}
