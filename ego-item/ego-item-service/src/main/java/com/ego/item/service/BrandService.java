package com.ego.item.service;

import com.ego.common.pojo.PageResult;
import com.ego.item.mapper.BrandMapper;
import com.ego.item.pojo.Brand;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    @Transactional(readOnly = true)  /*效率更高些*/
    public PageResult<Brand> page(Integer pageNo, Integer pageSize, Boolean descending, String sortBy, String key) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        if(StringUtils.isNotBlank(key))
        {
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
        if(StringUtils.isNotBlank(sortBy))
        {
            example.setOrderByClause(sortBy + (descending? " desc":" asc"));
        }
        Page<Brand> page = (Page<Brand>)brandMapper.selectByExample(example);
        return new PageResult<>(page.getTotal(),page.getResult());
    }

    @Transactional
    public void save(Brand brand, List<Long> cids) {
        //保存品牌
        brandMapper.insertSelective(brand);
//insert会对所有字段赋值，即使没有值； selective会对传入的值进行非空判断，有值才给字段赋值。
        //保存品牌和类别中间关系
        if(cids!=null){
            for(Long cid:cids)
            {
                brandMapper.insertCategory_Brand(cid,brand.getId());
            }
        }

    }
    @Transactional
    public void delete(Long id) {
        //1.从“品牌表”中删除
        Brand brand = new Brand();
        brand.setId(id);
        brandMapper.delete(brand);
        //2.从“品牌-类别中间表”中删除“关系”
        brandMapper.deleteCategory_BrandByBid(brand.getId());
    }

    @Transactional
    public void put(Brand brand, List<Long> cids) {
        //1.从“品牌表”中修改
        brandMapper.updateByPrimaryKey(brand);
        //2.从“品牌-类别中间表”中修改“关系”
             //2.1删除该品牌下，之前的所有分类（避免覆盖式修改）
        brandMapper.deleteCategory_BrandByBid(brand.getId());
        if(cids!=null){
            for(Long cid:cids)
            {
             //2.2 维护品牌-分类中间表
                brandMapper.insertCategory_Brand(cid,brand.getId());
//                brandMapper.updateCategory_Brand(cid,brand.getId());
            }
        }
    }

    public Brand getBrandByBid(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        return brand;
    }

    public List<Brand> getBrandListBycid(Long cid) {
        List<Brand> brandList = brandMapper.selectBrandByCid(cid);

        return brandList;
    }

    public List<Brand> getBrandListBybids(List<Long> bids) {
        List<Brand> brandList = brandMapper.selectByIdList(bids);
        return brandList;
    }
}
