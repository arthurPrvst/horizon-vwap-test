package hsoft;

import com.hsoft.codingtest.DataProvider;
import com.hsoft.codingtest.DataProviderFactory;
import com.hsoft.codingtest.PricingDataListener;
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
public class FairValueTest {

    @Spy
    private DataProvider provider = DataProviderFactory.getDataProvider();

    @InjectMocks
    private DataListenerService handler = new DataListenerService();

    private final String ISIN_TEST = "ISIN_TEST";

    @Test
    void shouldUpdateFairValue() {
        doAnswer((Answer<Object>) a -> {
            Field field = TestDataProvider.class.getDeclaredField("pricingDataListeners");
            field.setAccessible(true);
            List<PricingDataListener> listeners = (List<PricingDataListener>) field.get(provider);
            listeners.get(0).fairValueChanged(ISIN_TEST, 66D);
            return null;
        }).when(provider).listen();

        handler.listen();

        // Verify fairValue data
        Assertions.assertTrue(handler.getFairValues().containsKey(ISIN_TEST));
        Assertions.assertEquals(0, BigDecimal.valueOf(66).compareTo(handler.getFairValues().get(ISIN_TEST)));
    }

}