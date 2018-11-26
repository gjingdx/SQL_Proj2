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
            opLeft = new JoinOperator(ops, op.getPlainSelect(), false);
        }
        return (JoinOperator)opLeft;
    }

    public JoinOperator seperateLastTable () {
        if (op.getChildren().size() == 2) {
            return new JoinOperator(op.getChildren(), op.getPlainSelect(), false);
        }

        Operator opRight = op.getChildren().get(op.getChildren().size() - 1);
        List<Operator> ops = new ArrayList<>(op.getChildren());
        ops.remove(ops.size() - 1);
        Operator opLeft = new JoinOperator(ops, op.getPlainSelect(), false);

        List<Operator> retOps = new ArrayList<>();
        retOps.add(opLeft);
        retOps.add(opRight);
        return new JoinOperator(retOps, op.getPlainSelect(), false);
    }
}
