package helpdesk.repository;

import helpdesk.model.Ticket;
import java.util.List;
import java.util.Optional;
import helpdesk.model.TicketStatus;
import helpdesk.model.TicketDetails;
import helpdesk.model.StatusHistoryEntry;
import helpdesk.model.AgentTicketStats;

public interface TicketRepository {
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(long id);
    List<Ticket> findAll();
    List<Ticket> findPage(int page, int size);
    List<Ticket> searchByText(String text);
    List<TicketDetails> findAllWithDetails();
    List<AgentTicketStats> findAgentTicketStats();
    List<Ticket> findByStatus(TicketStatus status);
    List<StatusHistoryEntry> findStatusHistoryByTicketId(long ticketId);

    void update(Ticket ticket);
    void assignTicket(long ticketId, long assigneeId);

    void changeStatus(
            long ticketId,
            TicketStatus newStatus,
            Long changedById
    );

    boolean deleteById(long id);
}
