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
                "Сергей Иванов",
                "sergey@xxx.ru"
        );

        System.out.println("Клиент: " + customer.getName());
        System.out.println("Специалист: " + agent.getName());

        TicketRepository repository = new TicketRepository();

        NotificationService notificationService =
                new TelegramNotificationService();

        Ticket ticket = new Ticket(
                1,
                "Не работает Wi-Fi",
                "Нет подключения к беспроводной сети",
                TicketPriority.HIGH
        );

        System.out.println(
                "Заявка #" + ticket.getId() + ": "
                        + ticket.getTitle() + " | " + ticket.getStatus()
        );
        System.out.println(ticket);
        TicketService ticketService =
                new TicketService(notificationService, repository);
        ticketService.startTicket(agent, ticket);


        System.out.println("Статус: " + ticket.getStatus());
        ticketService.resolveTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());


        ticketService.closeTicket(agent, ticket);
        System.out.println("Статус: " + ticket.getStatus());

        System.out.println("История статусов:");

        for (StatusHistoryEntry entry : ticket.getStatusHistory()) {
            System.out.println(entry);
        }

        repository.add(ticket);
        repository.add(new Ticket(
                2,
                "Не запускается приложение",
                "При запуске появляется ошибка"
        ));

        System.out.println("Поиск заявки с ID 1:");
        System.out.println(ticketService.getTicketById(1));

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