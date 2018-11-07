# Query Plan
    The query plan is a tree of operators. The root operator will be the entry operator. The plan builder is seperated into logical plan builder and physical plan builder. The former servies for optimal solutions and the latter will implement the operator accroding to the config. For example, we have join operator in logical tree. In physical tree, it might be TNLJ, BNLJ or SMJ.
    
## Logical Query Plan
```logical/interpreter/LogicalPlanBuilder```
  
- After each scan operation, we implement a select operation if there exists related expression in where section.
- Then we implement join operation if exists. The detailed join operation will be illustrated below.
- After all tables are joined, we implement the projection, sort and distinct operation in order if they exist in the query statement.

    e.g.
    ```
    SELECT DISTINCT S.A, B.D 
    FROM Sailors As S, Reserves As R, Boats As B 
    WHERE R.H = B.D and S.A = R.G and B.D = 101 
    ORDER BY S.A

                                 Distinct
                                    |
                                  Order
                                    |
                                 Project
                                    |
                                Join with B
                    (R.H = B.D and S.A = R.G and B.D = 101)
                                /        \
                            Join S, R    select
                           (S.A = R.G)   (B.D = 101)
                           /        \       |
                        Scan S    Scan R   scan B
                                      
    ```
    When join with B, we take the whole expression as the join condition, instead of ```S.A = R.G```, since there will be no difference to I/Os.

## Physical Plan Builder
```com/sql/interpreter/PhysicalPlanBuilder```  
  
  The Physical Plan Builder is the class that builds a physical operator tree using the logical operator tree via visitor pattern. 
    The Physical PB Class has 6 visit field functions that each has a type of logical operator as the parameter. Correspondingly, 
    each logical operator has an accept function with a Physical Plan Builder instance as the parameter and inside the accept function, 
    the physical plan builder visits the operator.  
      
   The visit functions traverse the logical operator tree recursively in a way the depth-first search traverses a tree. 
    In each visit function, the child or children of the parameter logical operator will be visited in the way that the logical operator accepts the Physical Plan Builder instance it self;
    then a instance of the physical operator corresponding to the logical operator will be created using its physical operator child/children created before and stored in a stack (Deque in Java).
     The physical operator instance would then be pushed into the stack. In this way, the physical operator tree is built. 