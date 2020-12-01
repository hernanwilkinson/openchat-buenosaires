package bsas.org.openchat;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class IntegrationEnvironment extends Environment {
    public static boolean isCurrent() {
        return !DevelopmentEnvironment.isCurrent();
    }

    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new PersistentOpenChatSystem(clock);
    }

    @Override
    public Supplier<OpenChatSystem> systemFactory() {
        return ()->createSystem(() -> LocalDateTime.now());
    }
}
