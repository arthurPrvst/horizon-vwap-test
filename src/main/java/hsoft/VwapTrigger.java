package hsoft;

import hsoft.services.DataListenerService;
import org.apache.log4j.LogManager;

public class VwapTrigger {

  public static void main(String[] args) {

    DataListenerService handler = new DataListenerService();
    handler.listen();
    // When this method returns, the test is finished and you can check your results in the console
    LogManager.shutdown();
  }

}