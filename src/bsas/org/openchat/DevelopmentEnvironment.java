package bsas.org.openchat;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class DevelopmentEnvironment extends Environment {
    public static boolean isCurrent() {
        return "Development".equals(System.getenv("ENV"));
    }

    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new TransientOpenChatSystem(clock);
    }

    @Override
    public Supplier<OpenChatSystem> systemFactory() {
        final OpenChatSystem system = createSystem(() -> LocalDateTime.now());
        return ()->system;
    }
}
