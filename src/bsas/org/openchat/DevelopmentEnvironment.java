package bsas.org.openchat;

public class DevelopmentEnvironment extends Environment {
    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new TransientOpenChatSystem(clock);
    }
}
