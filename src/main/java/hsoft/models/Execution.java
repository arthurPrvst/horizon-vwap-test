package hsoft.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Execution {
    private long quantity;
    private double price;
    private BigDecimal amount;

    public Execution(long qty, double price) {
        this.quantity = qty;
        this.price = price;
        this.amount = BigDecimal.valueOf(qty).multiply(BigDecimal.valueOf(price));
    }
}
