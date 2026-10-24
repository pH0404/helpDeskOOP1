package helpdesk.repository;

import helpdesk.model.TicketComment;

import java.util.List;

public interface TicketCommentRepository {

    TicketComment save(TicketComment comment);
    List<TicketComment> findExternalByTicketId(long ticketId);
    List<TicketComment> findByTicketId(long ticketId);


}