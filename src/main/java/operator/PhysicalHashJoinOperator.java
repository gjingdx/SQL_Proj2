package operator;

import PlanBuilder.PhysicalOperatorVisitor;
import io.BinaryTupleReader;
import io.BinaryTupleWriter;
import io.TupleReader;
import io.TupleWriter;
import logical.operator.JoinOperator;
import model.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;
import util.Catalog;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * PhysicalHashJoinOperator extended from PhysicalJoinOperator
 *
 * @author Yufu Mo ym445
 */
public class PhysicalHashJoinOperator extends PhysicalJoinOperator {

    private final static int THREAD_POOL_SIZE = 10;

    // first hash function bucket size
    private final static int BUCKET_SIZE = 29;
    // second hash function bucket size
    private final static int INNER_BUCKET_SIZE = 43;

    // joint tuple queue, main thread as consumer
    // Task threads as producers
    Deque<Tuple> queue;
    private final static int QUEUE_MAX_SIZE = 400;

    // temp files
    private final String LEFT_BUCKETS_NAME =
            Catalog.getInstance().getTempPath() + "hash_bucket_left_" + this.hashCode() + "_";
    private final String RIGHT_BUCKETS_NAME =
            Catalog.getInstance().getTempPath() + "hash_bucket_right_" + this.hashCode() + "_";

    // count of finished threads
    private int finishCount = 0;

    // left and right temp file writers
    private Map<Integer, TupleWriter> leftBucketWriters;
    private Map<Integer, TupleWriter> rightBucketWriters;

    // index of columns in join condition
    List<Integer> leftOrder;
    List<Integer> rightOrder;



    public PhysicalHashJoinOperator(JoinOperator logicalJoinOp,
                                    PhysicalOperator opLeft,
                                    PhysicalOperator opRight,
                                    List<List<OrderByElement>> orders) {
        super(opLeft, opRight, logicalJoinOp);
        leftOrder = new ArrayList<>();
        rightOrder = new ArrayList<>();
        queue = new LinkedList<>();
        for (OrderByElement order : orders.get(1)) {
            leftOrder.add(opLeft.getSchema().get(order.toString()));
        }
        for (OrderByElement order : orders.get(0)) {
            rightOrder.add(opRight.getSchema().get(order.toString()));
        }
        init();
    }

    /**
     * initialization, hashing and write tuple into different bucket files
     */
    private void init() {
        leftBucketWriters = new HashMap<>();
        rightBucketWriters = new HashMap<>();

        for (int i = 0; i < BUCKET_SIZE; i++) {
            TupleWriter tupleWriter1 = new BinaryTupleWriter(
                    LEFT_BUCKETS_NAME + i, opLeft.getSchema().size());
            leftBucketWriters.put(i, tupleWriter1);
            TupleWriter tupleWriter2 = new BinaryTupleWriter(
                    RIGHT_BUCKETS_NAME + i, opRight.getSchema().size());
            rightBucketWriters.put(i, tupleWriter2);
        }

        // write tuple to buckets
        Tuple leftTuple = opLeft.getNextTuple();
        while (leftTuple != null) {
            int hash = hashcodeLeft1(leftTuple);
            leftBucketWriters.get(hash).writeNextTuple(leftTuple);
//            System.out.println("left " + leftTuple.toString());
            leftTuple = opLeft.getNextTuple();
        }
        Tuple rightTuple = opRight.getNextTuple();
        while (rightTuple != null) {
            int hash = hashcodeRight1(rightTuple);
            rightBucketWriters.get(hash).writeNextTuple(rightTuple);
//            System.out.println("right " + rightTuple.toString());
            rightTuple = opRight.getNextTuple();
        }

        // close files
        for (int i = 0; i < BUCKET_SIZE; i++) {
            leftBucketWriters.get(i).finish();
            rightBucketWriters.get(i).finish();
        }

        startThreads();
    }

