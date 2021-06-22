package com.example.demo.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionLoadRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.example.demo.entity.People;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamoDBRepository {

    private DynamoDBMapper dynamoDBMapper;

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBRepository.class);

    @Autowired
    public DynamoDBRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    // 1レコードを取得（複数ヒットする場合、最新のレコードを取得する）（load）
    // 引数 : PK
    public <T> People load(String pk, String subId) {
        return dynamoDBMapper.load(People.class, pk, subId);
    }

    // 1レコードを登録（save）
    // 引数 : PK
    public <T> void save(T record) {
        dynamoDBMapper.save(record);
    }

    // 1レコードを削除（delete）
    // 引数 : 1レコードのオブジェクト
    public <T> void delete(String id, String subId) {
        dynamoDBMapper.delete(load(id, subId));
    }

    // Id（パーティションキー）と一致かつSub Id（ソートキー）以下のレコードを取得（query）
    // 引数 : Id + Sub Id
    public <T> List findIdAndMoreSubId(String id, String subId) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(id));
        eav.put(":val2", new AttributeValue().withS(subId));

        DynamoDBQueryExpression<People> queryExpression = new DynamoDBQueryExpression<People>()
                .withKeyConditionExpression("Id <= :val1 and SubId <= :val2").withExpressionAttributeValues(eav);

        List<People> peopleList = dynamoDBMapper.query(People.class, queryExpression);
        return peopleList;
    }

    // Id（パーティションキー）と一致かつSub Id（ソートキー）以下のレコードを取得（query）
    // 引数 : Id + Sub Id
    public <T> List findIdAndEqName(String id, String name) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(id));
        eav.put(":val2", new AttributeValue().withS(name));

        Map<String, String> attributeNames = new HashMap<String, String>();
        attributeNames.put("#n", "Name");

        DynamoDBQueryExpression<People> queryExpression = new DynamoDBQueryExpression<People>()
                .withKeyConditionExpression("Id <= :val1 and #n = :val2").withExpressionAttributeNames(attributeNames).withExpressionAttributeValues(eav);

        List<People> peopleList = dynamoDBMapper.query(People.class, queryExpression);
        return peopleList;
    }

    // queryPageは省略（queryを1MB以下に収まる量だけ取得）


    // 受け取ったNameと一致するレコードを取得（scan）
    // 引数 : Name
    public <T> List scanByName(String searchVal) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":v1", new AttributeValue().withS(searchVal));

        Map<String, String> attributeNames = new HashMap<String, String>();
        attributeNames.put("#n", "Name");

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("#n = :v1").withExpressionAttributeNames(attributeNames).withExpressionAttributeValues(eav);

        logger.info("count : " + dynamoDBMapper.count(People.class, scanExpression));

        List<People> peopleList = dynamoDBMapper.scan(People.class, scanExpression);
        return peopleList;
    }

    // 1つまたは複数のテーブルからオブジェクトをロードする（transactionLoad）
    // 以下の場合、読み取りを中止する
    // ・競合する操作が、読み取る項目を更新中
    // ・トランザクションを完了するにはプロビジョニングされた容量が不足
    // ・データ形式が無効などのユーザー エラー
    // ・トランザクション内のアイテムの合計サイズは 4 MB を超える
    public List transactionLoad() {
        TransactionLoadRequest transactionLoadRequest = new TransactionLoadRequest();

        People people1 = new People("111", "111", "msk");
        People people2 = new People("222", "222", "msk");
        People people3 = new People("333", "333", "msk");
        People people4 = new People("444", "444", "msk");
        People people5 = new People("555", "555", "msk");

        transactionLoadRequest.addLoad(people1);
        transactionLoadRequest.addLoad(people2);
        transactionLoadRequest.addLoad(people3);
        transactionLoadRequest.addLoad(people4);
        transactionLoadRequest.addLoad(people5);

        return dynamoDBMapper.transactionLoad(transactionLoadRequest);
    }
}
