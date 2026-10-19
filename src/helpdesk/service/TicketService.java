package helpdesk.service;

import helpdesk.model.Administrator;
import helpdesk.model.SupportAgent;
import helpdesk.model.Ticket;
import helpdesk.model.User;
import helpdesk.repository.TicketRepository;

public class TicketService {
    private final NotificationService notificationService;
    private final TicketRepository ticketRepository;

    public TicketService(
            NotificationService notificationService,
            TicketRepository ticketRepository
    ) {
        this.notificationService = notificationService;
        this.ticketRepository = ticketRepository;
    }

    private void checkSpecialistAccess(User user) {
        if (!(user instanceof SupportAgent)
                && !(user instanceof Administrator)) {
            throw new SecurityException(
                    "Недостаточно прав для выполнения действия"
            );
        }
    }

    public void startTicket(User user, Ticket ticket) {
        checkSpecialistAccess(user);

        ticket.startProcessing();
        notificationService.send(
                "Заявка №" + ticket.getId() + " принята в работу"
        );
    }

    public void resolveTicket(User user, Ticket ticket) {
        checkSpecialistAccess(user);

        ticket.resolve();
        notificationService.send(
                "По заявке №" + ticket.getId() + " найдено решение"
        );
    }

    public void closeTicket(User user, Ticket ticket) {
        checkSpecialistAccess(user);

        ticket.close();
        notificationService.send(
                "Заявка №" + ticket.getId() + " закрыта"
        );
    }

    public Ticket getTicketById(long id) {
        return ticketRepository.findById(id);
    }
}