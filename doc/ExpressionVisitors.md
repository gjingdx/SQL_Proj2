# Expression Visitors

## Join Expression Visitor
- **How to evaluate a Join condition**
    1. Extract the related expression (Join condition), according to the current schema. We will demonstrate how to do this and some examples as below.
    2. Use the refined expression to accept the SelectExpressionVisitor to make the evaluation.
   
   Thus, the Join Expression Visitor only targets to realize step 1.

- **Rules**  
    1. We call an expression valid as they only use the column appeared in the current schema.
    2. For a ```AndExpression```:  
     If both the right and left expressions in stack are valid, ```left and right``` will be seem as valid.   
     If only either one is valid, we will return this valid expression to the parent node via keep it in stack.    
     If none valid, return back null.
    3. For a ``OrExpression`:   
     If either one of left or right expression is valid, returns back null(replace the stack by null)   
     Otherwise, return the ```left or right```
    4. ```LongValue``` is valid. The ```colume``` will be judged valid when the column is appeared in schema.
    5. For ```comparasion``` expression, it is similar as ```OrExpression```.
- **Examples**  
  current schema only contains table S and R, without B.  
  ```
  S.A = B.C                             -> null
  S.A = B.C or XXX                      -> null
  S.A > 3 and B.C = R.G and R.G < S.A   -> S.A > 3 and R.G < S.A
  ```
- **Java doc**  
__*JoinExpressionVisitor*__ lays in ```src/main/java/util/```, so does __*SelectExpressionVisitor*__.  
The related comments are added on the related functions.


## Select Expression Visitor
- **Principles**  
If we see select expressions as a tree, then they need to be evaluated 
from the bottom layer up to top, which means previous result would be used 
in later expressions. Thus, we use stacks to store the results. 
Since each expression has two sides, left and right. We need to 
store both left and right results accordingly into the stack. 
Since results can be int type or boolean type, we have two kind of stacks:
the first which is int type stores results in data form and the second 
stores results in boolean form. 

- **Implementation**  
This is implemented by a Deque<Long> and a Deque<Boolean> in Java.  
  
  Using the visitor pattern, 9 visitor methods are overridden which have parameter 
in AndExpression, Column, LongValue, EqualsTo, NotEqualsTo, GreaterThan, GreaterThanEquals, 
MinorThan, MinorThanEquals respectively.
  1. Implementation of visit method for Column Expression:  
get the data in the current tuple of the certain column and push it to data stack.
  2. Implementation of visit method for Long Expression:  
just push the long value of the expression to the data stack.

  3. Implementation of Each visit method except for Column and Long expression:  
  1 the left side of the expression accepts the visitor  
  2 the right side of expression accepts the visitor  
  3 get the right result by pop the stack  
  4 get the left result by pop the stack  
  5 push the evaluation of the expression using results of both sides into stack.

- **Java doc**  
__*SelectExpressionVisitor*__ lays in ```src/main/java/util/```.
The related Java docs are added on the related functions.

## IndexScanExpressionVisitor
The IndexScanExpressionVisitor aims to extract the higKey and lowKey from the select conditon. It only visit Expressioins: _>=, >, <, <=, =, AND, Column_ and _LongValue_.
- **Rule**
1. If thereis no high bound or low bound, ```highKey``` will be set ```MAX_INT``` or ```lowKey``` to be ```MIN_INT```. If both unavailable, we will not implement Index Scan Operator.
2. Since we have assumed all tuple are Integer, we let the key be the involved.   
    e.g. ```S.A < 50``` -> ```highKey = 49```
3. If there exists valid equal condition, e.g ```S.A = 50```, we will set both ```highKey``` and ```lowKey``` _50_.
