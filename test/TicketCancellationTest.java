import helpdesk.model.Customer;
import helpdesk.model.InvalidTicketStateException;
import helpdesk.model.SupportAgent;
import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;
import helpdesk.service.ConsoleNotificationService;
import helpdesk.service.TicketService;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketCancellationTest {

    private final TicketRepository repository =
            new JdbcTicketRepository();

    private final TicketService service =
            new TicketService(
                    new ConsoleNotificationService(),
                    repository
            );

    private final Customer customer = new Customer(
            1,
            "Анна Петрова",
            "anna@example.org"
    );

    private final SupportAgent agent = new SupportAgent(
            2,
            "Иван Соколов",
            "ivan.support@example.org"
    );

    private Ticket createTicket(String title) {
        Ticket ticket = new Ticket(
                0,
                title,
                "Тестирование отмены заявки",
                TicketPriority.MEDIUM
        );

        return service.createTicket(customer, ticket);
    }

    @Test
    void canCancelNewTicket() {
        Ticket ticket = createTicket("Отмена новой заявки");

        service.cancelTicket(agent, ticket);

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());

        Ticket saved = repository.findById(ticket.getId())
                .orElseThrow();

        assertEquals(TicketStatus.CANCELLED, saved.getStatus());
    }

    @Test
    void canCancelInProgressTicket() {
        Ticket ticket = createTicket("Отмена заявки в работе");

        service.startTicket(agent, ticket);
        service.cancelTicket(agent, ticket);

        Ticket saved = repository.findById(ticket.getId())
                .orElseThrow();

        assertEquals(TicketStatus.CANCELLED, saved.getStatus());
    }

    @Test
    void cannotCancelClosedTicket() {
        Ticket ticket = createTicket("Попытка отмены закрытой заявки");

        service.startTicket(agent, ticket);
        service.resolveTicket(agent, ticket);
        service.closeTicket(agent, ticket);

        assertThrows(
                InvalidTicketStateException.class,
                () -> service.cancelTicket(agent, ticket)
        );

        Ticket saved = repository.findById(ticket.getId())
                .orElseThrow();

        assertEquals(TicketStatus.CLOSED, saved.getStatus());
    }
}