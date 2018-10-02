package operator;

import org.junit.Test;

import junit.framework.Assert;

import java.io.StringReader;
import java.util.Arrays;
import java.util.ArrayList;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import model.Tuple;

public class ScanOperatorTest{

    @Test
    public void testReadFile() throws Exception{
        String statement = "SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = (PlainSelect) ((Select) parserManager.parse(new StringReader(statement))).getSelectBody();
        Operator op = new ScanOperator(plainSelect, 0);
        ArrayList<String> expectedResult = new ArrayList<>(Arrays.asList(
            "1,200,50",
            "2,200,200",
            "3,100,105",
            "4,100,50",
            "5,100,500",
            "6,300,400"
        ));
        ArrayList<String> outputResult = new ArrayList<>();
        Tuple tuple;
        while((tuple = op.getNextTuple()) != null){
            outputResult.add(tuple.toString());
        }
        Assert.assertEquals(expectedResult, outputResult);
    }
}