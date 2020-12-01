package bsas.org.openchat;

public abstract class Environment {
    public abstract OpenChatSystem createSystem(Clock clock);
}
