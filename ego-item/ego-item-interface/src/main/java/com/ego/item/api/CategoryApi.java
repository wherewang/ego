package com.ego.item.api;

import com.ego.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.CacheRequest;
import java.util.List;

@RequestMapping("/category")
public interface CategoryApi {

    @GetMapping("/list")
    public ResponseEntity<List<Category>> list(@RequestParam(value = "pid",required = true)long id);

    /**
       * 通过品牌id查询商品分类
       * @param bid
       * @return
       */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid);

    @GetMapping("/cnames")
    public ResponseEntity<String> selectCnamesByCid(@RequestParam("cidList") List<Long> cidList);

    @GetMapping("/list/cid")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("cids") List<Long> cids);

}
