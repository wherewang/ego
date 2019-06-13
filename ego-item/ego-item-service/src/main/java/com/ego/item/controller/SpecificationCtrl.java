package com.ego.item.controller;

import com.ego.item.pojo.Specification;
import com.ego.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spec")
public class SpecificationCtrl {

    @Autowired
    private SpecificationService specificationService;

    @GetMapping("/{cid}")
    public ResponseEntity<String> querySpecificationByCid(@PathVariable(name = "cid") Long cid){
        Specification specification = specificationService.queryByCid(cid);
        if(specification == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specification.getSpecifications());
    }
    @PutMapping
    public ResponseEntity<Void> update(Specification specification)
    {
        specificationService.update(specification);
        return ResponseEntity.ok(null);
    }

    @PostMapping
    public ResponseEntity<Void> save(Specification specification)
    {
        specificationService.save(specification);
        return ResponseEntity.ok(null);
    }

}
