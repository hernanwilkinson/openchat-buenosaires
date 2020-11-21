package bsas.org.openchat;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class RecoverExample {

    public static void main(String[] args) throws IOException {
        System.out.println("Creating data ...");
        final String fileName = "openchat.data";
        final FileWriter writer = new FileWriter(fileName);
        new LoadExample().load(new ActionPersistentReceptionist(
                new RestReceptionist(new OpenChatSystem(()-> LocalDateTime.now())),
                writer));

        writer.close();

        System.out.println("Loading data ...");
        final FileReader reader = new FileReader(fileName);
        final long startTime = System.currentTimeMillis();
        PersistedReceptionistLoader.loadFrom(reader);
        final long endTime = System.currentTimeMillis();
        System.out.println("Load Time: " + (endTime-startTime));

        reader.close();

    }
}
