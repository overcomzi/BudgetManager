package budget;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product {
    private final String name;
    private final BigDecimal price;
    private final Category category;

    public Category getCategory() {
        return category;
    }

    public Product(final String name, final BigDecimal price, final Category category) {
        this.name = name;
        if (price.compareTo(BigDecimal.ZERO) <= -1) {
            throw new IllegalArgumentException("Price of purchase must be a non-negative number");
        }
        this.price = price.setScale(2, RoundingMode.HALF_EVEN);
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("%s $%s", name, price.toString());
    }

    public enum Category {
        FOOD("Food"),
        CLOTHES("Clothes"),
        ENTERTAINMENT("Entertainment"),
        OTHER("Other");

        private String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
