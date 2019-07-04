package com.ego.seckill.mapper;

import com.ego.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @Author: yaorange
 * @Time: 2019-03-30 17:58
 * @Feature:
 */
public interface StockMapper extends Mapper<Stock>, SelectByIdListMapper<Stock,Long> {
}
