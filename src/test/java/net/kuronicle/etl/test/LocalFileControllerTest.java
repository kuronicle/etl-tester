package net.kuronicle.etl.test;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocalFileControllerTest {

    private static String TEST_TARGET_DIR = "./target/unit-test-tmp/net/kuronicle/etl/test/LocalFileControllerTest";

    @BeforeClass
    public static void setUp() {
        // Create test dir.
        File testTargetDir = new File(TEST_TARGET_DIR);
        testTargetDir.mkdirs();
    }

    @AfterClass
    public static void tearDown() {
        // Delete test dir.
    }

    @Test
    public void testSetUpData001() {
        String name = "UTF-8 CSV file";
        String type = "LocalFile";
        String dirPath = TEST_TARGET_DIR;
        String charset = "UTF-8";
        String columnDelimiter = ",";
        String columnQuote = "";
        String lineDelimiter = "\r\n";
        LocalFileFromExcelController target = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        // execute
        target.setupDatastore(
                "./src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx");

        // assert
        target.assertDatastore(
                "./src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");
    }

    @Test
    public void testSetUpData002() {
        String name = "UTF-8 TSV file";
        String type = "LocalFile";
        String dirPath = TEST_TARGET_DIR;
        String charset = "UTF-8";
        String columnDelimiter = "\\t";
        String columnQuote = "";
        String lineDelimiter = "\r\n";
        LocalFileFromExcelController target = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        // execute
        target.setupDatastore(
                "./src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx");

        // assert
        target.assertDatastore(
                "./src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");
    }

    @Test
    public void testSetUpData003() {
        String name = "MS932 CSV file";
        String type = "LocalFile";
        String dirPath = TEST_TARGET_DIR;
        String charset = "MS932";
        String columnDelimiter = ",";
        String columnQuote = "";
        String lineDelimiter = "\r\n";
        LocalFileFromExcelController target = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        // execute
        target.setupDatastore(
                "./src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx");

        // assert
        target.assertDatastore(
                "./src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");
    }

    @Test
    public void testSetUpData004() {
        String name = "MS932 TSV file";
        String type = "LocalFile";
        String dirPath = TEST_TARGET_DIR;
        String charset = "MS932";
        String columnDelimiter = "\\t";
        String columnQuote = "";
        String lineDelimiter = "\r\n";
        LocalFileFromExcelController target = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        // execute
        target.setupDatastore(
                "./src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx");

        // assert
        target.assertDatastore(
                "./src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");
    }

    @Test
    public void testSetUpData005() {
        String name = "UTF-8 CSV file";
        String type = "LocalFile";
        String dirPath = TEST_TARGET_DIR;
        String charset = "UTF-8";
        String columnDelimiter = ",";
        String columnQuote = "\"";
        String lineDelimiter = "\n";
        LocalFileFromExcelController target = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        // execute
        target.setupDatastore(
                "./src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx");

        // assert
        target.assertDatastore(
                "./src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");
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
        LocalFileFromExcelController target = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, true);

        target.assertDatastore(
                "./src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");
    }
}
