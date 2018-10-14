package util;

/**
 * Store constants and paths
 * due to refactoring, path variables are not final
 * Created by Yufu Mo
 */
public class Constants {
    public static String inputPath = "Samples/samples/input";
    public static String DATA_PATH = inputPath + "/db/data/";
    public static String SCHEMA_PATH = inputPath + "/db/schema.txt";
    public static String OUTPUT_PATH = "Samples/samples/output/";
    public static String SQLQURIES_PATH = inputPath + "/queries.sql";
    public static final int PAGE_SIZE = 4096;
    public static final int INT_SIZE = 4;

    public enum JoinMethod {
        TNLJ, BNLJ, SMJ;
    }

    public enum SortMethod {
        IN_MEMORY, EXTERNAL;
    }
}
