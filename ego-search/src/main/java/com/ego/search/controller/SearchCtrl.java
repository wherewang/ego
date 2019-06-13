package com.ego.search.controller;

import com.ego.common.pojo.PageResult;
import com.ego.search.pojo.Goods;
import com.ego.search.pojo.bo.SearchRequest;
import com.ego.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//Request URL: http://api.ego.com/api/search/page
//Request Method: POST

@RestController
//@RequestMapping("/search")  网关已经知道是谁了
public class SearchCtrl {
    @Autowired
    private SearchService searchService;

    @PostMapping("/page")
    public ResponseEntity<PageResult<Goods>> page(@RequestBody SearchRequest searchRequest)
    {
        PageResult<Goods> result = searchService.searchPage(searchRequest);
        if(result == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
