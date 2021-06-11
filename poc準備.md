# PoC準備（Spring Boot + DynamoDBについて学習）

## サンプルアプリ概要
![アプリ構成図](./awsDesign.png) 

<br/>

## ライブラリ選定
### ライブラリの候補
- AWS SDK（DynamoDBMapper） ★採用★
- Spring Data DynamoDB

### できることの比較
| 機能         | AWS SDK | Spring Data DynamoDB |
| ---------- | ------- | -------------------- |
| 登録機能       | ●       | ●                    |
| 検索機能（キー検索） | ●       | ●                    |
| 検索機能（値検索）  | ●       | ●                    |
| 並列検索       | ●       | ✗                    |
| トランザクション処理 | ●       | ✗                    |
|      バッチ処理      | ●        |  ✗                    |

<br/>

## DynamoDBMapperのメソッド一覧
| メソッド                       | 機能概要                                                                                       | 機能確認       |
| -------------------------- | ------------------------------------------------------------------------------------------ | ---------- |
| save                       | ・RDBで言うMERGE相当の機能<br>・PKが未登録の場合はINSERT、登録済の場合はUPDATEを行う                                  | ●          |
| load                       | ・PK検索<br>・PKと一致するレコードを返却する                                                                                      | ●          |
| delete                     | ・レコードを削除する<br>・検索結果に一致する複数レコードを削除したい場合、queryやscanとの組み合わせが必要                                | ●          |
| query                      | ・パーティションキー＋ソートキー検索<br>・ソートキーにのみ比較演算子を使用することができる<br>　("=", "<", "<=", ">", ">=", "BETWEEN") | ●          |
| queryPage                  |・queryの取得結果の先頭1MBのみを取得する                                                                   | ●          |
| scan                       | ・指定した条件で結果セットを絞り込む<br>・テーブルフルスキャン→絞り込みとなるため、他のオペレーションと比べ、性能がよくない                                                                                           | ●          |
| scanPage                   | ・scanの取得結果の先頭1MBのみを取得する                                                                                           | ●          |
| parallelScan               |・一つのテーブルを複数のセグメントに分割し、それらを並行してscanする                                                                                            | ●          |
| batchSave                  |              ・複数のレコードをまとめてテーブルに登録する<br>・トランザクション保証はされない                                                                              | ▲(Spring Batchで確認予定) |
| batchLoad                  |             ・PKを使用して、複数のテーブルからレコードを取得する                                                                               | ▲(Spring Batchで確認予定) |
| batchDelete                | ・複数のレコードをまとめてテーブルから削除する<br>・トランザクション保証はされない                                                                                           | ▲(Spring Batchで確認予定) |
| batchWrite                 |        ・batchLoadとbatchSaveをまとめたメソッド<br>・複数のレコードをまとめて削除、登録する<br>・バッチ実行できるのは、最大25件かつデータサイズが1MB以下                                                                                    | ▲(Spring Batchで確認予定) |
| transactionWrite           |  ・複数のレコードをまとめて削除、登録する<br>・トランザクション制御できるのは、最大25件かつデータサイズが4MB以下<br>・トランザクション処理の途中に同一のレコードに対して他の操作が入った場合、例外をスローする&ロールバックする                                                                                          | ●          |
| transactionLoad            | ・PKに一致するレコードを最大25個のテーブルから取得する<br>・同一トランザクションのデータを取得することができるので、整合性が取れる<br>・取得対象のデータが更新中の場合、例外をスローする                                                                                           | ●          |
| count                      | ・指定されたスキャン式の値を求め、一致する項目数を返し、項目データは返されない                                                                                     | ●          |
| generateCreateTableRequest | ・Entityクラスを元に、DynamoDBのテーブルを作成する                                                                                           | ✗(業務アプリ内でテーブル作成は行わない想定為)          |
| createS3Link               | ・AmzonS3ストレージへのリンクを作成する<br>・画像等を保存・参照する際に使用する                                                                                           | ✗(今のところDynamoDBとAmazonS3との連携を想定していない為)          |
| getS3ClientCache           |             ・AmazonS3にアクセスするためのオブジェクトを取得する                                                                               | ✗(今のところDynamoDBとAmazonS3との連携を想定していない為)          |

<br/>

## 異常系（一部）
- 作成、更新（PKがNull）
<br/>　→　Exception:com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException: TestMst[id]; null or empty value for primary key
- 検索（検索結果が0件の場合）
<br/>　→　Nullを返す
- 削除（PKが存在しないレコードの場合）
<br/>　→　空振りして、正常終了

<br/>

## トランザクションメモ
<details>
- APIについて
<br/>　　→　DynamoDBMapperクラス（上位レベル）　と　AmazonDynamoDBクラス

- トランザクションAPI
<br/>　　→　DynamoDBMapper.transactionWrite()　から　AmazonDynamoDB.transactWriteItems()　を呼び出す

