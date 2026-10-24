package helpdesk.service;

import helpdesk.model.Administrator;
import helpdesk.model.SupportAgent;
import helpdesk.model.Ticket;
import helpdesk.model.User;
import helpdesk.repository.TicketRepository;
import helpdesk.model.TicketNotFoundException;
import helpdesk.model.Customer;
import helpdesk.model.TicketStatus;
import helpdesk.model.InvalidTicketStateException;

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

    private void validateTransition(
            TicketStatus currentStatus,
            TicketStatus newStatus
    ) {
        boolean valid = switch (currentStatus) {
            case NEW ->
                    newStatus == TicketStatus.IN_PROGRESS
                            || newStatus == TicketStatus.CANCELLED;

            case IN_PROGRESS ->
                    newStatus == TicketStatus.RESOLVED
                            || newStatus == TicketStatus.CANCELLED;

            case RESOLVED ->
                    newStatus == TicketStatus.CLOSED;

            case CLOSED, CANCELLED -> false;
        };

        if (!valid) {
            throw new InvalidTicketStateException(
                    "Недопустимый переход статуса: "
                            + currentStatus + " -> " + newStatus
            );
        }
    }

    public Ticket createTicket(User user, Ticket ticket) {
        if (!(user instanceof Customer)) {
            throw new SecurityException(
                    "Только клиент может создавать заявку"
            );
        }

        ticket.setCustomerId(user.getId());

        return ticketRepository.save(ticket);
    }

    private void checkSpecialistAccess(User user) {
        if (!(user instanceof SupportAgent)
                && !(user instanceof Administrator)) {
            throw new SecurityException(
                    "Недостаточно прав для выполнения действия"
            );
        }
    }

    public void assignTicket(
            User user,
            Ticket ticket,
            SupportAgent assignee
    ) {
        checkSpecialistAccess(user);

        if (ticket.getId() == null) {
            throw new IllegalArgumentException(
                    "Нельзя назначить специалиста на заявку без ID"
            );
        }

        if (assignee == null) {
            throw new IllegalArgumentException(
                    "Специалист не может быть null"
            );
        }

        ticketRepository.assignTicket(
                ticket.getId(),
                assignee.getId()
        );

        Ticket updatedTicket = getTicketById(ticket.getId());

        ticket.setAssigneeId(updatedTicket.getAssigneeId());
        ticket.setUpdatedAt(updatedTicket.getUpdatedAt());
    }

    private void changeTicketStatus(User user, Ticket ticket, TicketStatus newStatus) {
        checkSpecialistAccess(user);

        if (ticket.getId() == null) {
            throw new IllegalArgumentException(
                    "Нельзя изменить статус заявки без ID"
            );
        }

        validateTransition(ticket.getStatus(), newStatus);

        ticketRepository.changeStatus(
                ticket.getId(),
                newStatus,
                user.getId()
        );

        Ticket updatedTicket = getTicketById(ticket.getId());

        ticket.setStatus(updatedTicket.getStatus());
        ticket.setUpdatedAt(updatedTicket.getUpdatedAt());
        ticket.setClosedAt(updatedTicket.getClosedAt());
    }

    public void startTicket(User user, Ticket ticket) {
        changeTicketStatus(user, ticket, TicketStatus.IN_PROGRESS);

        notificationService.send(
                "Заявка №" + ticket.getId() + " принята в работу"
        );
    }

    public void resolveTicket(User user, Ticket ticket) {
        changeTicketStatus(user, ticket, TicketStatus.RESOLVED);

        notificationService.send(
                "По заявке №" + ticket.getId() + " найдено решение"
        );
    }

    public void closeTicket(User user, Ticket ticket) {
        changeTicketStatus(user, ticket, TicketStatus.CLOSED);

        notificationService.send(
                "Заявка №" + ticket.getId() + " закрыта"
        );
    }

    public Ticket getTicketById(long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Заявка с ID " + id + " не найдена"
                ));
    }

    public void cancelTicket(User user, Ticket ticket) {
        changeTicketStatus(user, ticket, TicketStatus.CANCELLED);

        notificationService.send(
                "Заявка №" + ticket.getId() + " отменена"
        );
    }
}