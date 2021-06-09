# PoC準備（Spring Boot + DynamoDBについて学習）

## サンプルアプリ概要
![アプリ構成図](./awsDesign.png) 

<br/>

## 使用するライブラリの候補
- AWS SDK（DynamoDBMapper） ※AWS公式
- Spring Data DynamoDB ※Spring非公式

<br/>

## できることの比較
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
| メソッド                       | 機能概要                                                                              | 機能確認 |
| -------------------------- | --------------------------------------------------------------------------------- | ---- |
| save                       | RDBで言うMERGE相当。PKが既に登録済の場合はINSERT、未登録の場合はUPDATEを行う。                                | ●    |
| load                       | PK検索。                                                                             | ●    |
| delete                     | レコードを削除する。検索結果に一致する複数レコードを削除したい場合、queryやscanとの組み合わせが必要。                           | ●    |
| query                      | パーティションキー＋ソートキー検索。ソートキーにのみ比較演算子を使用することができる。("=", "<", "<=", ">", ">=", "BETWEEN") | ●    |
| queryPage                  |                                                                                   | ●    |
| scan                       |                                                                                   | ●    |
| scanPage                   |                                                                                   | ●    |
| parallelScan               |                                                                                   | ●    |
| batchSave                  |                                                                                   | ✗(バッチ用途の為)   |
| batchLoad                  |                                                                                   | ✗(バッチ用途の為)   |
| batchDelete                |                                                                                   | ✗(バッチ用途の為)  |
| batchWrite                 |                                                                                   | ✗(バッチ用途の為) |
| transactionWrite           |                                                                                   | ●    |
| transactionLoad            |                                                                                   | ●    |
| count                      |                                                                                   | ●    |
| generateCreateTableRequest |                                                                                   | ✗    |
| createS3Link               |                                                                                   | ✗    |
| getS3ClientCache           |                                                                                   | ✗    |

<br/>




2つのAPIを試してみた
・登録
・検索
・トランザクション
表を作って丸付けする

公式にかかれているメソッド一覧をかく
→動かしてみた際の所感・かんたんな処理概要を書く
→似たような名前のメソッドの違いを書く
ためしていないAPIも書く（なぜためしていないかも）

異常系の挙動
DELETE、検索でエラーが発生した場合、等






