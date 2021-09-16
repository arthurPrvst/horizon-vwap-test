package hsoft;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import com.hsoft.codingtest.MarketDataListener;
import com.hsoft.codingtest.impl.TestDataProvider;
import hsoft.models.Execution;
import hsoft.services.DataListenerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class WrongDataTest {

    @Spy
    private DataProvider provider = DataProviderFactory.getDataProvider();

    @InjectMocks
    private DataListenerService handler = new DataListenerService();
    private final String ISIN_TEST = "ISIN_TEST";

    @ParameterizedTest
    @MethodSource("executionFeedSource")
    void testIncorrectExecutionsFeed(Execution exec) {
        doAnswer((Answer<Object>) a -> {
            Field field = TestDataProvider.class.getDeclaredField("marketDataListeners");
            field.setAccessible(true);
            List<MarketDataListener> listeners = (List<MarketDataListener>) field.get(provider);
            listeners.get(0).transactionOccured(ISIN_TEST, exec.getQuantity(), exec.getPrice());
            return null;
        }).when(provider).listen();

        handler.listen();

        if (exec.getQuantity() <= 0 || exec.getPrice() <= 0) {
            Assertions.assertTrue(handler.getExecutionRecords().isEmpty());
        }
    }

    static Stream<Execution> executionFeedSource() {
        Execution e1 = new Execution(1, 2);
        Execution e2 = new Execution(-1, 2);
        Execution e3 = new Execution(1, 0);
        return Stream.of(e1, e2, e3);
    }

}