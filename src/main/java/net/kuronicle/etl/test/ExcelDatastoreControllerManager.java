package net.kuronicle.etl.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelDatastoreControllerManager implements
                                             DatastoreControllerManager {

    /**
     * データストア情報のシート名。
     */
    private static final String SHEET_NAME_DATASTORE_INFO = "DatastoreInfo";

    /**
     * データストア情報が開始する行数。
     */
    private static final int ROW_NUM_START_DATASTORE_INFO = 1;

    /**
     * Excelシートを読み込む最大行数。
     */
    private static final int ROW_NUM_LIMIT = 10_000;

    /**
     * 列数：データストア名
     */
    private static final int COL_NUM_NAME = 0;

    /**
     * 列数：データストアタイプ
     */
    private static final int COL_NUM_TYPE = 1;

    /**
     * データストアタイプ：LocalFile
     */
    private static final String DATASTORE_TYPE_LOCAL_FILE = "LocalFile";

    /**
     * データストアタイプ：Database
     */
    private static final String DATASTORE_TYPE_DATABCE = "Database";

    /**
     * データストアタイプ：LocalFile from Excle file.
     */
    private static final String DATASTORE_TYPE_LOCAL_FILE_FORM_EXCEL = "LocalFileFromExcel";

    /**
     * 列数：JDBCドライバクラス名
     */
    private static final int COL_NUM_JDBC_DRIVER_CLASS_NAME = 2;

    /**
     * 列数：JDBCコネクションURL
     */
    private static final int COL_NUM_JDBC_CONNECTION_URL = 3;

    /**
     * 列数：DBユーザ名
     */
    private static final int COL_NUM_DB_USER_NAME = 4;

    /**
     * 列数：DBパスワード
     */
    private static final int COL_NUM_DB_PASSWORD = 5;

    /**
     * 列数：DBスキーマ
     */
    private static final int COL_NUM_DB_SCHEMA = 6;

    /**
     * 列数：ディレクトリパス
     */
    private static final int COL_NUM_DIR_PATH = 7;

    /**
     * 列数：文字コード
     */
    private static final int COL_NUM_CHARSET = 8;

    /**
     * 列数：区切り文字
     */
    private static final int COL_NUM_COL_DELIMITER = 9;

    /**
     * 列数：引用符
     */
    private static final int COL_NUM_COL_QUOTE = 10;

    /**
     * 列数：改行コード
     */
    private static final int COL_NUM_LINE_DELIMITER = 11;

    /**
     * 列数：ヘッダ行有り無し
     */
    private static final int COL_NUM_HAS_HEADER = 12;

    private Map<String, DatastoreController> datastoreControllerMap = new HashMap<>();

    public ExcelDatastoreControllerManager(String datastoreInfoExcelFilePath) {
        readDatastoreInfo(datastoreInfoExcelFilePath);
    }

    private void readDatastoreInfo(String datastoreInfoExcelFilePath) {
        try {
            Workbook datastoreInfoWb = WorkbookFactory.create(
                    new File(datastoreInfoExcelFilePath));

            Sheet datastoreInfoSheet = datastoreInfoWb.getSheet(
                    SHEET_NAME_DATASTORE_INFO);

            if (datastoreInfoSheet == null) {
                throw new RuntimeException(String.format(
                        "Datastore info sheet does not found. file=%s, sheetName=%s.",
                        datastoreInfoExcelFilePath, SHEET_NAME_DATASTORE_INFO));
            }

            int rowCount = ROW_NUM_START_DATASTORE_INFO;
            while (true) {
                Row row = datastoreInfoSheet.getRow(rowCount);

                String name = row.getCell(COL_NUM_NAME).getStringCellValue();

                if (name == null || "".equals(name)) {
                    log.debug("stop reading datastore info.");
                    break;
                }

                String type = row.getCell(COL_NUM_TYPE).getStringCellValue();

                DatastoreController contorller = null;

                switch (type) {
                case DATASTORE_TYPE_DATABCE:
                    contorller = createDatabaseDatastoreController(name, type,
                            row);
                    break;
                case DATASTORE_TYPE_LOCAL_FILE:
                    contorller = createFlatFileDatastoreController(name, type,
                            row);
                    break;
                case DATASTORE_TYPE_LOCAL_FILE_FORM_EXCEL:
                    contorller = createLocalFileFromExcelController(name, type,
                            row);
                    break;
                default:
                    throw new IllegalStateException(String.format(
                            "Not supported datastore type. name=%s, type=%",
                            name, type));
                }

                if (contorller != null) {
                    log.debug(String.format(
                            "read datastore controller. controller=%s",
                            contorller.toString()));
                    datastoreControllerMap.put(name, contorller);
                }

                // 次の行を見る。
                rowCount++;

                if (rowCount > ROW_NUM_LIMIT) {
                    throw new IllegalStateException("Exceed the limit of parce Excel sheet. Please check the check input sheet. limit="
                            + ROW_NUM_LIMIT);
                }
            }

        } catch (InvalidFormatException e) {
            throw new RuntimeException("Invalid datastore file. file="
                    + datastoreInfoExcelFilePath, e);
        } catch (IOException e) {
            throw new RuntimeException("Error occerd when reading datastore file. file="
                    + datastoreInfoExcelFilePath, e);
        }
    }

    private DatastoreController createDatabaseDatastoreController(String name,
            String type, Row row) {

        String jdbcDriverClassName = row.getCell(COL_NUM_JDBC_DRIVER_CLASS_NAME)
                .getStringCellValue();
        String jdbcConnectionUrl = row.getCell(COL_NUM_JDBC_CONNECTION_URL)
                .getStringCellValue();
        String dbUserName = row.getCell(COL_NUM_DB_USER_NAME)
                .getStringCellValue();
        String dbPassword = row.getCell(COL_NUM_DB_PASSWORD)
                .getStringCellValue();
        String dbSchema = row.getCell(COL_NUM_DB_SCHEMA).getStringCellValue();
        dbSchema = (dbSchema == null || "".equals(dbSchema)) ? null : dbSchema;

        return new DatabaseController(name, jdbcDriverClassName, jdbcConnectionUrl, dbUserName, dbPassword, dbSchema);
    }

    private DatastoreController createFlatFileDatastoreController(String name,
            String type, Row row) {

        String dirPath = row.getCell(COL_NUM_DIR_PATH).getStringCellValue();
        String charset = row.getCell(COL_NUM_CHARSET).getStringCellValue();

        LocalFileController controller = new LocalFileController(name, type, dirPath, charset);
        return controller;
    }

    private DatastoreController createLocalFileFromExcelController(String name,
            String type, Row row) {

        String dirPath = row.getCell(COL_NUM_DIR_PATH).getStringCellValue();
        String charset = row.getCell(COL_NUM_CHARSET).getStringCellValue();
        String columnDelimiter = row.getCell(COL_NUM_COL_DELIMITER)
                .getStringCellValue();
        String columnQuote = row.getCell(COL_NUM_COL_QUOTE)
                .getStringCellValue();
        String lineDelimiter = row.getCell(COL_NUM_LINE_DELIMITER)
                .getStringCellValue().replaceAll("\\\\r", "\r").replaceAll(
                        "\\\\n", "\n");
        boolean hasHeader = row.getCell(COL_NUM_HAS_HEADER)
                .getBooleanCellValue();

        LocalFileFromExcelController controller = new LocalFileFromExcelController(name, type, dirPath, charset, columnDelimiter, columnQuote, lineDelimiter, hasHeader);
        return controller;
    }

    public DatastoreController getDatastoreController(String datastoreName) {

        DatastoreController controller = datastoreControllerMap.get(
                datastoreName);

        if (controller != null) {
            return controller;
        }

        throw new IllegalArgumentException("Invalid datastore name. datastoreName="
                + datastoreName);
    }
}
