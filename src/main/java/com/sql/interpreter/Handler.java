package com.sql.interpreter;

import PlanBuilder.LogicalOperatorVisitor;
import PlanBuilder.PhysicalOperatorVisitor;
import PlanBuilder.PhysicalPlanBuilder;
import io.*;
import PlanBuilder.LogicalPlanBuilder;
import logical.operator.Operator;
import model.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import util.Catalog;
import util.Constants;
import util.Constants.JoinMethod;
import util.Constants.SortMethod;

import java.io.*;
import java.util.*;

import btree.BPlusTree;

/**
 * Handler class to parse SQL, construct query plan and handle initialization
 * Created by Yufu Mo
 */
public class Handler {



    /**
     * initialize the file paths and directories
     */
    public static void init(String[] args) throws Exception {
        String outputPath = Constants.OUTPUT_PATH;
        if (args != null && args.length >= 3) {
            if (args[0].charAt(args[0].length() - 1) == '/') {
                args[0] = args[0].substring(0, args[0].length() - 1);
            }
            if (args[1].charAt(args[1].length() - 1) != '/') {
                args[1] = args[1] + "/";
            }
            if (args[2].charAt(args[2].length() - 1) != '/') {
                args[2] = args[2] + "/";
            }
            outputPath = args[1];
            Constants.inputPath = args[0];
            Constants.DATA_PATH = Constants.inputPath + "/db/data/";
            Constants.SCHEMA_PATH = Constants.inputPath + "/db/schema.txt";
            Constants.OUTPUT_PATH = args[1];
            Constants.TEMP_PATH = args[2];
            Constants.SQLQURIES_PATH = Constants.inputPath + "/queries.sql";
            Constants.CONFIG_PATH = Constants.inputPath + "/plan_builder_config.txt";
            System.out.println("Constants.inputPath init");
            System.out.println(Constants.inputPath);

            Catalog.getInstance().setBuildIndex("1");
            Catalog.getInstance().setEvaluateSQL("1");
        }
        new File(outputPath).mkdirs();
        new File(Constants.TEMP_PATH).mkdirs();
        final File[] files = new File(outputPath).listFiles();
        for (File f : files) {
            f.delete();
        }
        outputPath += "query";
        Catalog.getInstance().setOutputPath(outputPath);
        createStats();

        setConfigs();
        try {
            parserIndexInfo();
        } catch (Exception e) {
            System.err.println("Index Info parse failed");
            if (Catalog.getInstance().isEvaluateSQL()
                || Catalog.getInstance().isBuildIndex())
            {
                throw e;
            }
        }
    }

    /**
     * create stats.txt file by going through the tables
     *
     * @throws Exception
     */
    public static void createStats() throws Exception {
        String statsPath = Catalog.getInstance().getStatsPath();
//        File fout = new File(statsPath);
//        FileOutputStream fos = new FileOutputStream(fout);
        PrintWriter writer = new PrintWriter(statsPath, "UTF-8");

//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        Map<String, String> files = Catalog.getInstance().getTablePaths();
        Map<String, Map<String, Integer>> schemas = Catalog.getInstance().getSchemas();
        for (String table : files.keySet()) {
            TupleReader reader = new BinaryTupleReader(files.get(table));
            int size = schemas.get(table).size();
            String[] schema = new String[size];
            int[] maxArray = new int[size];
            int[] minArray = new int[size];
            Histogram[] histograms = new Histogram[size];
            for (int i = 0; i < size; ++i) {
                histograms[i] = new Histogram("");
            }

            Arrays.fill(maxArray, Integer.MIN_VALUE);
            Arrays.fill(minArray, Integer.MAX_VALUE);
            int count = 0;
            for (String col : schemas.get(table).keySet()) {
                schema[schemas.get(table).get(col)] = col.split("\\.")[1];
            }
            Tuple tuple = reader.readNextTuple();
            while (tuple != null) {
                count++;
                for (int i = 0; i < size; i++) {
                    int data = tuple.getDataAt(i);
                    maxArray[i] = Math.max(maxArray[i], data);
                    minArray[i] = Math.min(minArray[i], data);

                    histograms[i].add(data);
                }
                tuple = reader.readNextTuple();
            }
            TableHistogram tableHistogram = new TableHistogram();
            
            for (Map.Entry<String, Integer> entry : schemas.get(table).entrySet()) {
                tableHistogram.put(entry.getKey(), histograms[entry.getValue()]);
            }
            Catalog.getInstance().setOriginHistograms(table, tableHistogram);

            StringBuilder sb = new StringBuilder();
            sb.append(table);
            sb.append(' ');
            sb.append(count);
            for (int i = 0; i < size; i++) {
                sb.append(' ');
                sb.append(schema[i]);
                sb.append(',');
                sb.append(minArray[i]);
                sb.append(',');
                sb.append(maxArray[i]);
            }
            writer.println(sb.toString());

        }
        writer.close();

        Catalog.getInstance().parserStats();
    }

