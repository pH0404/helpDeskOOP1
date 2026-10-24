import helpdesk.model.Ticket;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TicketPaginationTest {

    private final TicketRepository repository =
            new JdbcTicketRepository();

    @Test
    void returnsPagesWithoutDuplicates() {
        assertTrue(
                repository.findAll().size() >= 20,
                "Для проверки нужно минимум 20 заявок"
        );

        var firstPage = repository.findPage(1, 5);
        var secondPage = repository.findPage(2, 5);

        assertEquals(5, firstPage.size());
        assertEquals(5, secondPage.size());

        Set<Long> firstIds = firstPage.stream()
                .map(Ticket::getId)
                .collect(Collectors.toSet());

        Set<Long> secondIds = secondPage.stream()
                .map(Ticket::getId)
                .collect(Collectors.toSet());

        assertTrue(
                java.util.Collections.disjoint(firstIds, secondIds),
                "На соседних страницах не должно быть одинаковых заявок"
        );
    }

    @Test
    void returnsEmptyPageBeyondAvailableData() {
        assertTrue(repository.findPage(100, 5).isEmpty());
    }

    @Test
    void rejectsInvalidPageAndSize() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findPage(0, 5)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.findPage(1, 0)
        );
    }
}