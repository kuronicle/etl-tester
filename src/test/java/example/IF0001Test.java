package example;

import java.nio.charset.Charset;

import org.h2.engine.Constants;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;
import org.junit.Test;

import net.kuronicle.etl.test.EtlTester;
import net.kuronicle.etl.test.ExcelEtlTester;

public class IF0001Test {

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

    @Test
    public void UT0001() throws Exception {
        // DB,ファイルの準備
        etlTester.setupDatastore("SourceFiles",
                "src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx");

        etlTester.setupDatastore("DB_H2_001",
                "src/test/resources/example/IF0001/UT0001/setup_DB_H2_001.xlsx");

        // ETL処理の実行

        // DB, ファイルの検査
        etlTester.assertDatastore("TargetFiles",
                "src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx");

        etlTester.assertDatastore("DB_H2_001",
                "src/test/resources/example/IF0001/UT0001/expected_DB_H2_001.xlsx");

    }
}
