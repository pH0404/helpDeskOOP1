import helpdesk.model.Ticket;
import helpdesk.model.TicketStatus;
import helpdesk.model.TicketPriority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void newTicketHasNewStatus() {
        Ticket ticket = new Ticket(1, "Не работает Wi-Fi", "Описание проблемы",
                TicketPriority.HIGH);

        assertEquals(TicketStatus.NEW, ticket.getStatus());
    }

    @Test
    void newTicketCanStartProcessing() {
        Ticket ticket = new Ticket(1, "Не работает Wi-Fi", "Описание проблемы",
                TicketPriority.HIGH);

        ticket.startProcessing();

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
    }

    @Test
    void ticketCanBeResolvedAfterStarting() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        ticket.startProcessing();
        ticket.resolve();

        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
    }

    @Test
    void ticketCanBeClosedAfterResolving() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        ticket.startProcessing();
        ticket.resolve();
        ticket.close();

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
    }

    @Test
    void cannotResolveNewTicket() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        assertThrows(
                helpdesk.model.InvalidTicketStateException.class,
                ticket::resolve
        );

        assertEquals(TicketStatus.NEW, ticket.getStatus());
    }
}
