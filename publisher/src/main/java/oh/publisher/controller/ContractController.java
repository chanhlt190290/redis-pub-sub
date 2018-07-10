/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oh.publisher.controller;

import oh.publisher.entity.Contract;
import oh.publisher.redis.Publisher;
import oh.publisher.repository.ContractRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author trungchanh
 */
@Controller
public class ContractController {

    @Autowired
    Publisher publisher;

    @Autowired
    ContractRepo contractRepo;

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addContract(@RequestParam("name") String name, @RequestParam("type") int type) {
        Contract contract = new Contract();
        contract.setName(name);
        contract.setType(type);
        Contract data = contractRepo.save(contract);
        publisher.publish("There is a new contract has been added!");
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
