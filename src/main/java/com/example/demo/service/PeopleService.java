package com.example.demo.service;

import com.example.demo.entity.People;
import com.example.demo.repository.DynamoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeopleService {

	@Autowired
	DynamoDBRepository dynamoDBRepository;

	public People save(String id, String subId, String name) {
		People m = new People(id, subId, name);
		dynamoDBRepository.save(m);
		return m;
	}

	public List<People> scanByName(String name) {
		return dynamoDBRepository.scanByName(name);
	}

	public People load(String id, String subId) { return dynamoDBRepository.load(id, subId); }

	public void delete(String id, String subId) { dynamoDBRepository.delete(id, subId); }

	public List<People> findIdAndMoreSubId(String id, String subId) {
		return dynamoDBRepository.findIdAndMoreSubId(id, subId);
	}

	public List<People> transactionLoad() {
		return dynamoDBRepository.transactionLoad();
	}
}