    /**
     * Build the index files according to the index config stored in Catalog
     *
     */
    public static void buildIndexes() {
        new File(Catalog.getInstance().getIndexPath()).mkdirs();
        List<String> indexConfigsToDelete = new ArrayList<>();
        for (Map.Entry<String, IndexConfig> entry : Catalog.getInstance().getIndexConfigs().entrySet()) {
            IndexConfig indexConfig = entry.getValue();
            String tableName = indexConfig.tableName;
            int attr = Catalog.getInstance().getTableSchema(tableName).get(indexConfig.schemaName);
            try {
                new BPlusTree(
                        Catalog.getInstance().getDataPath(tableName),
                        attr,
                        indexConfig.order,
                        indexConfig.indexFile
                );
            } catch (Exception e) {
                System.out.println(tableName + attr + " failed to build");
                indexConfigsToDelete.add(entry.getKey());
            }
        }
        for (String key : indexConfigsToDelete) {
            Catalog.getInstance().getIndexConfigs().remove(key);
        }
    }

    private static void sortAndReplaceTable(IndexConfig indexConfig) throws Exception{
        String statement = "Select * From " + indexConfig.tableName
                + " Order By " + indexConfig.schemaName + ";";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Catalog.getInstance().setAttributeOrder(plainSelect);
        PhysicalOperator operator = constructPhysicalQueryPlan(plainSelect);
        ArrayList<Tuple> sortedResult = new ArrayList<>();
        Tuple tuple;
        while ((tuple = operator.getNextTuple()) != null) {
            sortedResult.add(tuple);
        }
        String path = Catalog.getInstance().getDataPath(indexConfig.tableName);
        TupleWriter tupleWriter = new BinaryTupleWriter(path, operator.getSchema().size());
        //TupleWriter readableWriter = new ReadableTupleWriter(path+"_r1", operator.getSchema().size());
        for (Tuple writeTuple : sortedResult) {
            tupleWriter.writeNextTuple(writeTuple);
            //readableWriter.writeNextTuple(writeTuple);
        }
        // finish
        tupleWriter.finish();
        //readableWriter.finish();
    }

