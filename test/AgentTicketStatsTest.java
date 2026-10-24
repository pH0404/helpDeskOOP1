import helpdesk.model.AgentTicketStats;
import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AgentTicketStatsTest {

    private final TicketRepository repository =
            new JdbcTicketRepository();

    private Ticket createTicket(String title) {
        Ticket ticket = new Ticket(
                0,
                title,
                "Тест статистики сотрудника",
                TicketPriority.MEDIUM
        );

        ticket.setCustomerId(1L);
        return repository.save(ticket);
    }

    @Test
    void countsActiveResolvedAndClosedTickets() {
        // Агент с ID 3 — SUPPORT_AGENT из тестовых данных.
        long agentId = 3L;

        Map<Long, AgentTicketStats> before =
                repository.findAgentTicketStats().stream()
                        .collect(Collectors.toMap(
                                AgentTicketStats::getAgentId,
                                Function.identity()
                        ));

        assertTrue(
                before.containsKey(agentId),
                "В базе должен существовать специалист с ID 3"
        );

        AgentTicketStats initial = before.get(agentId);

        Ticket active = createTicket("Статистика: активная");
        repository.assignTicket(active.getId(), agentId);

        Ticket resolved = createTicket("Статистика: решённая");
        repository.assignTicket(resolved.getId(), agentId);
        repository.changeStatus(
                resolved.getId(), TicketStatus.IN_PROGRESS, agentId
        );
        repository.changeStatus(
                resolved.getId(), TicketStatus.RESOLVED, agentId
        );

        Ticket closed = createTicket("Статистика: закрытая");
        repository.assignTicket(closed.getId(), agentId);
        repository.changeStatus(
                closed.getId(), TicketStatus.IN_PROGRESS, agentId
        );
        repository.changeStatus(
                closed.getId(), TicketStatus.RESOLVED, agentId
        );
        repository.changeStatus(
                closed.getId(), TicketStatus.CLOSED, agentId
        );

        Map<Long, AgentTicketStats> after =
                repository.findAgentTicketStats().stream()
                        .collect(Collectors.toMap(
                                AgentTicketStats::getAgentId,
                                Function.identity()
                        ));

        AgentTicketStats actual = after.get(agentId);

        assertNotNull(actual);
        assertEquals(
                initial.getActiveTickets() + 1,
                actual.getActiveTickets()
        );
        assertEquals(
                initial.getResolvedTickets() + 1,
                actual.getResolvedTickets()
        );
        assertEquals(
                initial.getClosedTickets() + 1,
                actual.getClosedTickets()
        );
    }
}