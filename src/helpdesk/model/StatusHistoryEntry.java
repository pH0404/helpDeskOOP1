package helpdesk.model;

import java.time.LocalDateTime;

public class StatusHistoryEntry {
    private final TicketStatus oldStatus;
    private final TicketStatus newStatus;
    private final LocalDateTime changedAt;

    public StatusHistoryEntry(
            TicketStatus oldStatus,
            TicketStatus newStatus,
            LocalDateTime changedAt
    ) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public TicketStatus getOldStatus() {
        return oldStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    @Override
    public String toString() {
        return oldStatus + " -> " + newStatus
                + " (" + changedAt + ")";
    }
}