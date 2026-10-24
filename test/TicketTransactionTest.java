import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTransactionTest {

    private final TicketRepository repository =
            new JdbcTicketRepository();

    @Test
    void rollsBackStatusWhenHistoryInsertFails() {
        Ticket ticket = new Ticket(
                0,
                "Проверка rollback",
                "Тест отката транзакции",
                TicketPriority.MEDIUM
        );

        ticket.setCustomerId(1L);
        ticket = repository.save(ticket);

        long ticketId = ticket.getId();

        // Несуществующий пользователь вызовет ошибку внешнего ключа
        // при записи истории после обновления статуса.
        assertThrows(
                RuntimeException.class,
                () -> repository.changeStatus(
                        ticketId,
                        TicketStatus.IN_PROGRESS,
                        9_999_999L
                )
        );

        // Статус должен остаться прежним.
        Ticket saved = repository.findById(ticketId)
                .orElseThrow();

        assertEquals(TicketStatus.NEW, saved.getStatus());

        // Запись об отклонённом переходе не должна сохраниться.
        assertTrue(
                repository.findStatusHistoryByTicketId(ticketId).isEmpty()
        );
    }
}