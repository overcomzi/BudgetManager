package budget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Menu implements Runnable {
    private static Scanner scanner = new Scanner(System.in);

    private final Map<String, MenuItem> menuItems = new LinkedHashMap<>();

    private final String title;
    private boolean once = false;
    private String format = "%s) %s\n";

    public Menu(final String title) {
        this.title = title;
    }

    public Menu once() {
        once = true;
        return this;
    }

    public void setFormat(String pattern) {
        this.format = pattern;
    }

    public Menu add(String key, String name, Runnable action) {
        menuItems.put(key, new MenuItem(name, action));
        return this;
    }

    public Menu add(String name, Runnable action) {
        return add(String.valueOf(menuItems.size() + 1), name, action);
    }

    public Menu addExit() {
        return add("0", "Exit", () -> {
            scanner.close();
            this.once();
        });
    }

    public Menu addBack() {
        return add("Back", this::once);
    }

    @Override
    public void run() {
        setOnce();
        do {
            System.out.println(title);
            menuItems.forEach((key, menuItem) -> System.out.printf(format, key, menuItem));
            final String inputKey = scanner.nextLine().toLowerCase();
            MenuItem menuItem = menuItems.getOrDefault(inputKey, new MenuItem("Undefined action", this::printMessage));
            if (!menuItem.getName().equals("Back")) {
                System.out.println();
            }
            menuItem.run();
            if (!once) {
                System.out.println();
            }
        } while (!once);
    }

    private void setOnce() {
        this.once = false;
    }

    private void printMessage() {
        if (menuItems.containsKey("0")) {
            System.out.println(String.format("Please enter the number from 0 to %d", menuItems.size() - 1));
        } else {
            System.out.println(String.format("Please enter the number from 1 to %d", menuItems.size()));
        }
    }

    private final static class MenuItem implements Runnable {
        String name;
        Runnable action;

        public MenuItem(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public void run() {
            action.run();
        }

        public String getName() {
            return name;
        }

        public Runnable getAction() {
            return action;
        }
    }
}
