package helpdesk.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status = TicketStatus.NEW;
    private TicketPriority priority = TicketPriority.MEDIUM;

    private Long customerId;
    private Long assigneeId;
    private Long categoryId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    private final List<StatusHistoryEntry> statusHistory = new ArrayList<>();

    public Ticket() {
    }
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
        this.updatedAt = this.createdAt;
        this.status = TicketStatus.NEW;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Название заявки не может быть пустым"
            );
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
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
    public void setStatus(TicketStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Статус заявки не может быть null"
            );
        }
        this.status = status;
    }

    public void setPriority(TicketPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException(
                    "Приоритет заявки не может быть null"
            );
        }
        this.priority = priority;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<StatusHistoryEntry> getStatusHistory() {
        return List.copyOf(statusHistory);
    }

    private void changeStatus(TicketStatus newStatus) {
        TicketStatus oldStatus = status;
        LocalDateTime now = LocalDateTime.now();

        status = newStatus;
        updatedAt = now;

        if (newStatus == TicketStatus.CLOSED) {
            closedAt = now;
        }
        statusHistory.add(new StatusHistoryEntry(
                oldStatus,
                newStatus,
                now
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