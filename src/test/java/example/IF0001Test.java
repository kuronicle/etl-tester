package example;

import java.nio.charset.Charset;

import org.h2.engine.Constants;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;
import org.junit.Test;

import net.kuronicle.etl.test.EtlTester;
import net.kuronicle.etl.test.ExcelEtlTester;

public class IF0001Test {

    private static final String TEST_DATA_ROOT_DIR = "src/test/resources/example/IF0001/";

    private static final String TEST_EVIDENCE_ROOT_DIR = "target/test-result/evicence/IF0001/";

    private EtlTester etlTester = new ExcelEtlTester("src/test/resources/example/DatastoreInfo.xlsx");

    @BeforeClass
    public static void setUpClass() throws Exception {
        // set up H2 database schema for test.
        String url = "jdbc:h2:mem:db_h2_001;DB_CLOSE_DELAY=-1";
        String user = "sa";
        String password = "";
        String fileName = "src/test/resources/example/ddl/create_test_tables_DB_H2_001.sql";
        Charset charset = Constants.UTF8;
        RunScript.execute(url, user, password, fileName, charset, false);

        url = "jdbc:h2:mem:db_h2_002;DB_CLOSE_DELAY=-1";
        user = "sa";
        password = "";
        fileName = "src/test/resources/example/ddl/create_test_tables_DB_H2_002.sql";
        charset = Constants.UTF8;
        RunScript.execute(url, user, password, fileName, charset, false);
    }

    /**
     * Test for Database.
     * 
     * @throws Exception
     */
    @Test
    public void UT0001() throws Exception {
        String testCaseName = "UT0001";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("DB_H2_001", testDataDir + "setup_DB_H2_001.xlsx");

        // execute ETL.
        // TODO: write some code for execute ETL.

        // backup actual DB.
        etlTester.backupDatastore("DB_H2_001", testDataDir + "expected_DB_H2_001.xlsx", testEvidenceDir + "actual_DB_H2_001.xlsx");

        // assert output files and DB.
        etlTester.assertDatastore("DB_H2_001", testDataDir + "expected_DB_H2_001.xlsx");

    }

    /**
     * Test for CSV , UTF-8 File.
     * 
     * @throws Exception
     */
    @Test
    public void UT0002() throws Exception {
        String testCaseName = "UT0002";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles_CSV_UTF-8", testDataDir + "setup_SourceFiles.xlsx");

        // execute ETL.
        // TODO: write some code for execute ETL.

        // backup actual files.
        etlTester.backupDatastore("TargetFiles_CSV_UTF-8", testDataDir + "expected_TargetFiles.xlsx", testEvidenceDir + "actual");

        // assert output files and DB.
        etlTester.assertDatastore("TargetFiles_CSV_UTF-8", testDataDir + "expected_TargetFiles.xlsx");

    }

    /**
     * Test for TSV, Shift-JIS File.
     * 
     * @throws Exception
     */
    @Test
    public void UT0003() throws Exception {
        String testCaseName = "UT0003";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles_TSV_MS932", testDataDir + "setup_SourceFiles.xlsx");

        // execute ETL.
        // TODO: write some code for execute ETL.

        // backup actual files.
        etlTester.backupDatastore("TargetFiles_TSV_MS932", testDataDir + "expected_TargetFiles.xlsx", testEvidenceDir + "actual");

        // assert output files and DB.
        etlTester.assertDatastore("TargetFiles_TSV_MS932", testDataDir + "expected_TargetFiles.xlsx");

    }

    /**
     * Test for Database with data sort for assartion.
     * 
     * @throws Exception
     */
    @Test
    public void UT0004() throws Exception {
        String testCaseName = "UT0004";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("DB_H2_001", testDataDir + "setup_DB_H2_001.xlsx");

        // execute ETL.
        // TODO: write some code for execute ETL.

        // backup actual DB.
        etlTester.backupDatastore("DB_H2_001", testDataDir + "expected_DB_H2_001.xlsx", testEvidenceDir + "actual_DB_H2_001.xlsx");

        // assert output files and DB.
        etlTester.assertDatastore("DB_H2_001", testDataDir + "expected_DB_H2_001.xlsx", "EMPLOYEE", new String[] { "ID" });

    }

    /**
     * Test for CSV, UTF-8 file with dat sort for assertion.
     * 
     * @throws Exception
     */
    @Test
    public void UT0005() throws Exception {
        String testCaseName = "UT0005";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles_CSV_UTF-8", testDataDir + "setup_SourceFiles.xlsx");

        // execute ETL.
        // TODO: write some code for execute ETL.

        // backup actual files.
        etlTester.backupDatastore("TargetFiles_CSV_UTF-8", testDataDir + "expected_TargetFiles.xlsx", testEvidenceDir + "actual");

        // assert output files and DB.
        etlTester.assertDatastore("TargetFiles_CSV_UTF-8", testDataDir + "expected_TargetFiles.xlsx", "item_UT0005_%1%.txt");
        etlTester.assertDatastore("TargetFiles_CSV_UTF-8", testDataDir + "expected_TargetFiles.xlsx", "item_UT0005_%2%.txt");

    }

    /**
     * @throws Exception
     */
    @Test
    public void UT0006() throws Exception {
        String testCaseName = "UT0006";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles_TSV_MS932", testDataDir + "setup_SourceFiles.xlsx");

        // execute ETL.
        // TODO: write some code for execute ETL.

        // backup actual files.
        etlTester.backupDatastore("TargetFiles_TSV_MS932", testDataDir + "expected_TargetFiles.xlsx", testEvidenceDir + "actual");

        // assert output files and DB.
        etlTester.assertDatastore("TargetFiles_TSV_MS932", testDataDir + "expected_TargetFiles.xlsx", "item_UT0006_%1%.txt", new String[] { "ID" });
        etlTester.assertDatastore("TargetFiles_TSV_MS932", testDataDir + "expected_TargetFiles.xlsx", "item_UT0006_%2%.txt", new String[] { "ID" });

    }

    @Test
    public void UT0007() throws Exception {
        String testCaseName = "UT0007";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles_MS932", testDataDir + "setup");

        // execute ETL.
        // TODO: write some code for execute ETL.

        etlTester.assertDatastore("TargetFiles_MS932", testDataDir + "expected", "item_UT0007.txt");
        etlTester.assertDatastore("TargetFiles_MS932", testDataDir + "expected", "shop_UT0007.txt");

    }

    @Test
    public void UT0008() throws Exception {
        String testCaseName = "UT0008";
        String testDataDir = TEST_DATA_ROOT_DIR + testCaseName + "/";
        String testEvidenceDir = TEST_EVIDENCE_ROOT_DIR + testCaseName + "/";

        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles_UTF-8", testDataDir + "setup");

        // execute ETL.
        // TODO: write some code for execute ETL.

        etlTester.assertDatastore("TargetFiles_UTF-8", testDataDir + "expected", "item_UT0008_%1%.txt", new String[] {});
        etlTester.assertDatastore("TargetFiles_UTF-8", testDataDir + "expected", "item_UT0008_%2%.txt", new String[] {});
    }

}
