package PlanBuilder;

import logical.operator.JoinOperator;
import logical.operator.Operator;

import java.util.ArrayList;
import java.util.List;

public class JoinPlanBuilderHelper {
    JoinOperator op;
    public JoinPlanBuilderHelper (JoinOperator op) {
        this.op = op;
    }

    public JoinOperator rebuildLogicalTree() {
        Operator opLeft =  op.getChildren().get(0);
        for (int i = 1; i < op.getChildren().size(); i++) {
            Operator opRight = op.getChildren().get(i);
            List<Operator> ops = new ArrayList<>();
            ops.add(opLeft);
            ops.add(opRight);
            opLeft = new JoinOperator(ops, op.getPlainSelect());
        }
        return (JoinOperator)opLeft;
    }
}
