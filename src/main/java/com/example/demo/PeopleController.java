package com.example.demo;

import com.example.demo.entity.People;
import com.example.demo.service.PeopleService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PeopleController {
	@Autowired
	PeopleService peopleService;

	Log log = LogFactory.getLog(PeopleController.class);

	@GetMapping("/create")
	public People insertPeople(@RequestParam(value = "id") String id, @RequestParam(value = "subId") String subId, @RequestParam(value = "name") String name) {
		People m = peopleService.save(id, subId, name);
		log.info("complete insert. id : " + m.getId() + " sub id : " + m.getSubId() + " name : " + m.getName());
		return m;
	}

	@GetMapping("/scan")
	public List<People> scanByName(@RequestParam(value = "name") String name) {
		return peopleService.scanByName(name);
	}

	@GetMapping("/load")
	public People load(@RequestParam(value = "id") String id, @RequestParam(value = "subId") String subId) {
		return peopleService.load(id, subId);
	}

	@GetMapping("/delete")
	public void delete(@RequestParam(value = "id") String id, @RequestParam(value = "subId") String subId) {
		peopleService.delete(id, subId);
	}

	@GetMapping("/find")
	public List<People> findIdAndMoreSubId(@RequestParam(value = "id") String id, @RequestParam(value = "subId") String subId) {
		return peopleService.findIdAndMoreSubId(id, subId);
	}

	@GetMapping("/tran_load")
	public List<People> transactionLoad() {
		return peopleService.transactionLoad();
	}
}