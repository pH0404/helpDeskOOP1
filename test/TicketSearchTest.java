import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TicketSearchTest {

    private final TicketRepository repository =
            new JdbcTicketRepository();

    private Ticket createTicket(String title, String description) {
        Ticket ticket = new Ticket(
                0,
                title,
                description,
                TicketPriority.MEDIUM
        );

        ticket.setCustomerId(1L);

        return repository.save(ticket);
    }

    @Test
    void searchesTitleIgnoringCase() {
        String marker = "SearchTitle" + UUID.randomUUID()
                .toString().replace("-", "");

        Ticket ticket = createTicket(
                "Заявка " + marker,
                "Обычное описание"
        );

        var results = repository.searchByText(
                marker.toLowerCase(Locale.ROOT)
        );

        assertTrue(results.stream().anyMatch(
                result -> result.getId().equals(ticket.getId())
        ));
    }

    @Test
    void searchesDescriptionIgnoringCase() {
        String marker = "SearchDescription" + UUID.randomUUID()
                .toString().replace("-", "");

        Ticket ticket = createTicket(
                "Обычная заявка",
                "Проблема: " + marker
        );

        var results = repository.searchByText(
                marker.toLowerCase(Locale.ROOT)
        );

        assertTrue(results.stream().anyMatch(
                result -> result.getId().equals(ticket.getId())
        ));
    }

    @Test
    void rejectsBlankSearchText() {
        assertThrows(
                IllegalArgumentException.class,
                () -> repository.searchByText("  ")
        );
    }
}