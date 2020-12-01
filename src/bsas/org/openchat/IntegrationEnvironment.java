package bsas.org.openchat;

public class IntegrationEnvironment {
    public PersistentOpenChatSystem createSystem(Clock clock) {
        return new PersistentOpenChatSystem(clock);
    }
}
