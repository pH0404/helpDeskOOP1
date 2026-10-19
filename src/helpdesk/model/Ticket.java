package helpdesk.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private final long id;
    private final String title;
    private final String description;
    private TicketStatus status;
    private final TicketPriority priority;
    private final LocalDateTime createdAt;
    private final List<StatusHistoryEntry> statusHistory = new ArrayList<>();

    public Ticket(long id, String title, String description) {
        this(id, title, description, TicketPriority.MEDIUM);
    }


    public Ticket(long id, String title, String description,
                  TicketPriority priority) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Название заявки не может быть пустым"
            );
        }

        if (priority == null) {
            throw new IllegalArgumentException(
                    "Приоритет заявки не может быть null"
            );
        }

        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.status = TicketStatus.NEW;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<StatusHistoryEntry> getStatusHistory() {
        return List.copyOf(statusHistory);
    }

    private void changeStatus(TicketStatus newStatus) {
        TicketStatus oldStatus = status;
        status = newStatus;

        statusHistory.add(new StatusHistoryEntry(
                oldStatus,
                newStatus,
                LocalDateTime.now()
        ));
    }

    public void startProcessing() {
        if (status != TicketStatus.NEW) {
            System.out.println(
                    "Ошибка: в работу можно взять только новую заявку"
            );
            return;
        }

        changeStatus(TicketStatus.IN_PROGRESS);
    }

    public void resolve() {
        if (status != TicketStatus.IN_PROGRESS) {
            throw new InvalidTicketStateException(
                    "Решить можно только заявку в работе"
            );
        }

        changeStatus(TicketStatus.RESOLVED);
    }

    public void close() {
        if (status != TicketStatus.RESOLVED) {
            throw new InvalidTicketStateException(
                    "Закрыть можно только решённую заявку"
            );
        }

        changeStatus(TicketStatus.CLOSED);
    }

    public void cancel() {
        if (status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException(
                    "Нельзя отменить закрытую заявку"
            );
        }

        if (status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException(
                    "Заявка уже отменена"
            );
        }

        changeStatus(TicketStatus.CANCELLED);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                '}';
    }
}