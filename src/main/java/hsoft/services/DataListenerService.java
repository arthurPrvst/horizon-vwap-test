package hsoft.services;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import hsoft.models.Execution;
import hsoft.models.ExecutionRecords;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Getter
public class DataListenerService {
    private static final Logger LOGGER = Logger.getLogger(DataListenerService.class);
    private static final int PRECISION = 12;
    private DataProvider provider = DataProviderFactory.getDataProvider();
    private final Predicate<String> isTestProduct = "TEST_PRODUCT"::equals;

    private final Map<String, BigDecimal> fairValues = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> vwaps = new ConcurrentHashMap<>();
    private final Map<String, ExecutionRecords> executionRecords = new HashMap<>();

    /**
     * Start listening from feeds
     */
    public void listen() {
        this.registerMarketDataListener();
        this.registerFairValueListener();
        provider.listen();
    }

    /**
     * Register listener to the MarketData feed
     */
    private void registerMarketDataListener() {
        provider.addMarketDataListener((productId, quantity, price) -> {
            if (StringUtils.isBlank(productId) || quantity <= 0 || price <= 0) {
                LOGGER.warn("Received incorrect data. Quantity=" + quantity + ", price=" + price);
                return;
            }

            Execution lastExec = new Execution(quantity, price);

            synchronized (executionRecords) {
                ExecutionRecords productRecord = executionRecords.compute(productId, (k, v) -> {
                    if (v == null) {
                        return new ExecutionRecords(lastExec);
                    } else {
                        v.addExecution(lastExec);
                        return v;
                    }
                });

                // Calculate VWAP
                BigDecimal totalAmount = productRecord.getExecutions()
                        .stream()
                        .map(Execution::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalQty = productRecord.getExecutions()
                        .stream()
                        .map(e -> BigDecimal.valueOf(e.getQuantity()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal currentVwap = totalAmount.divide(totalQty, PRECISION, RoundingMode.HALF_EVEN);
                vwaps.put(productId, currentVwap);
                BigDecimal fairValue = fairValues.getOrDefault(productId, BigDecimal.ZERO);

                // Logging if needed
                if (isVwapHigherThanFairValue(currentVwap, fairValue))
                    logVwap(productId, currentVwap, fairValue);
            }
        });
    }

    /**
     * Register listener to the FairValue feed
     */
    private void registerFairValueListener() {
        provider.addPricingDataListener((productId, fairValue) -> {

            if (StringUtils.isBlank(productId) || fairValue < 0) {
                LOGGER.warn("Received an incorrect productId");
                return;
            }

            BigDecimal fairValueBd = BigDecimal.valueOf(fairValue);
            fairValues.put(productId, fairValueBd);
            BigDecimal currentVwap = vwaps.getOrDefault(productId, BigDecimal.ZERO);

            if (isVwapHigherThanFairValue(currentVwap, fairValueBd))
                logVwap(productId, currentVwap, fairValueBd);
        });
    }

    /**
     * Check if the calculated vwap is higher than fairValue received
     * @param currentVwap : vwap previously calculated
     * @param currentFairValue : last fair value received from the fairValue feed
     * @return vwap > fairValue
     */
    private boolean isVwapHigherThanFairValue(BigDecimal currentVwap, BigDecimal currentFairValue) {
        return currentVwap.compareTo(BigDecimal.ZERO) > 0 && currentVwap.compareTo(currentFairValue) > 0;
    }

    /**
     * Log vwap and fairValue
     * @param productId : product received from MarketFeed
     * @param currentVwap : vwap previously calculated
     * @param currentFairValue : last fair value received from the fairValue feed
     */
    private void logVwap(String productId, BigDecimal currentVwap, BigDecimal currentFairValue) {
        if (isTestProduct.test(productId)) {
            LOGGER.debug("VWAP (" + currentVwap + ") > FairValue (" + currentFairValue + ")");
        } else {
            LOGGER.info("VWAP (" + currentVwap + ") > FairValue (" + currentFairValue + ")");
        }
    }
}
