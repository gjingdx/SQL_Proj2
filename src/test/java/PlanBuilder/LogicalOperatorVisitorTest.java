package PlanBuilder;

import com.sql.interpreter.Handler;
import logical.operator.ScanOperator;
import model.Tuple;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import operator.PhysicalOperator;
import org.junit.Test;
import util.Catalog;
import util.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class LogicalOperatorVisitorTest {
    public LogicalOperatorVisitorTest() throws Exception {
        String[] configs = Handler.parserInterpreterConfig("Samples/interpreter_config_file_samples1.txt");
        Handler.init(configs);
        if (Catalog.getInstance().isBuildIndex()) {
            Handler.buildIndexes();
        }
    }

    @Test
    public void getOutput() throws Exception {

    }

}