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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ExecutionsTest {

    @Spy
    private DataProvider provider = DataProviderFactory.getDataProvider();

    @InjectMocks
    private DataListenerService handler = new DataListenerService();

    private final String ISIN_TEST = "ISIN_TEST";
    private final int MAX_RECORDS = 5;

    @Test
    void shouldDiscardOldExecutionsAndMultiThread() {
        doAnswer((Answer<Object>) a -> {
            Field field = TestDataProvider.class.getDeclaredField("marketDataListeners");
            field.setAccessible(true);
            List<MarketDataListener> listeners = (List<MarketDataListener>) field.get(provider);
            MarketDataListener listener = listeners.get(0);

            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                futures.add(CompletableFuture
                        .runAsync(() -> listener.transactionOccured(ISIN_TEST, 2, 55D), executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return null;
        }).when(provider).listen();

        handler.listen();

        // Verify nb of executions
        Assertions.assertEquals(MAX_RECORDS, handler.getExecutionRecords().get(ISIN_TEST).getExecutions().size());
    }

}