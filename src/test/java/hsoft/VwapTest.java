package hsoft;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import com.hsoft.codingtest.MarketDataListener;
import com.hsoft.codingtest.impl.TestDataProvider;
import hsoft.services.DataListenerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class VwapTest {

    @Spy
    private DataProvider provider = DataProviderFactory.getDataProvider();

    @InjectMocks
    private DataListenerService handler = new DataListenerService();

    private final String ISIN_TEST = "ISIN_TEST";

    @Test
    void shouldUpdateVwap() {
        doAnswer((Answer<Object>) a -> {
            Field field = TestDataProvider.class.getDeclaredField("marketDataListeners");
            field.setAccessible(true);
            List<MarketDataListener> listeners = (List<MarketDataListener>) field.get(provider);
            listeners.get(0).transactionOccured(ISIN_TEST, 2, 55D);
            return null;
        }).when(provider).listen();

        handler.listen();

        // Verify execution data
        Assertions.assertEquals(1, handler.getExecutionRecords().size());
        // Verify quantity
        Assertions.assertEquals(2, handler.getExecutionRecords()
                .get(ISIN_TEST).getExecutions().getFirst().getQuantity());
        // Verify price
        Assertions.assertEquals(55, handler.getExecutionRecords()
                .get(ISIN_TEST).getExecutions().getFirst().getPrice());
        // Verify amount
        Assertions.assertEquals(0, BigDecimal.valueOf(110).compareTo(handler.getExecutionRecords()
                .get(ISIN_TEST).getExecutions().getFirst().getAmount()));
    }

}