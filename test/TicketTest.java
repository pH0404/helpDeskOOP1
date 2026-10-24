import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void newTicketHasNewStatus() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        assertEquals(TicketStatus.NEW, ticket.getStatus());

        System.out.println("newTicketHasNewStatus — PASSED");
    }

    @Test
    void canStartProcessing() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        ticket.startProcessing();

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());

        System.out.println("canStartProcessing — PASSED");
    }

    @Test
    void canResolveAfterStarting() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        ticket.startProcessing();
        ticket.resolve();

        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());

        System.out.println("canResolveAfterStarting — PASSED");
    }

    @Test
    void canCloseAfterResolving() {
        Ticket ticket = new Ticket(1, "Wi-Fi", "Не работает",
                TicketPriority.HIGH);

        ticket.startProcessing();
        ticket.resolve();
        ticket.close();

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());

        System.out.println("canCloseAfterResolving — PASSED");
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

        System.out.println("cannotResolveNewTicket — PASSED");
    }
}