    /**
     * called in main function, parse all the queries one by one
     * in the input queries file
     */
    public static void parseSql() {
        try {
            String inputPath = Catalog.getInstance().getSqlQueriesPath();
            CCJSqlParser parser = new CCJSqlParser(new FileReader(inputPath));
            Statement statement;
            int ind = 1;
            while ((statement = parser.Statement()) != null) {
                long startTime = System.currentTimeMillis();

                System.out.println(ind);
                System.out.println("Read statement: " + statement);
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
                Catalog.getInstance().setAttributeOrder(plainSelect);
                PhysicalOperator operator = constructAndPrintQueryPlan(plainSelect, ind);
                operator.dump(ind);
                ind++;

                long endTime = System.currentTimeMillis();
                System.out.println("time: " + (endTime - startTime) + "ms");
            }
        } catch (ParseException e) {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        deleteTempFiles();
    }

    /**
     * parser the join and sort configuration into the Catalog
     *
     * @return true for no issue
     */
    protected static boolean parserPlanBuilderConfig() throws Exception {
        int[][] ret = new int[2][2];
        File configFile = new File(Constants.CONFIG_PATH);
        try {
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            String join = br.readLine();
            String sort = br.readLine();
            String btree = br.readLine();
            br.close();

            if (!setConfig(ret[0], join)) throw new IOException("Fail to read join config");;
            if (!setConfig(ret[1], sort)) throw new IOException("Fail to read sort config");;

            switch (ret[0][0]) {
                case 0:
                    Catalog.getInstance().setJoinMethod(JoinMethod.TNLJ);
                    break;
                case 1:
                    Catalog.getInstance().setJoinMethod(JoinMethod.BNLJ);
                    Catalog.getInstance().setJoinBlockSize(ret[0][1]);
                    break;
                case 2:
                    Catalog.getInstance().setJoinMethod(JoinMethod.SMJ);
                    break;
                default:
                throw new IOException("Unexpected join method");
            }

            switch (ret[1][0]) {
                case 0:
                    Catalog.getInstance().setSortMethod(SortMethod.IN_MEMORY);
                    break;
                case 1:
                    Catalog.getInstance().setSortMethod(SortMethod.EXTERNAL);
                    Catalog.getInstance().setSortBlockSize(ret[1][1]);
                    break;
                default:
                    throw new IOException("Unexpected sort method");
            }

            Catalog.getInstance().setIndexScan(btree.equals("1"));

        } catch (FileNotFoundException e) {
            System.err.println("Cannot find the target config file");
            throw e;
        } catch (IOException e) {
            System.err.println("Unexpected config file format");
            throw e;
        }
        return true;
    }

    private static boolean setConfig(int[] ret, String config) {
        String[] splitedConfig = config.split("\\s+");
        if (splitedConfig.length == 1) {
            ret[0] = Integer.valueOf(splitedConfig[0]);
            return true;
        }
        if (splitedConfig.length == 2) {
            ret[0] = Integer.valueOf(splitedConfig[0]);
            ret[1] = Integer.valueOf(splitedConfig[1]);
            return true;
        }
        return false;
    }

    /**
     * parser the index info from disk
     * must implement before set isBuildIndex
     *
     * @throws Exception file not exists or unexpected format
     */
    public static void parserIndexInfo() throws Exception {
        File file = new File(Catalog.getInstance().getIndexInfoPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String config;
        while ((config = br.readLine()) != null) {
            IndexConfig indexConfig = Catalog.getInstance().setIndexConfig(config);
            if (indexConfig.isClustered) {
                if (Catalog.getInstance().isBuildIndex()){
                    sortAndReplaceTable(indexConfig);
                }
            }
        }
        br.close();
    }

    /**
     * parser the interpreter config from disk
     *
     * @param configFile path
     * @return 5 lines configuration
     * @throws Exception file not exists or unexpected format
     */
    public static String[] parserInterpreterConfig(String configFile) throws Exception {
        File file = new File(configFile);
        int lineCount = 5;
        String[] ret = new String[lineCount];
        BufferedReader br = new BufferedReader(new FileReader(file));
        for (int i = 0; i < lineCount; i++) {
            ret[i] = br.readLine();
        }
        br.close();
        return ret;
    }

    /**
     * build a logicalPlanTree then convert it to a physical plan
     *
     * @param plainSelect
     * @return the root physical operator
     */
    public static PhysicalOperator constructPhysicalQueryPlan(PlainSelect plainSelect) {
        System.out.println("###### Constructing Logical Plan ######");
        Operator logicalOperator = LogicalPlanBuilder.constructLogicalPlanTree(plainSelect);

        LogicalOperatorVisitor logicalOperatorVisitor = new LogicalOperatorVisitor();
        logicalOperator.accept(logicalOperatorVisitor);
        for (String s : logicalOperatorVisitor.getOutput()) {
            System.out.println(s);
        }

        System.out.println("###### Constructing Physical Plan ######");
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        logicalOperator.accept(physPB);
        PhysicalOperator physicalOperator = physPB.getPhysOpChildren().peek();

        PhysicalOperatorVisitor phOpVisitor = new PhysicalOperatorVisitor();
        physicalOperator.accept(phOpVisitor, 0);
        System.out.println(phOpVisitor.getPhPBTree().toString());

        return physicalOperator;
    }

    public static PhysicalOperator constructAndPrintQueryPlan(PlainSelect plainSelect, int i) {
        String physicalPath = Catalog.getInstance().getOutputPath() + i + "_physicalplan";
        String logicalPath = Catalog.getInstance().getOutputPath() + i + "_logicalplan";

        System.out.println("###### Constructing Logical Plan ######");
        Operator logicalOperator = LogicalPlanBuilder.constructLogicalPlanTree(plainSelect);

        LogicalOperatorVisitor logicalOperatorVisitor = new LogicalOperatorVisitor();
        logicalOperator.accept(logicalOperatorVisitor);
        for (String s : logicalOperatorVisitor.getOutput()) {
            System.out.println(s);
        }


        System.out.println("###### Constructing Physical Plan ######");
        PhysicalPlanBuilder physPB = new PhysicalPlanBuilder();
        logicalOperator.accept(physPB);
        PhysicalOperator physicalOperator = physPB.getPhysOpChildren().peek();

        PhysicalOperatorVisitor phOpVisitor = new PhysicalOperatorVisitor();
        physicalOperator.accept(phOpVisitor, 0);
        System.out.println(phOpVisitor.getPhPBTree().toString());
        try {
            // write logical plan
            File logicalout = new File(logicalPath);
            FileOutputStream fos1 = new FileOutputStream(logicalout);
            BufferedWriter lbw = new BufferedWriter(new OutputStreamWriter(fos1));
            for (String s : logicalOperatorVisitor.getOutput()) {
                lbw.write(s);
                lbw.newLine();
            }
            lbw.close();
            // write physical plan
            File physicalout = new File(physicalPath);
            FileOutputStream fos2 = new FileOutputStream(physicalout);
            BufferedWriter pbw = new BufferedWriter(new OutputStreamWriter(fos2));
            pbw.write(phOpVisitor.getPhPBTree().toString());
            pbw.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return physicalOperator;
    }

    private static void deleteTempFiles() {
        File[] fileList = new File(Catalog.getInstance().getTempPath()).listFiles();
        for (File file : fileList) {
            try {
                deletFile(file);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void deletFile(File file) throws Exception {
        if (file.getName().contains("temp_")) {
            if (!file.delete()) {
                throw new Exception("Fail to delete temp file: " + file.getName());
            }
        }
    }

    private static void parserStats() throws Exception {
        File file = new File(Catalog.getInstance().getStatsPath());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String config;
        while ((config = br.readLine()) != null) {
            Catalog.getInstance().putStats(config);
        }
        br.close();
    }

    private static void setConfigs() {
        Catalog.getInstance().setIndexScan(true);
        Catalog.getInstance().setJoinBlockSize(5);
        Catalog.getInstance().setSortBlockSize(5);
    }
}
