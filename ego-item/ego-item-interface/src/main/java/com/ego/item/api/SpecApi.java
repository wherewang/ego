package com.ego.item.api;

import com.ego.item.pojo.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/spec")
public interface SpecApi {

    @GetMapping("/{cid}")
    public ResponseEntity<String> querySpecificationByCid(@PathVariable(name = "cid") Long cid);


    @PutMapping
    public ResponseEntity<Void> update(Specification specification);

}