- transactWriteItems()
<br/>　　→　同じリージョン内の 1 つ以上の DynamoDB テーブルにある最大 25 個の異なる項目をターゲット
<br/>　　→　トランザクション内のアイテムの合計サイズは 4 MB を超えることはできません
<br/>　　→　すべて成功するかどれも成功しないかのどちらとなるように（BatchWriteItem では、一部のみ成功可）
<br/>　　→　同じトランザクション内の複数のオペレーションが同じ項目をターゲットとすることはできない
<br/>　　→　トランザクション内にできるアクション
<br/>　　　　　・Put
<br/>　　　　　・Update
<br/>　　　　　・Delete
<br/>　　　　　・ConditionCheck　→　項目が存在することを確認するか、項目の特定の属性の条件を確認
<br/>　　→　クライアントトークン設定可：10分間（複数回送信された場合に、アプリケーションエラーを防ぐ）
<br/>　　→　グローバルセカンダリインデックス (GSI)、ストリーム、バックアップの反映は、即時ではない（非同期）

- 書き込みのエラー

  - 同じ TransactWriteItems オペレーション内の複数のアクションが同じ項目をターゲットとしているために、トランザクション検証エラーが発生
  - TransactWriteItems リクエストが、TransactWriteItems リクエスト内の 1 つ以上の項目に対する継続中の TransactWriteItems オペレーションと競合する場合、TransactionCanceledException が発生
  - トランザクションを完了するプロビジョンドキャパシティーが足りない場合
  - 項目サイズが大きくなりすぎる (400 KB 超)、ローカルセカンダリインデックス (LSI) が大きくなりすぎる
  - 無効なデータ形式などのユーザーエラーがある場合

- 読み取りのエラー
  - TransactGetItems リクエストが、TransactGetItems リクエスト内の 1 つ以上の項目に対する継続中の TransactWriteItems オペレーションと競合する場合、 TransactionCanceledException が発生
  - トランザクションを完了するプロビジョンドキャパシティーが足りない場合
  - 無効なデータ形式などのユーザーエラーがある場合

- Exceptionのキャプチャー
~~~java
     private static List<Object> executeTransactionLoad(TransactionLoadRequest transactionLoadRequest) {
        List<Object> loadedObjects = new ArrayList<Object>();
        try {
            loadedObjects = mapper.transactionLoad(transactionLoadRequest);
        } catch (DynamoDBMappingException ddbme) {
            System.err.println("Client side error in Mapper, fix before retrying. Error: " + ddbme.getMessage());
        } catch (ResourceNotFoundException rnfe) {
            System.err.println("One of the tables was not found, verify table exists before retrying. Error: " + rnfe.getMessage());
        } catch (InternalServerErrorException ise) {
            System.err.println("Internal Server Error, generally safe to retry with back-off. Error: " + ise.getMessage());
        } catch (TransactionCanceledException tce) {
            System.err.println("Transaction Canceled, implies a client issue, fix before retrying. Error: " + tce.getMessage());
        } catch (Exception ex) {
            System.err.println("An exception occurred, investigate and configure retry strategy. Error: " + ex.getMessage());
        }
        return loadedObjects;
    }
    private static void executeTransactionWrite(TransactionWriteRequest transactionWriteRequest) {
        try {
            mapper.transactionWrite(transactionWriteRequest);
        } catch (DynamoDBMappingException ddbme) {
            System.err.println("Client side error in Mapper, fix before retrying. Error: " + ddbme.getMessage());
        } catch (ResourceNotFoundException rnfe) {
            System.err.println("One of the tables was not found, verify table exists before retrying. Error: " + rnfe.getMessage());
        } catch (InternalServerErrorException ise) {
            System.err.println("Internal Server Error, generally safe to retry with back-off. Error: " + ise.getMessage());
        } catch (TransactionCanceledException tce) {
            System.err.println("Transaction Canceled, implies a client issue, fix before retrying. Error: " + tce.getMessage());
        } catch (Exception ex) {
            System.err.println("An exception occurred, investigate and configure retry strategy. Error: " + ex.getMessage());
        }
    }
~~~
</details>
<br>

## DynamoDBを使用してみての所感
- テーブル設計が重要<br>
→各パーティションに分散するようにパーティションキーを設計する必要がある（ホットパーティションを作らない）<br>
→テーブルフルスキャンを避けるため、セカンダリインデックスを駆使する必要がある<br>
　https://qiita.com/shibataka000/items/e3f3792201d6fcc397fd <br>
　https://docs.aws.amazon.com/ja_jp/amazondynamodb/latest/developerguide/GSI.html
　
- 検索が苦手<br>
→機能として値検索（フィルター）はあるが、性能的にあまり使用しないほうがいい<br>
→検索用のテーブル等を作る必要がある
- Spring → DynamoDBの操作は簡単に実装できる<br>
→公式のライブラリでお手軽に実装できた

<br/>

## これからやりたいこと・やりのこしていること
- 開発に向けてDynamoDB localを試す
- レセ振りのAP基盤部品を組み込んだJavaプロジェクトの作成（したいので、ソースをいただきたいです）
- セカンダリインデックスを使ってみる
- Spring Boot + Spring Batchのタスクレットモデル・チャンクレットモデルで、Batch系のメソッドを試してみる(Spring Boot + Spring Batchの学習)（PoCにBatchは必要？）
- lambdaとDynamoDBの連携？
- DynamoDBとAmazonS3の連携をやってみる？
- CI/CDの打ち合わせに出ていないので、構成管理周りの資料（gitとか）がほしいです


