package net.kuronicle.etl.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.kuronicle.etl.test.exception.EtlTesterException;

@RequiredArgsConstructor
@ToString
@Slf4j
public class LocalFileController implements DatastoreController {

    private static final String MULTI_FILE_NAME_REGEX = ".*%[0-9]+%.*";

    @NonNull
    private final String name;

    @NonNull
    private final String type;

    @NonNull
    private final String dirPath;

    @NonNull
    private final String charset;

    /**
     * @param setupDataFile 準備ファイル配置ディレクトリ
     */
    @Override
    public void setupDatastore(String setupDataFile) {
        log.info("***** Start setup. dataStore={}, setupFilesDir={}", name,
                setupDataFile);

        Path srcDirPath = new File(setupDataFile).toPath();

        try (DirectoryStream<Path> directroyStream = Files.newDirectoryStream(
                srcDirPath)) {
            for (Path srcFilePath : directroyStream) {
                Path dstFilePath = new File(FilenameUtils.concat(dirPath,
                        srcFilePath.getFileName().toString())).toPath();
                log.info("Copy file. source={}, dest={}", srcFilePath,
                        dstFilePath);
                Files.copy(srcFilePath, dstFilePath,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    @Override
    public void assertDatastore(String expectedDataFile) {
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

        log.info("***** Start assertion. dataStore={}, folder={}, fileName={}",
                name, expectedDataFile, targetDataName);

        String expectedFilePath = FilenameUtils.concat(expectedDataFile,
                targetDataName);
        String actualFilePath = getActualFilePath(dirPath, targetDataName);

        File expectedFile = new File(expectedFilePath);
        if (!expectedFile.isFile()) {
            throw new EtlTesterException(String.format(
                    "Expected file is not found. expectedFilePath=",
                    expectedFilePath));
        }
        File actualFile = new File(actualFilePath);
        if (!actualFile.isFile()) {
            throw new EtlTesterException(String.format(
                    "Actual file is not found. actualFilePath=",
                    actualFilePath));
        }

        List<String> expectedLines = null;
        List<String> actualLines = null;
        try {

            expectedLines = Files.readAllLines(expectedFile.toPath(), Charset
                    .forName(charset));
            actualLines = Files.readAllLines(actualFile.toPath(), Charset
                    .forName(charset));

        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            throw new EtlTesterException("failes to read files.", e);
        }

        if (sortColumns != null) {
            Collections.sort(expectedLines);
            Collections.sort(actualLines);
        }

        Patch patch = DiffUtils.diff(expectedLines, actualLines);
        List<Delta> deltas = patch.getDeltas();

        if (deltas != null && deltas.size() > 0) {
            String diff = createDiffMessage(deltas);
            String ngMessage = String.format(
                    "Assertion:NG. dataStore=%s, actualFile=%s, expectedFile=%s, diff=%s",
                    name, actualFilePath, expectedFilePath, diff);

            log.info(ngMessage);
            fail(ngMessage);
        }

        log.info(String.format(
                "Assertion:OK. dataStore=%s, actualFile=%s, expectedFile=%s",
                name, actualFilePath, expectedFilePath));
    }

    private String getActualFilePath(String targetFileDir,
            String targetFileName) {

        // 期待ファイル名に「%n%」が含まれていない場合は、
        // 単純にディレクトリパスとファイル名を結合して返却。
        if (!targetFileName.matches(MULTI_FILE_NAME_REGEX)) {
            return FilenameUtils.concat(targetFileDir, targetFileName);
        }

        // 期待ファイル名に「%n%」が含まている場合は、
        // 実ファイルをディレクトリ内でファイル名の昇順でソートし、
        // 対応する期待ファイル名を変更する。

        File targetDir = new File(targetFileDir);
        if (!targetDir.isDirectory()) {
            throw new EtlTesterException("Target dir is not directory. targetDir="
                    + targetFileDir);
        }

        // targetDir からファイル名にマッチするファイル名の一覧を取得する。ファイル名昇順に並び替える。
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
                    "Exceed fine number. fileName=%s, fileNumber=%d",
                    targetFileName, fileNames.length));
        }
        String fileName = fileNames[fileNum - 1];
        log.info("Convert file name. from={}, to={}", targetFileName, fileName);

        return FilenameUtils.concat(targetFileDir, fileName);
    }

    private String createDiffMessage(List<Delta> deltas) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("line.separator"));
        for (Delta delta : deltas) {
            sb.append("expected(line:" + delta.getOriginal().getPosition() + 1
                    + ")" + System.getProperty("line.separator"));
            for (Object expectedLine : delta.getOriginal().getLines()) {
                sb.append("> " + expectedLine + System.getProperty(
                        "line.separator"));
            }
            sb.append("actural(line:" + delta.getRevised().getPosition() + 1
                    + ")" + System.getProperty("line.separator"));
            for (Object expectedLine : delta.getRevised().getLines()) {
                sb.append("< " + expectedLine + System.getProperty(
                        "line.separator"));
            }
        }
        return sb.toString();
    }

    @Override
    public void backupDatastore(String targetDataFile, String backupDataFile) {
        // TODO Auto-generated method stub
        
    }

}
