package com.ego.search.pojo.bo;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import com.ego.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResponse extends PageResult {
    private List<Category> categories;
    private List<Brand> brands;

    private List<Map<String,Object>> specs; // 规格参数过滤条件

    public SearchResponse(Long total, Long totalPage, List items, List<Category> categories, List<Brand> brands) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResponse(Long total, Long totalPage, List items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
