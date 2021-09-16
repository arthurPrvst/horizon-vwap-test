package hsoft.models;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Getter
@NoArgsConstructor
public class ExecutionRecords {
    private static final int MAX_RECORDS = 5;
    private final LinkedList<Execution> executions = new LinkedList<>();

    public ExecutionRecords(Execution exec) {
        this.addExecution(exec);
    }

    /**
     * Add last execution on a given product
     * @param exec : last Execution received from the market feed
     */
    public void addExecution(Execution exec) {
        executions.addFirst(exec);
        if (executions.size() > MAX_RECORDS) {
            executions.removeLast();
        }
    }
}