    private void startThreads() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < BUCKET_SIZE; i++) {
            threadPool.execute(new Task(i));
        }
    }

    private int hashcodeLeft1(Tuple tuple) {
        int sum = 0;
        for (int index : leftOrder) {
            sum += tuple.getDataAt(index);
        }
        return sum % BUCKET_SIZE;
    }

    private int hashcodeRight1(Tuple tuple) {
        int sum = 0;
        for (int index : rightOrder) {
            sum += tuple.getDataAt(index);
        }
        return sum % BUCKET_SIZE;
    }

    private int hashcodeLeft2(Tuple tuple) {
        int sum = 0;
        for (int index : leftOrder) {
            sum += tuple.getDataAt(index);
        }
        return sum % INNER_BUCKET_SIZE;
    }

    private int hashcodeRight2(Tuple tuple) {
        int sum = 0;
        for (int index : rightOrder) {
            sum += tuple.getDataAt(index);
        }
        return sum % INNER_BUCKET_SIZE;
    }

    /**
     * cross production, taking a tuple from the queue
     *
     * @return joint tuple
     */
    @Override
    protected Tuple crossProduction() {
        Tuple tuple = null;
        try {
            synchronized (queue) {
                if (queue.isEmpty() && finishCount == BUCKET_SIZE) {
                    return null;
                }
//                System.out.println("consume: " + queue.size());
//                System.out.println("count " + finishCount);
                // if queue is empty, wait
                if (queue.isEmpty()) {
                    queue.wait(200);
                }
                tuple = queue.poll();
                if (queue.size() == 0) {
                    queue.notify();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return tuple;
        }
    }

    @Override
    public void accept(PhysicalOperatorVisitor phOpVisitor, int level) {
        phOpVisitor.visit(this, level);
    }


    /**
     * Thread class
     * each Task thread operates one particular bucket
     * join the tuple from left and right buckets with same hashcode1
     */
    class Task implements Runnable {

        private int index;
        private TupleReader innerReader;
        private TupleReader outerReader;
        private Object lock = new Object();

        // hash table for outer bucket1
        Map<Integer, List<Tuple>> buckets;

        public Task(int i) {
            index = i;
            buckets = new HashMap<>();
            innerReader = new BinaryTupleReader(LEFT_BUCKETS_NAME + index);
            outerReader = new BinaryTupleReader(RIGHT_BUCKETS_NAME + index);

            for (int j = 0; j < INNER_BUCKET_SIZE; j++) {
                buckets.put(j, new ArrayList<>());
            }
            try {
                Tuple tuple = innerReader.readNextTuple();
                while (tuple != null) {
                    buckets.get(hashcodeLeft2(tuple)).add(tuple);
                    tuple = innerReader.readNextTuple();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Tuple rightTuple = outerReader.readNextTuple();
                while (rightTuple != null) {
                    int hash = hashcodeRight2(rightTuple);
                    for (Tuple leftTuple : buckets.get(hash)) {

                        // tuple is left tuple, outertuple is right tuple
                        boolean e = true;
//                        System.out.println("task " + index + ": " + queue.size());


                        // check if two tuple satisfy join condition
                        for (int k = 0; k < leftOrder.size(); k++) {
                            if (leftTuple.getDataAt(leftOrder.get(k)) != rightTuple.getDataAt(rightOrder.get(k))) {
                                e = false;
                                break;
                            }
                        }
                        if (!e) {
                            continue;
                        }

                        Tuple newTuple = joinTuple(leftTuple, rightTuple);

                        // wait if the queue is full
                        synchronized (queue) {
                            while (queue.size() > QUEUE_MAX_SIZE) {
                                queue.wait(200);
                            }
                            queue.offer(newTuple);
                            queue.notify();
                        }
                    }
                    rightTuple = outerReader.readNextTuple();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                synchronized (lock) {
//                    System.out.println("" + index + " finished");
                    File file1 = new File(LEFT_BUCKETS_NAME + index);
                    File file2 = new File(RIGHT_BUCKETS_NAME + index);
                    file1.delete();
                    file2.delete();
                    finishCount++;
                }
            }
        }
    }


}
