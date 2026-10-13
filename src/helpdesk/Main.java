package helpdesk;

import helpdesk.model.Ticket;
import helpdesk.model.Customer;
import helpdesk.model.SupportAgent;
import helpdesk.service.NotificationService;
import helpdesk.service.ConsoleNotificationService;
import helpdesk.service.TicketService;
import helpdesk.repository.TicketRepository;

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
                new ConsoleNotificationService();
        Ticket ticket = new Ticket(
                1,
                "Не работает Wi-Fi",
                "Нет подключения к беспроводной сети"
        );

        System.out.println(
                "Заявка #" + ticket.getId() + ": "
                        + ticket.getTitle() + " | " + ticket.getStatus()
        );

        TicketService ticketService = new TicketService(notificationService);
        ticketService.startTicket(ticket);


        System.out.println("Статус: " + ticket.getStatus());
        ticketService.resolveTicket(ticket);
        System.out.println("Статус: " + ticket.getStatus());


        ticketService.closeTicket(ticket);
        System.out.println("Статус: " + ticket.getStatus());

        repository.add(ticket);
        repository.add(new Ticket(
                2,
                "Не запускается приложение",
                "При запуске появляется ошибка"
        ));

        System.out.println("Полный список заявок:");
        for (Ticket t : repository.findAll()) {
            System.out.println(
                    "Заявка #" + t.getId() + ": "
                            + t.getTitle() + " | " + t.getStatus()
            );
        }

    }
}