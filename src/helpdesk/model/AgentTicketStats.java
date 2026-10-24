package helpdesk.model;

public class AgentTicketStats {
    private final long agentId;
    private final String agentName;
    private final long activeTickets;
    private final long resolvedTickets;
    private final long closedTickets;

    public AgentTicketStats(
            long agentId,
            String agentName,
            long activeTickets,
            long resolvedTickets,
            long closedTickets
    ) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.activeTickets = activeTickets;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
    }

    public long getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public long getActiveTickets() {
        return activeTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    @Override
    public String toString() {
        return "AgentTicketStats{" +
                "agentId=" + agentId +
                ", agentName='" + agentName + '\'' +
                ", activeTickets=" + activeTickets +
                ", resolvedTickets=" + resolvedTickets +
                ", closedTickets=" + closedTickets +
                '}';
    }
}