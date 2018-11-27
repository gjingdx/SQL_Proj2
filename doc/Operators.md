# Operators

## Sort Merge Join
```operator/PhysicalSortMergeJoinOperator```  

### Partition reset  

In TupleReader, we set interfaces ```recordPosition```, ```revertToPosition```, ```moveBack```, and ```reset(i)```.  

1. ```reset(i)``` will reset the tuple reader to the position before the _ith_ tuple. First, we calculate which page the tuple lies in and set the filechannel to this position. Then read the page and set the tuple pointer to the position of the tuple in this page. This method is only used by revertToPostion. For requirement in instruction, we made it public.  
2. ```moveBack``` only go back one tuple length step in tuple reader. It is used in External Sort operation. When merging sort, we read all the first tuple in the blocks but only extracted the minimum one. To leave the remain still be the next tuple to read, we let these tuple readers move back one step. Fortunately, this action will actually reload any byte-buffer.
3. ```recordPosition``` and ```revertToPosition``` exist because we think the method of ```reset(i)``` seems inharmony with ```readNextTuple```, where the former only need record the index of tuple, while the other does not. So we make the method ```recordPosition``` to record the position of last inequal tuple in right table. Then, when need to reset, we only need to refer to ```revertToPosition``` to set back to the record position.
4. Distinct will not have such reset problem, since we sort the tuple not only by the order element but also the rest columns.
### Unbouded State  
1. SMJ  
    SMJ will inherit two sort operations as left and right operation. The memory used to sort is only allocated in the construction of sort operation. The sorted tuple list will be stored in a temp file then. Thus, the memory used for SMJ is at most two pages for reading these two temp files (smaller than minimum block size of external sort).
2. External Sort  
    External Sort will store each run of each pass into an independent temp file. When implementing merge sort, we only need #(block-1) tuple readers to read these runs, and one single page(tuple writer) to write out the minimum tuple among these buffered runs. Thus, the memory will be contrained in #block pages. 
3. Distinct
    Whatever if there exists sort in the sql statement or not, we will implement sort operations before distinct. Thus, we do not need any memory in distinct.  

## Index Scan
### Where the ```lowkey``` and ```highkey``` are set  
In fact, ```lowkey``` and ```highkey``` are extracted in physical plan builder, and be parsered into the operator via constructor. The tool to extract them is ```IndexScanExpressionVisitor```, which might be detailed illustrated in ```doc\ExpressionVisitor.md``` or the related _Java doc_.  
Briefly speaking, we extract the lowKey and highKey from the select condition, with the following rules:
    - If thereis no high bound or low bound, ```highKey``` will be set ```MAX_INT``` or ```lowKey``` to be ```MIN_INT```. If both unavailable, we will not implement Index Scan Operator.
    - Since we have assumed all tuple are Integer, we let the key be the involved.   
        e.g. ```S.A < 50``` -> ```highKey = 49```
    - If there exists valid equal condition, e.g ```S.A = 50```, we will set both ```highKey``` and ```lowKey``` _50_.

### Difference in clustered vs. unclustered  
    We will load the Index Config according to the table name and column name.  
    You can find the related code in ```operator.PhysicalIndexScanOperator.nextTuple```
    - If clustered:  
        After we get the first Rid and reset the tuple reader according to the Rid. Then we directly implement ```getNextTuple``` of the tuple reader. Read sequently until it exceeds ```highKey```.
    - If unclustered:  
        Each time we get the next Rid via ```deserializer``` and reset the tuple reader to read the tuple in the table.  
    
### How preform the root-ro-leaf tree descent  
    The deserializer first searches for the starting node via the ```lowKey```. It works via ```btree.Deserializer.searchLeafNode``` recursively.  
    Each time it reach an index node, we will get the next node address via ``lowKey` until we reach an entry node.     
### How decides which nodes to be deserialized   
   After we reach the first leaf node, we only need to sequently scan the node address by address. The deserialization will end until exceed ```highKey``` or meet unexpected format node (end of entry node).