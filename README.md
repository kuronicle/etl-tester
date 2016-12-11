# ETL Tester
This is a testing tool for ETL function. This extends DbUnit.

## Overview 

ETL処理のテストをサポートするツールである。JUnitテストでの入力データの準備、出力データと期待データの検証を行う。入力データ、期待データはExcelファイルとして準備する。

## Features

* 対応データタイプ
  * local flat file (csv, tsv)
  * Database (JDBC接続)
* テストデータはExcelファイルで定義
* ETL実行前の入力データのセットアップ（setup）
* ETL実行後の出力エータの検証（assert）

## Usage

### 1. データストア情報の作成

入力データ、出力データのデータストア情報をExcelファイルで作成する。フォーマット、サンプルは [./src/test/resources/example/DatastoreInfo.xlsx](./src/test/resources/example/DatastoreInfo.xlsx) を参照。入力が必要な情報は以下の通り。

| No. | 設定項目 | LocalFile | Database | 説明 |
|-----|----------|-----------|----------|------|
| 1 | Datastore Name | o | o | データストア名。任意の値を設定可能。 |
| 2 | Datastore Type | o | o | データストアのタイプを以下から選択する。LocalFile: 実行マシンのローカルファイル。Database: データベース。」 |
| 3 | JDBC Driver Class | x | o | JDBCドライバクラス。該当クラスをクラスパスに配置する必要がある。 |
| 4 | JDBC Connection URL | x | o | データベースに接続するためのコネクションURL。 |
| 5 | DB User | x | o | データベースユーザー。 |
| 6 | DB Password | x | o | データベースパスワード。 |
| 7 | DB Schema | x | o | データベーススキーマ。スキーマが存在しない場合は空白を指定する。 |
| 8 | File Directory Path | o | x | ファイルの配置先ディレクトリ。 |
| 9 | Codepage | o | x | ファイルの文字コード。 |
| 10 | Column Delimiter | o | x | ファイルのカラム区切り文字。 |
| 11 | Column Quate | o | x | ファイルの引用符。 |
| 12 | Line Separator | o | x | ファイルの改行コード。 |
| 13 | File Header | o | x | ファイルのヘッダ行。Trueとした場合、書き込み時は1行目を読み込まず、書き込み時は1行目にカラム名を書き込む。 |

### 2. データファイルの作成

入力データのセットアップ、出力データの検証に使用するデータファイルを作成する。フォーマット、サンプルは [./src/test/resources/example/IF0001/UT0001](./src/test/resources/example/IF0001/UT0001) 内のExcelファイルを参照すること。ファイルの場合はシート名がファイル名となる。データべースの場合はシート名がテーブル名となる。

### 3. テストケースの作成

JUnitでテストケースを作成する。サンプルは [./src/test/java/example/IF0001Test.java](./src/test/java/example/IF0001Test.java) を参照すること。

```java
public class IF0001Test {

    private EtlTester etlTester = new ExcelEtlTester("src/test/resources/example/DatastoreInfo.xlsx"); // ...(1)
    
    @Test
    public void UT0001() throws Exception {
        // setup input files and DB.
        etlTester.setupDatastore("SourceFiles",
                "src/test/resources/example/IF0001/UT0001/setup_SourceFiles.xlsx"); // ...(2)

        etlTester.setupDatastore("DB_H2_001",
                "src/test/resources/example/IF0001/UT0001/setup_DB_H2_001.xlsx"); // ...(2)

        // execute ETL.
        // TODO: write some code for execute ETL.

        // assert output files and DB.
        etlTester.assertDatastore("TargetFiles",
                "src/test/resources/example/IF0001/UT0001/expected_TargetFiles.xlsx"); //...(3)

        etlTester.assertAndSaveDatastore("DB_H2_001",
                "src/test/resources/example/IF0001/UT0001/expected_DB_H2_001.xlsx",
                "target/test-result/evicence/IF0001/UT0001/actual_DB_H2_001.xlsx"); //...(3)

    }
}
```

| No. | 項目 | 説明 |
|-----|------|-----|
| (1) | EtlTester初期化 | データストア情報ファイル（DatastoreInfo.xlsx）を指定して、EtlTesterを初期化する。 |
| (2) | 入力データセットアップ | 入力データをセットアップする。データストア情報ファイルのデータストア名とセットアップに利用するデータファイルを指定する。 |
| (3) | 出力データ検証 | 出力データを検証する。データストア情報ファイルのデータストア名と検証に利用するデータファイルを指定する。出力データを結果確認や証跡として後で確認したい場合、assertAndSaveDatastoreメソッドを利用し、出力データの保持する出力ファイル名を指定する。 |
