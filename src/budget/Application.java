package budget;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;


public class Application implements Runnable {
    private final Account account;
    private final Scanner scanner;
    private final static String purchasePath = "src/budget/purchases";

    public Application(Account account) {
        this.account = account;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {
        loadPurchases();
        Menu mainMenu = getMainMenu();
        mainMenu.run();
        finish();
    }

    private Menu getMainMenu() {
        return new Menu("Choose your action:")
                .add("Add income", this::addIncome)
                .add("Add purchase", getCategoryMenu(this::addPurchase, false))
                .add("Show list of purchases",
                        getCategoryMenu((category -> showTotalPurchases(null, category)), true))
                .add("Balance", this::showBalance)
                .add("Save", this::savePurchases)
                .add("Load", this::loadPurchases)
                .add("Analyze (Sort)", getAnalyzeMenu())
                .addExit();
    }

    private Menu getAnalyzeMenu() {
        Menu menuCertainType = getCategoryMenu(category -> {
            List<Product> allSorted = getSortedPurchasesByPrice();
            showTotalPurchases(allSorted, category);
        }, false);

        return new Menu("How do you want to sort?")
                .add("Sort All purchases", () -> {
                    List<Product> allSorted = getSortedPurchasesByPrice();
                    showTotalPurchases(allSorted, null);
                })
                .add("Sort by type", this::showSortTypes)
                .add("Sort by certain type", menuCertainType)
                .addBack();
    }

    private void showSortTypes() {
        List<Product.Category> categories = Arrays.asList(Product.Category.values());
        categories.stream()
                .map(category -> {
                    BigDecimal sum = account.getPurchaseAmountByCategory(category);
                    return Map.entry(category.getName(), sum);
                })
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> System.out.printf("%s - $%s\n", entry.getKey(), entry.getValue()));
        System.out.println("Total sum: $" + account.getPurchaseAmount());
    }


    private void addIncome() {
        boolean isCorrectIncome;
        do {
            try {
                System.out.println("Enter income:");
                String income = scanner.nextLine();
                BigDecimal decIncome = new BigDecimal("-1");
                try {
                    decIncome = new BigDecimal(income);
                } catch (NumberFormatException ignored) {
                }

                account.addIncome(decIncome);
                System.out.println("Income was added!");
                isCorrectIncome = true;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                isCorrectIncome = false;
            }
        } while (!isCorrectIncome);
    }

    private Menu getCategoryMenu(final Consumer<Product.Category> action, final boolean isAll) {
        Menu menu = new Menu("Choose your type of purchase:");
        Arrays.stream(Product.Category.values())
                .forEach(category -> menu.add(category.getName(), () -> action.accept(category)));
        if (isAll) {
            menu.add("All", () -> action.accept(null));
        }
        menu.addBack();
        return menu;
    }

    private void addPurchase(final Product.Category category) {
        boolean isCorrect;
        do {
            try {
                System.out.println("Enter purchase name:");
                String name = scanner.nextLine();
                System.out.println("Enter its price:");
                String price = scanner.nextLine();
                BigDecimal decPrice = new BigDecimal("-1");
                try {
                    decPrice = new BigDecimal(price);
                } catch (NumberFormatException ignored) {
                }

                Product product = new Product(name, decPrice, category);
                account.addPurchase(product);
                System.out.println("Purchase was added!");
                isCorrect = true;
            } catch (IllegalArgumentException e) {
                System.out.println("\n" + e.getMessage() + "\n");
                isCorrect = false;
            }
        } while (!isCorrect);
    }

    private void showTotalPurchases(List<Product> productList, final Product.Category category) {
        System.out.printf("%s:\n", isNull(category) ? "All" : category.getName());
        productList = isNull(productList) ? account.getHistory() : productList;
        productList.stream()
                .filter(product -> product.getCategory() == category || isNull(category))
                .peek(System.out::println)
                .map(Product::getPrice)
                .reduce(BigDecimal::add)
                .ifPresentOrElse((total) -> System.out.printf("Total: $%s\n", total),
                        this::showListEmpty);
    }

    private void showBalance() {
        System.out.printf("Balance: $%s\n", account.getBalance());
    }

    private void showListEmpty() {
        System.out.println("Purchase list is empty");
    }

    private void loadPurchases() {
        try {
            Scanner scanner = new Scanner(new File(purchasePath));
            String firstLine = scanner.nextLine();
            BigDecimal balance = new BigDecimal("-1");
            try {
                balance = new BigDecimal(firstLine);
            } catch (NumberFormatException ignored) {
            }
            if (scanner.hasNextLine()) {
                account.setBalance(balance);
            }
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(";");
                String name = data[0];
                BigDecimal price = new BigDecimal("-1");
                try {
                    price = new BigDecimal(data[1]);
                } catch (NumberFormatException ignored) {
                }
                Product.Category category = Product.Category.valueOf(data[2].toUpperCase());
                account.addPurchase(new Product(
                        name,
                        price,
                        category)
                );
            }
            scanner.close();
            System.out.println("Purchases were loaded!");
        } catch (Exception e) {
            System.out.println("Error loading the file purchases");
            System.out.println(e.getMessage());
        }
        System.out.println();
    }

    private void savePurchases() {
        try {
            PrintWriter writer = new PrintWriter(purchasePath);
            writer.println(account.getBalance().add(account.getPurchaseAmount()));
            account.getHistory()
                    .forEach(product -> {
                        String productString = String.format("%s;%s;%s", product.getName(),
                                product.getPrice(), product.getCategory().getName());
                        writer.println(productString);
                    });
            writer.close();
            System.out.println("Purchases were saved!");
        } catch (Exception e) {
            System.out.println("Error saving the file purchases");
        }
        System.out.println();
    }

    private void finish() {
        System.out.println("Bye!");
        scanner.close();
    }

    private List<Product> getSortedPurchasesByPrice() {
        return account.getHistory().stream()
                .sorted(Comparator.comparing(Product::getPrice).reversed())
                .collect(Collectors.toList());
    }
}
