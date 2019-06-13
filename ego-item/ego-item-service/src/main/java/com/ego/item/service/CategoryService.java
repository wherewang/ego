package com.ego.item.service;

import com.ego.item.mapper.CategoryMapper;
import com.ego.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    public List<Category> findByParentId(long id) {
        Category category = new Category();
        category.setParentId(id);
        List<Category> list =categoryMapper.select(category);
        return  list;
    }


    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    public List<Category> getListByCids(List<Long> cids) {
//        Example example = new Example(Category.class);
//        Example.Criteria criteria = example.createCriteria().andIn("cid", cids);
//        List<Category> categoryList = categoryMapper.selectByExample(example);
        return  categoryMapper.selectByIdList(cids);
    }


}
