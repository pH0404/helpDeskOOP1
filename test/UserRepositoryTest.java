import helpdesk.model.User;
import helpdesk.repository.UserRepository;
import helpdesk.repository.jdbc.JdbcUserRepository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private final UserRepository repository =
            new JdbcUserRepository();

    @Test
    void findsExistingEmail() {
        var result = repository.findByEmail("anna@example.org");

        assertTrue(result.isPresent());
        assertEquals("Анна Петрова", result.get().getName());
    }

    @Test
    void returnsEmptyForMissingEmail() {
        var result =
                repository.findByEmail("missing-user@example.org");

        assertTrue(result.isEmpty());
    }
}