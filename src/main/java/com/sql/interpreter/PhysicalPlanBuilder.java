package com.sql.interpreter;

import logical.operator.Operator;
import logical.operator.ProjectOperator;

public class PhysicalPlanBuilder {

    //private LogicalPlanBuilder logicalPB;
    private Operator operator;


    public void visit(ProjectOperator projectOp) {
        Operator[] children = this.operator.getChildren();
        children[0].accept(this);

    }


}
