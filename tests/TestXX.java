import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestXX {
    @Test
    public void publisherCanNotHaveBlankName() {
        RuntimeException error = assertThrows(
                RuntimeException.class,
                ()->Publisher.named(" ","password","about"));

        assertEquals(Publisher.INVALID_NAME,error.getMessage());
    }
}