package bsas.org.openchat;

import java.time.LocalDateTime;
import java.util.function.Supplier;

public class IntegrationEnvironment extends Environment {
    @Override
    public OpenChatSystem createSystem(Clock clock) {
        return new PersistentOpenChatSystem(clock);
    }

    @Override
    public Supplier<OpenChatSystem> systemFactory() {
        return ()->createSystem(() -> LocalDateTime.now());
    }
}
