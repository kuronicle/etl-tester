package net.kuronicle.etl.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.excel.XlsDataSet;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import com.univocity.parsers.tsv.TsvFormat;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import com.univocity.parsers.tsv.TsvWriter;
import com.univocity.parsers.tsv.TsvWriterSettings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.kuronicle.etl.test.dbunit.Assertion;
import net.kuronicle.etl.test.exception.EtlTesterException;

@RequiredArgsConstructor
@ToString
@Slf4j
public class LocalFileFromExcelController implements DatastoreController {

    private static final String MULTI_FILE_NAME_REGEX = ".*%[0-9]+%.*";

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

    private Map<String, IDataSet> expectedDataSetMap = new HashMap<>();

    @Override
    public void setupDatastore(String setupDataFile) {

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

    private void writeFile(String fileName,
            XlsDataSet dataSet) throws DataSetException, IOException {
        String filePath = FilenameUtils.concat(dirPath, fileName);

        List<Object[]> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        ITable table = dataSet.getTable(fileName);
        int rowCount = table.getRowCount();
        ITableMetaData tableMetaData = table.getTableMetaData();
        Column[] columns = tableMetaData.getColumns();
        Arrays.stream(columns).forEach(column -> headers.add(column
                .getColumnName()));

        for (int i = 0; i < rowCount; i++) {
            Object[] row = new Object[headers.size()];

            for (int j = 0; j < headers.size(); j++) {
                row[j] = table.getValue(i, headers.get(j));
            }
            rows.add(row);
        }

        log.debug("Start to write file. path=" + filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(
                filePath), Charset.forName(charset))) {

            if (",".equals(columnDelimiter)) {
                CsvFormat format = new CsvFormat();
                format.setLineSeparator(lineSeparator);
                if (columnQuote != null && !"".equals(columnQuote)) {
                    format.setQuote(columnQuote.charAt(0));
                }
                CsvWriterSettings writerSettings = new CsvWriterSettings();
                writerSettings.setFormat(format);
                CsvWriter csvWriter = new CsvWriter(writer, writerSettings);

                if (hasHeader) {
                    csvWriter.writeHeaders(headers);
                }

                csvWriter.writeRowsAndClose(rows);
            } else if ("\\t".equals(columnDelimiter)) {
                TsvFormat format = new TsvFormat();
                format.setLineSeparator(lineSeparator);
                TsvWriterSettings writerSettings = new TsvWriterSettings();
                writerSettings.setFormat(format);
                TsvWriter tsvWriter = new TsvWriter(writer, writerSettings);

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
    public void assertDatastore(String expectedDataFile) {
        try {
            XlsDataSet expectedDataSet = createXlsDataSetFrom(expectedDataFile);

            XlsDataSet actualDataSet = createActualDataset(expectedDataSet);

            Assertion.assertEquals(expectedDataSet, actualDataSet);

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

    private XlsDataSet createXlsDataSetFrom(String dataFilePath) {
        try {
            return new XlsDataSet(new File(dataFilePath));
        } catch (DataSetException e) {
            throw new RuntimeException("Failed to read datastore. datastoreName="
                    + dataFilePath, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read datastore file."
                    + dataFilePath, e);
        }
    }

    private XlsDataSet createActualDataset(
            XlsDataSet expectedDataSet) throws InvalidFormatException, IOException, DataSetException {

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

                } else if ("\\t".equals(columnDelimiter)) {
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

    @Override
    public void assertAndSaveDatastore(String expectedDataFile,
            String saveDataFile) {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void assertDatastore(String expectedDataFile,
            String targetDataName) {
        assertDatastore(expectedDataFile, targetDataName, null);

    }

    @Override
    public void assertDatastore(String expectedDataFile, String targetDataName,
            String[] sortColumns) {

        IDataSet expectedDataSet = expectedDataSetMap.get(expectedDataFile);
        if (expectedDataSet == null) {
            expectedDataSet = createXlsDataSetFrom(expectedDataFile);
            expectedDataSetMap.put(expectedDataFile, expectedDataSet);
        }

        try {
            ITable expectedData = expectedDataSet.getTable(targetDataName);
            // 実ファイルからXlsDataSetを作成する
            IDataSet actualDataSet = createActualDataSet(expectedData,
                    targetDataName);
            ITable actualData = actualDataSet.getTable(targetDataName);

            if (sortColumns != null) {
                expectedData = new SortedTable(expectedData, sortColumns);
                actualData = new SortedTable(actualData, sortColumns);
            }

            Assertion.assertEquals(expectedData, actualData);

        } catch (DataSetException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (DatabaseUnitException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }

    }

    private IDataSet createActualDataSet(ITable expectedData,
            String targetDataName) throws IOException, DataSetException {

        String actualFilePath = getActualFilePath(dirPath, targetDataName);

        String tmpXlsFilePath = "./target/actual.xlsx";

        try (Workbook actualDataWb = new SXSSFWorkbook();
                FileOutputStream out = new FileOutputStream(tmpXlsFilePath)) {

            File actualFile = new File(actualFilePath);
            if (!actualFile.isFile()) {
                throw new EtlTesterException(String.format(
                        "actual file not found. actualfile=%s",
                        actualFilePath));
            }

            // 実ファイル用のシートを追加する。
            Sheet sheet = actualDataWb.createSheet();
            actualDataWb.setSheetName(0, targetDataName);

            // ヘッダ行は期待値Excelシートから取得して設定する。
            Row headerRow = sheet.createRow(0);
            Column[] columns = expectedData.getTableMetaData().getColumns();
            for (int j = 0; j < columns.length; j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(columns[j].getColumnName());
            }

            // CSV設定
            CsvParserSettings settings = new CsvParserSettings();
            if("\\t".equals(columnDelimiter)) {
                settings.getFormat().setDelimiter('\t');
            } else {
                settings.getFormat().setDelimiter(columnDelimiter.charAt(0));
            }
            settings.getFormat().setLineSeparator(lineSeparator);
            if (!"".equals(columnQuote)) {
                settings.getFormat().setQuote(columnQuote.charAt(0));
            }

            // CSV読込
            List<String[]> rows;
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(
                    actualFilePath), Charset.forName(charset))) {

                CsvParser parser = new CsvParser(settings);
                rows = parser.parseAll(reader);
            }

            // ヘッダ行がある場合は先頭1行を取り除く
            if (hasHeader) {
                rows.remove(0);
            }

            // Excelファイルにデータを書き込む。
            for (int j = 0; j < rows.size(); j++) {
                String[] row = rows.get(j);
                Row dataRow = sheet.createRow(j + 1); // 「0」行目にヘッダ行があるため+1する。
                for (int k = 0; k < row.length; k++) {
                    Cell cell = dataRow.createCell(k);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(row[k]);
                }
            }

            // Excelファイルに出力する
            actualDataWb.write(out);
        }

        File tmpExcleFile = new File(tmpXlsFilePath);
        XlsDataSet actualDataSet = new XlsDataSet(tmpExcleFile);
        tmpExcleFile.delete();

        return actualDataSet;
    }

    private String getActualFilePath(String targetFileDir,
            String targetFileName) {
        if (!targetFileName.matches(MULTI_FILE_NAME_REGEX)) {
            return FilenameUtils.concat(targetFileDir, targetFileName);
        }

        File targetDir = new File(targetFileDir);
        if (!targetDir.isDirectory()) {
            throw new EtlTesterException("Target dir is not directory. targetDir="
                    + targetFileDir);
        }

        // targetDir からファイル名にマッチするファイル名の一覧を取得する。ファイル名順に並び替える。
        String[] fileNames = targetDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(targetFileName.replaceAll("%[0-9]*%",
                        ".+"));
            }
        });
        Arrays.sort(fileNames);

        // 「%n%」の部分から数値を取得する。
        int fileNum = Integer.parseInt(targetFileName.substring(targetFileName
                .indexOf("%") + 1, targetFileName.lastIndexOf("%")));

        // 「%n%」の番号に合わせてファイル名を実際のファイル名に変更する。
        if (fileNames.length < fileNum) {
            throw new EtlTesterException(String.format(
                    "exceed fine number. fileName=%s, fileNumber=%d",
                    targetFileName, fileNames.length));
        }
        String fileName = fileNames[fileNum - 1];
        log.info("convert file name. from={}, to={}", targetFileName, fileName);

        return FilenameUtils.concat(targetFileDir, fileName);
    }

}
