package helpdesk.repository;

import helpdesk.model.Ticket;
import java.util.ArrayList;
import java.util.List;
import helpdesk.model.TicketStatus;
import helpdesk.model.TicketNotFoundException;

public class TicketRepository {
    private final List<Ticket> tickets = new ArrayList<>();
    public void add(Ticket ticket) {
        tickets.add(ticket);
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets);
    }
    public List<Ticket> findByStatus(TicketStatus status) {
        List<Ticket> result = new ArrayList<>();

        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == status) {
                result.add(ticket);
            }
        }

        return result;
    }

    public Ticket findById(long id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }

        throw new TicketNotFoundException(
                "Заявка с ID " + id + " не найдена"
        );
    }

}
