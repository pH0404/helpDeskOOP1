
package helpdesk;

import helpdesk.model.Customer;
import helpdesk.model.SupportAgent;
import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.StatusHistoryEntry;
import helpdesk.model.TicketComment;
import helpdesk.model.TicketDetails;
import helpdesk.repository.TicketRepository;
import helpdesk.repository.TicketCommentRepository;
import helpdesk.repository.jdbc.JdbcTicketRepository;
import helpdesk.repository.jdbc.JdbcTicketCommentRepository;
import helpdesk.service.NotificationService;
import helpdesk.service.TelegramNotificationService;
import helpdesk.service.TicketService;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== HELP DESK ===");

        Customer customer = new Customer(
                1, "Анна Петрова", "anna@xxx.ru"
        );

        SupportAgent agent = new SupportAgent(
                3,
                "Сергей Волков",
                "sergey.net@example.org"
        );

        System.out.println("Клиент: " + customer.getName());
        System.out.println("Специалист: " + agent.getName());

        TicketRepository repository = new JdbcTicketRepository();

        NotificationService notificationService =
                new TelegramNotificationService();

        TicketService ticketService =
                new TicketService(notificationService, repository);

        // Создание и сохранение заявки
        Ticket ticket = new Ticket(
                1,
                "Не работает Wi-Fi",
                "Нет подключения к беспроводной сети",
                TicketPriority.HIGH
        );

        ticket = ticketService.createTicket(customer, ticket);

        System.out.println("\n=== ОБРАБОТКА ЗАЯВКИ ===");
        System.out.println(
                "Заявка #" + ticket.getId() + ": "
                        + ticket.getTitle() + " | " + ticket.getStatus()
        );

        // Назначение специалиста
        ticketService.assignTicket(agent, ticket, agent);
        System.out.println("Назначен специалист: " + agent.getName());

        // Изменение статусов и уведомления
        ticketService.startTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());

        ticketService.resolveTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());

        ticketService.closeTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());

        // Добавление комментария в БД
        TicketCommentRepository commentRepository =
                new JdbcTicketCommentRepository();

        TicketComment comment = new TicketComment(
                ticket.getId(),
                customer.getId(),
                "Проверка комментария клиента",
                false
        );

        comment = commentRepository.save(comment);

        System.out.println("\n=== КОММЕНТАРИИ ===");
        for (TicketComment savedComment :
                commentRepository.findByTicketId(ticket.getId())) {
            System.out.println(
                    "#" + savedComment.getId()
                            + " | Автор ID: " + savedComment.getAuthorId()
                            + " | " + savedComment.getText()
            );
        }

        // История изменения статусов из БД
        System.out.println("\n=== ИСТОРИЯ СТАТУСОВ ===");
        for (StatusHistoryEntry entry :
                repository.findStatusHistoryByTicketId(ticket.getId())) {
            System.out.println(entry);
        }

        // Полный список заявок из БД
        System.out.println("\n=== ПОЛНЫЙ СПИСОК ЗАЯВОК ===");
        for (Ticket savedTicket : repository.findAll()) {
            System.out.println(
                    "Заявка #" + savedTicket.getId()
                            + ": " + savedTicket.getTitle()
                            + " | " + savedTicket.getStatus()
            );
        }

        // Заявки с именами клиентов и специалистов через JOIN
        System.out.println("\n=== ЗАЯВКИ С ПОДРОБНОЙ ИНФОРМАЦИЕЙ (JOIN) ===");
        for (TicketDetails details : repository.findAllWithDetails()) {
            System.out.println(details);
        }
    }
}
