package com.ego.item.service;

import com.ego.item.mapper.SpecifictionMapper;
import com.ego.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecificationService {
    @Autowired
    private SpecifictionMapper specifictionMapper;

    public Specification queryByCid(Long cid) {
        Specification specification = specifictionMapper.selectByPrimaryKey(cid);
        return specification;
    }

    @Transactional
    public void update(Specification specification) {
        specifictionMapper.updateByPrimaryKeySelective(specification);
    }

    @Transactional
    public void save(Specification specification) {
        specifictionMapper.insertSelective(specification);
    }
}
