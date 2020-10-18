package budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

public class Account {
    private final List<Product> history;
    private BigDecimal balance;

    public Account() {
        history = new LinkedList<>();
        balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
    }

    public void addIncome(final BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= -1) {
            throw new IllegalArgumentException("Income must be a non-negative number");
        }
        balance = balance.add(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(final BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) <= -1) {
            throw new IllegalArgumentException("Balance must be a non-negative number");
        }
        this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
    }

    public void addPurchase(final Product product) {
        history.add(product);
        balance = balance.subtract(product.getPrice());
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
    }

    public List<Product> getHistory() {
        return history;
    }

    public BigDecimal getPurchaseAmountByCategory(Product.Category category) {
        return history.stream()
                .filter((product) -> product.getCategory() == category)
                .map(Product::getPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getPurchaseAmount() {
        return history.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
}
