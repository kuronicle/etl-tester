package net.kuronicle.etl.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.excel.XlsDataSet;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.kuronicle.etl.test.dbunit.Assertion;

@RequiredArgsConstructor
@ToString
@Slf4j
public class LocalFileController implements DatastoreController {

    @NonNull
    private final String name;

    @NonNull
    private final String type;

    @NonNull
    private final String dirPath;

    @NonNull
    private final String charset;

    @NonNull
    private final String columnDelimiter;

    @NonNull
    private final String columnQuote;

    @NonNull
    private final String lineSeparator;

    private final boolean hasHeader;

    @Override
    public void setupData(String setupDataFile) {

        try {
            XlsDataSet dataSet = new XlsDataSet(new File(setupDataFile));

            // Excelシート名 = ファイル名
            String[] fileNames = dataSet.getTableNames();

            // Excelシート毎の処理
            for (String fileName : fileNames) {
                log.debug("Start to setup a local file. name=" + fileName);

                deleteFile(fileName);

                writeFile(fileName, dataSet);

                log.debug("Finish to setup a local file. name=" + fileName);
            }

        } catch (DataSetException e) {
            throw new IllegalStateException("Fail to read data set. file="
                    + setupDataFile, e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to read data file. file="
                    + setupDataFile, e);
        }
    }

    private void deleteFile(String fileName) {
        String filePath = FilenameUtils.concat(dirPath, fileName);
        File file = new File(filePath);
        if (file.exists()) {
            log.debug("Delete file. file=" + filePath);
            file.delete();
        }
    }

    private void writeFile(String fileName, XlsDataSet dataSet) throws DataSetException, IOException {
        String filePath = FilenameUtils.concat(dirPath, fileName);

        List<Object[]> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        ITable table = dataSet.getTable(fileName);
        int rowCount = table.getRowCount();
        ITableMetaData tableMetaData = table.getTableMetaData();
        Column[] columns = tableMetaData.getColumns();
        Arrays.stream(columns).forEach(
                column -> headers.add(column.getColumnName()));

        for (int i = 0; i < rowCount; i++) {
            Object[] row = new Object[headers.size()];

            for (int j = 0; j < headers.size(); j++) {
                row[j] = table.getValue(i, headers.get(j));
            }
            rows.add(row);
        }

        log.debug("Start to write file. path=" + filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths
                .get(filePath), Charset.forName(charset))) {

            if (",".equals(columnDelimiter)) {
                CsvFormat format = new CsvFormat();
                format.setLineSeparator(lineSeparator);
                CsvWriterSettings writerSettings = new CsvWriterSettings();
                writerSettings.setFormat(format);
                CsvWriter csvWriter = new CsvWriter(writer, new CsvWriterSettings());

                if (hasHeader) {
                    csvWriter.writeHeaders(headers);
                }

                csvWriter.writeRowsAndClose(rows);
            } else if ("\t".equals(columnDelimiter)) {
                TsvWriter tsvWriter = new TsvWriter(writer, new TsvWriterSettings());

                if (hasHeader) {
                    tsvWriter.writeHeaders(headers);
                }

                tsvWriter.writeRowsAndClose(rows);
            } else {
                throw new IllegalArgumentException("illegal column delimiter. delimiter="
                        + columnDelimiter);
            }
        }

        log.debug("Finish to write file. path=" + filePath);
    }

    @Override
    public void assertData(String expectedDataFile) {
        try {
            XlsDataSet expectedDataSet = new XlsDataSet(new File(expectedDataFile));

            XlsDataSet actualDataSet = createActualDataset(expectedDataSet);

            Assertion.assertEquals(expectedDataSet, actualDataSet);

            /*
             * DiffCollectingFailureHandler failureHandler = new DiffCollectingFailureHandler();
             * Assertion.assertEquals(expectedDataSet, actualDataSet, failureHandler); List<Difference> diffList =
             * failureHandler.getDiffList(); for (Difference diff : diffList) {
             * log.info(String.format("column=%s, excepcted=%s, actual=%s", diff.getColumnName(), diff.getExpectedValue(), diff
             * .getActualValue())); }
             */
        } catch (DataSetException e) {
            throw new IllegalStateException("Fail to read data set. file="
                    + expectedDataFile, e);
        } catch (IOException e) {
            throw new IllegalStateException("Fail to read data file. file="
                    + expectedDataFile, e);
        } catch (InvalidFormatException e) {
            throw new IllegalStateException("Fail to create actual data Excel file.", e);
        } catch (DatabaseUnitException e) {
            throw new RuntimeException(e);
        }

    }

    private XlsDataSet createActualDataset(XlsDataSet expectedDataSet) throws InvalidFormatException, IOException, DataSetException {

        String actualFilePath = "./target/actual.xlsx";

        try (Workbook actualDataWb = new SXSSFWorkbook();
                FileOutputStream out = new FileOutputStream(actualFilePath)) {

            ITable[] tables = expectedDataSet.getTables();

            int excelSheetCount = 0;
            for (int i = 0; i < tables.length; i++) {
                String fileName = tables[i].getTableMetaData().getTableName();
                String filePath = FilenameUtils.concat(dirPath, fileName);

                // 実ファイルがない場合はExcelシートを作らない。
                File actualFile = new File(filePath);
                if (!actualFile.exists()) {
                    continue;
                }

                Sheet sheet = actualDataWb.createSheet();
                actualDataWb.setSheetName(excelSheetCount, fileName);
                excelSheetCount++;

                // ヘッダ行
                Row headerRow = sheet.createRow(0);
                Column[] columns = tables[i].getTableMetaData().getColumns();
                for (int j = 0; j < columns.length; j++) {
                    Cell cell = headerRow.createCell(j);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(columns[j].getColumnName());
                }

                // データ行（実ファイルを読み込む）
                List<String[]> rows;
                if (",".equals(columnDelimiter)) {
                    // CSV設定
                    CsvParserSettings settings = new CsvParserSettings();
                    settings.getFormat().setLineSeparator(lineSeparator);
                    if (!"".equals(columnQuote)) {
                        settings.getFormat().setQuote(columnQuote.charAt(0));
                    }

                    // CSV読込
                    try (BufferedReader reader = Files.newBufferedReader(Paths
                            .get(filePath), Charset.forName(charset))) {

                        CsvParser parser = new CsvParser(settings);
                        rows = parser.parseAll(reader);
                    }

                } else if ("\t".equals(columnDelimiter)) {
                    // TSV設定
                    TsvParserSettings settings = new TsvParserSettings();
                    settings.getFormat().setLineSeparator(lineSeparator);

                    // TSV読込
                    try (BufferedReader reader = Files.newBufferedReader(Paths
                            .get(filePath), Charset.forName(charset))) {

                        TsvParser parser = new TsvParser(settings);
                        rows = parser.parseAll(reader);
                    }
                } else {
                    throw new IllegalArgumentException("illegal column delimiter. delimiter="
                            + columnDelimiter);
                }

                // ヘッダ行がある場合は先頭1行を取り除く
                if (hasHeader) {
                    rows.remove(0);
                }

                for (int j = 0; j < rows.size(); j++) {
                    String[] row = rows.get(j);
                    Row dataRow = sheet.createRow(j + 1); // 「0」行目にヘッダ行があるため+1する。
                    for (int k = 0; k < row.length; k++) {
                        Cell cell = dataRow.createCell(k);
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        cell.setCellValue(row[k]);
                    }
                }

            }

            // Excelファイルに出力する
            actualDataWb.write(out);

            XlsDataSet actualDataSet = new XlsDataSet(new File(actualFilePath));
            return actualDataSet;
        }
    }

}
