package com.ego.item.controller;

import com.ego.item.pojo.Category;
import com.ego.item.service.CategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/category")
public class CategoryCtrl {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/list")
    public ResponseEntity<List<Category>> list(@RequestParam(value = "pid",required = true)long id){
        List<Category> list = categoryService.findByParentId(id);
        if(list == null || list.size()==0){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }

    /**
       * 通过品牌id查询商品分类
       * @param bid
       * @return
       */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid) {
        List<Category> list = this.categoryService.queryByBrandId(bid);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/cnames")
    public ResponseEntity<String> selectCnamesByCid(@RequestParam("cidList") List<Long> cidList)
    {
        List<Category> categoryList = categoryService.getListByCids(cidList);
        List<String> cnameList = categoryList.stream().map(category -> category.getName()).collect(Collectors.toList());
        if(categoryList==null || categoryList.size()==0)
        {
            return ResponseEntity.notFound().build();
        }
        String cnames = StringUtils.join(cnameList, ",");
        return ResponseEntity.ok(cnames);
    }
}
