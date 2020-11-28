package bsas.org.openchat;

import org.apache.http.impl.client.HttpClients;

public abstract class Environment {
    public static Environment current() {
        if(DevelopmentEnvironment.isCurrent()) return new DevelopmentEnvironment();
        if(IntegrationEnvironment.isCurrent()) return new IntegrationEnvironment();

        throw new RuntimeException("Unknown Environment");
    }

    public abstract OpenChatSystem createSystem(Clock clock);
}
