# etl-tester
This is a testing tool for ETL function. This extends DbUnit.

## Overview 

ETL処理のテストをサポートするツールです。JUnitテストでの入力データの準備、出力データと期待データの検証を行います。入力データ、期待データはExcelファイルとして準備します。

## Features

* 対応データタイプ
  * local flat file (csv, tsv)
  * Database (JDBC接続)
* テストデータはExcelファイルで定義
* ETL実行前の入力データのセットアップ
* ETL実行後の出力エータの検証
