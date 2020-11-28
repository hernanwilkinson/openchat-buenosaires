package bsas.org.openchat;

public class DevelopmentEnvironment extends Environment{
    public static boolean isCurrent() {
        return "Development".equals(System.getenv("ENV"));
    }

    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new TransientOpenChatSystem(clock);
    }
}
