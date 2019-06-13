package com.ego.item.mapper;

import com.ego.item.pojo.Sku;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SkuMapper extends tk.mybatis.mapper.common.Mapper<Sku> {

    @Delete("delete from tb_sku where spu_id = #{spu_id}")
    void deleteBySpuId(@Param("spu_id") Long spuId);

    @Select("select sku.id,sku.price,sku.images,sku.enable from tb_sku sku where spu_id = #{spu_id}")
    List<Sku> selectSkuListBySpuId(@Param("spu_id") Long spuId);
}
