package helpdesk;

import helpdesk.model.Ticket;
import helpdesk.model.Customer;
import helpdesk.model.SupportAgent;
import helpdesk.service.NotificationService;
import helpdesk.service.ConsoleNotificationService;
import helpdesk.service.TicketService;
import helpdesk.repository.TicketRepository;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.service.TelegramNotificationService;
import helpdesk.model.StatusHistoryEntry;
import helpdesk.model.InvalidTicketStateException;
import helpdesk.model.TicketNotFoundException;
import helpdesk.repository.jdbc.JdbcTicketRepository;
import helpdesk.model.TicketComment;
import helpdesk.repository.TicketCommentRepository;
import helpdesk.repository.jdbc.JdbcTicketCommentRepository;
import helpdesk.model.TicketDetails;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== HELP DESK ===");

        Customer customer = new Customer(
                1,
                "Анна Петрова",
                "anna@xxx.ru"
        );

        SupportAgent agent = new SupportAgent(
                2,
                "Иван Соколов",
                "ivan.support@xxx.ru"
        );

        System.out.println("Клиент: " + customer.getName());
        System.out.println("Специалист: " + agent.getName());

        TicketRepository repository = new JdbcTicketRepository();

        NotificationService notificationService =
                new TelegramNotificationService();
        TicketService ticketService =
                new TicketService(notificationService, repository);

        Ticket ticket = new Ticket(
                1,
                "Не работает Wi-Fi",
                "Нет подключения к беспроводной сети",
                TicketPriority.HIGH
        );
        ticket = ticketService.createTicket(customer, ticket);
        TicketCommentRepository commentRepository =
                new JdbcTicketCommentRepository();

        TicketComment comment = new TicketComment(
                ticket.getId(),
                customer.getId(),
                "Проверка комментария клиента",
                false
        );

        comment = commentRepository.save(comment);

        System.out.println(
                "Сохранён комментарий #" + comment.getId()
                        + ": " + comment.getText()
        );

        System.out.println("Комментарии заявки:");

        for (TicketComment savedComment :
                commentRepository.findByTicketId(ticket.getId())) {
            System.out.println(
                    "#" + savedComment.getId()
                            + " | Автор ID: " + savedComment.getAuthorId()
                            + " | Внутренний: " + savedComment.isInternal()
                            + " | " + savedComment.getText()
            );
        }
        ticketService.assignTicket(agent, ticket, agent);

        System.out.println(
                "На заявку назначен специалист: " + agent.getName()
        );

        System.out.println(
                "Заявка #" + ticket.getId() + ": "
                        + ticket.getTitle() + " | " + ticket.getStatus()
        );
        System.out.println(ticket);

        ticketService.startTicket(agent, ticket);


        System.out.println("Статус: " + ticket.getStatus());
        ticketService.resolveTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());


        ticketService.closeTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());


        System.out.println("История статусов из DB:");

        for (StatusHistoryEntry entry :
                repository.findStatusHistoryByTicketId(ticket.getId())) {
            System.out.println(entry);
        }

        Ticket secondTicket = new Ticket(
                2,
                "Не запускается приложение",
                "При запуске появляется ошибка"
        );

        secondTicket = ticketService.createTicket(customer, secondTicket);

        System.out.println("Поиск заявки с ID " + ticket.getId() + ":");
        System.out.println(ticketService.getTicketById(ticket.getId()));

        try {
            ticketService.getTicketById(999);
        } catch (TicketNotFoundException e) {
            System.out.println("Ошибка поиска: " + e.getMessage());
        }

        System.out.println("Полный список заявок:");
        for (Ticket t : repository.findAll()) {
            System.out.println(
                    "Заявка #" + t.getId() + ": "
                            + t.getTitle() + " | " + t.getStatus()
            );
        }

        System.out.println("Заявки с подробной информацией (JOIN):");

        for (TicketDetails details : repository.findAllWithDetails()) {
            System.out.println(details);
        }


        System.out.println("Статистика по статусам:");

        for (TicketStatus status : TicketStatus.values()) {
            int count = 0;

            for (Ticket t : repository.findAll()) {
                if (t.getStatus() == status) {
                    count++;
                }
            }

            System.out.println(status + ": " + count);
        }

        System.out.println("Статистика по приоритетам:");

        for (TicketPriority priority : TicketPriority.values()) {
            int count = 0;

            for (Ticket t : repository.findAll()) {
                if (t.getPriority() == priority) {
                    count++;
                }
            }

            System.out.println(priority + ": " + count);
        }



        Ticket testTicket = new Ticket(
                3,
                "Проверка отмены",
                "Тестирование статуса"
        );

        testTicket.startProcessing();
        testTicket.resolve();
        testTicket.close();
        try {
            ticket.cancel();
        } catch (InvalidTicketStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }

        System.out.println("Статус после попытки отмены: " + testTicket.getStatus());

        System.out.println("Новые заявки:");

        for (Ticket t : repository.findByStatus(TicketStatus.NEW)) {
            System.out.println(t);
        }

        Ticket accessTestTicket = new Ticket(
                4,
                "Проверка доступа",
                "Клиент пытается начать обработку"
        );

        try {
            ticketService.startTicket(customer, accessTestTicket);
        } catch (SecurityException e) {
            System.out.println("Доступ запрещён: " + e.getMessage());
        }

        System.out.println(
                "Статус после попытки клиента: "
                        + accessTestTicket.getStatus()
        );
    }


}