package ru.ranepa.presentation;

import ru.ranepa.model.Employee;
import ru.ranepa.service.HRMService;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner;
    private final HRMService service;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Menu(HRMService service) {
        this.service = service;
        // Переключение на кодировку UTF-8, чтоб можно было использовать русский язык
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
            System.setErr(new PrintStream(System.err, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // если вдруг не получилось — ничего страшного
        }

        this.scanner = new Scanner(System.in, "UTF-8");
    }

    //бесконечный цикл
    public void start() {
        while (true) {
            printMainMenu();
            int choice = readIntInput("Выберите действие: ");

            switch (choice) {
                case 1 -> showAllEmployees();
                case 2 -> addEmployee();
                case 3 -> deleteEmployee();
                case 4 -> findEmployeeById();
                case 5 -> showStatistics();
                case 6 -> filterByPosition();
                case 7 -> showEmployeesSortedByDate();
                case 0 -> {
                    System.out.println("До свидания!");
                    return;
                }
                default -> System.out.println("Пожалуйста, выберите значение от 0 до 7");
            }
        }
    }

    //пункты меню
    private void printMainMenu() {
        System.out.println("\nМеню");
        System.out.println("1. Показать всех сотрудников");
        System.out.println("2. Добавить сотрудника");
        System.out.println("3. Удалить сотрудника");
        System.out.println("4. Найти сотрудника по ID");
        System.out.println("5. Статистика по компании");
        System.out.println("6. Фильтр по должности");
        System.out.println("7. Сортировка сотрудников по дате приёма на работу");
        System.out.println("0. Выход");
    }

    private void showAllEmployees() {
        List<Employee> employees = service.getAllEmployees();
        if (employees.isEmpty()) {
            System.out.println("В системе не созданны сотрудники");
            return;
        }

        System.out.printf("%-5s %-22s %-20s %-12s %-15s%n",
                "ID", "Name", "Position", "Salary", "Hire Date");

        for (Employee e : employees) {
            System.out.printf("%-5d %-22s %-20s %-12.0f %-15s%n",
                    e.getId(),
                    truncateString(e.getName(), 22),
                    truncateString(e.getPosition(), 20),
                    e.getSalary(),
                    e.getHireDate().format(dateFormatter));
        }
        System.out.println("Всего сотруников: " + employees.size());
    }

    private void showEmployeesSortedByDate() {
        List<Employee> employees = service.getAllEmployeesSortedByHireDate();
        if (employees.isEmpty()) {
            System.out.println("В системе не созданны сотрудники");
            return;
        }

        System.out.printf("%-5s %-22s %-20s %-12s %-15s%n",
                "ID", "Name", "Position", "Salary", "Hire Date");

        for (Employee e : employees) {
            System.out.printf("%-5d %-22s %-20s %-12.0f %-15s%n",
                    e.getId(),
                    truncateString(e.getName(), 22),
                    truncateString(e.getPosition(), 20),
                    e.getSalary(),
                    e.getHireDate().format(dateFormatter));
        }
        System.out.println("Всего сотруников: " + employees.size());
    }

    private void addEmployee() {
        String name = readStringInput("ФИО: ");
        String position = readStringInput("Должность: ");
        double salary = readDoubleInput("Зарплата: ");
        LocalDate hireDate = readDateInput("Дата трудоустройства: ");

        Employee employee = new Employee(null, name, position, BigDecimal.valueOf(salary), hireDate);
        Employee saved = service.addEmployee(employee);

        System.out.printf("Сотрудник добавлен с ID: %d%n", saved.getId());
    }

    private void deleteEmployee() {
        Long id = readLongInput("ID сотрудника для удаления: ");

        if (service.deleteEmployee(id)) {
            System.out.println("Сотрудник с ID " + id + " удалён");
        } else {
            System.out.println("Сотрудник с ID " + id + " не найден");
        }
    }

    private void findEmployeeById() {
        Long id = readLongInput("ID сотрудник, которого необходимо найти: ");

        Optional<Employee> employee = service.findById(id);
        if (employee.isPresent()) {
            System.out.println("\nСотрудник найден:");
            System.out.println(employee.get());
        } else {
            System.out.println("Не удалось найти сотрудника с ID " + id);
        }
    }

    private void showStatistics() {
        System.out.println("\nСводная статистика по компании");
        List<Employee> employees = service.getAllEmployees();

        if (employees.isEmpty()) {
            System.out.println("Нет данных для формирования статистики. Нет сотрудников");
            return;
        }

        double avgSalary = service.getAverageSalary();
        System.out.printf("Средняя ЗП по компании: %.0f rub.%n", avgSalary);

        Optional<Employee> topEmployee = service.findHighestPaidEmployee();
        if (topEmployee.isPresent()) {
            Employee top = topEmployee.get();
            System.out.println("Самая высокая ЗП по компании:");
            System.out.println("   " + top);
        }
    }

    private void filterByPosition() {
        String position = readStringInput("Введите должность для посика: ");

        List<Employee> filtered = service.filterByPosition(position);
        if (filtered.isEmpty()) {
            System.out.println("Не найдены сотрудники с должностью '" + position);
            return;
        }

        System.out.println("\nСотрудники найдены: " + filtered.size());
        for (Employee e : filtered) {
            System.out.println(e);
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }
    }

    private long readLongInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите действительный номер");
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите действительное число (используйте точку в качестве разделителя).");
            }
        }
    }

    private String readStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private LocalDate readDateInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(scanner.nextLine().trim(), dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: введите дату в формате ДД.ММ.ГГГГ");
            }
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}