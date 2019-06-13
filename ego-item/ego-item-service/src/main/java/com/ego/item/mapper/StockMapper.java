package com.ego.item.mapper;

import com.ego.item.pojo.Stock;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockMapper extends tk.mybatis.mapper.common.Mapper<Stock> {

//    @Delete("delete from tb_stock s inner join tb_sku sk" +
//            " on s.sku_id = sk.id" +
//            " where sk.spu_id = #{spu_id}")
//    void deleteBySkuId(@Param("spu_id") Long spuId);

//    @Delete("delete from tb_stock where sku_id = ( select sk.id from tb_sku sk where sk.spu_id = #{spu_id})")
    @Delete("delete from tb_stock where sku_id in (select sk.id from tb_sku sk where sk.spu_id = #{spu_id})")
    void deleteBySkuId(@Param("spu_id") Long spuId);
}
