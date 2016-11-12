package net.kuronicle.etl.test;

import org.junit.Test;

public class LocalFileControllerTest {

    @Test
    public void testSetUpData() {
        String name = "";
        String type = "LocalFile";
        String dirPath = "./target";
        String charset = "UTF-8";
        String columnDelimiter = ",";
        String columnQuote = "";
        String lineDelimiter = "\r\n";
        LocalFileController target = new LocalFileController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        target.setupData("./src/test/resources/example/IF0001/UT0001/setup_local_file.xlsx");
    }

    @Test
    public void testAssertData() {
        String name = "";
        String type = "LocalFile";
        String dirPath = "./target";
        String charset = "UTF-8";
        String columnDelimiter = ",";
        String columnQuote = "";
        String lineDelimiter = "\r\n";
        LocalFileController target = new LocalFileController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        target.assertData("./src/test/resources/example/IF0001/UT0001/expected_local_file.xlsx");
    }
}
