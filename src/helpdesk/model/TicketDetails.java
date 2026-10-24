package helpdesk.model;

public class TicketDetails {
    private final long ticketId;
    private final String title;
    private final TicketStatus status;
    private final TicketPriority priority;
    private final String customerName;
    private final String assigneeName;
    private final String categoryName;

    public TicketDetails(
            long ticketId,
            String title,
            TicketStatus status,
            TicketPriority priority,
            String customerName,
            String assigneeName,
            String categoryName
    ) {
        this.ticketId = ticketId;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.customerName = customerName;
        this.assigneeName = assigneeName;
        this.categoryName = categoryName;
    }

    public long getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String toString() {
        return "TicketDetails{" +
                "ticketId=" + ticketId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", customerName='" + customerName + '\'' +
                ", assigneeName='" + assigneeName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}