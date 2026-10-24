import helpdesk.model.Ticket;
import helpdesk.model.TicketComment;
import helpdesk.model.TicketPriority;
import helpdesk.repository.TicketCommentRepository;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.jdbc.JdbcTicketCommentRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketCommentRepositoryTest {

    @Test
    void returnsOnlyExternalComments() {
        TicketRepository ticketRepository =
                new JdbcTicketRepository();

        TicketCommentRepository commentRepository =
                new JdbcTicketCommentRepository();

        Ticket ticket = new Ticket(
                0,
                "Тест фильтра комментариев",
                "Проверка external-комментариев",
                TicketPriority.MEDIUM
        );

        ticket.setCustomerId(1L);
        ticket = ticketRepository.save(ticket);

        commentRepository.save(new TicketComment(
                ticket.getId(),
                1L,
                "Внешний комментарий",
                false
        ));

        commentRepository.save(new TicketComment(
                ticket.getId(),
                2L,
                "Внутренний комментарий",
                true
        ));

        List<TicketComment> externalComments =
                commentRepository.findExternalByTicketId(ticket.getId());

        assertEquals(1, externalComments.size());
        assertEquals(
                "Внешний комментарий",
                externalComments.get(0).getText()
        );
        assertFalse(externalComments.get(0).isInternal());

        assertTrue(
                externalComments.stream()
                        .noneMatch(c -> c.getText().equals(
                                "Внутренний комментарий"
                        ))
        );
    }
}