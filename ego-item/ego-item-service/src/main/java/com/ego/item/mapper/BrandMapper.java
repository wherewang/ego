package com.ego.item.mapper;

import com.ego.item.pojo.Brand;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@org.apache.ibatis.annotations.Mapper
public interface BrandMapper extends Mapper<Brand>, SelectByIdListMapper<Brand,Long> {
    /**
     * 保存品牌和类别的关系
     * @param cid
     * @param bid
     */
    @Insert("insert into tb_category_brand(category_id,brand_id) values(#{cid},#{bid})")
    void insertCategory_Brand(@Param("cid") Long cid, @Param("bid") Long bid);
//    加个注解来对应着赋值，将@Param("cid") 赋值到#{cid}


    /**
     * z这种方式，只能修改一条数据，由于是多对多关系，不符合业务需求，会造成“覆盖式”修改
     * @param cid
     * @param bid
     */
    @Update("update tb_category_brand set category_id = #{cid} where brand_id = #{bid}")
    void updateCategory_Brand(@Param("cid")Long cid, @Param("bid")Long bid);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    void deleteCategory_BrandByBid(@Param("bid")Long bid);

    @Select("select b.id,b.name,b.image,b.letter from tb_brand b inner join tb_category_brand cb " +
            "on b.id=cb.brand_id " +
            "where cb.category_id=#{cid}")
    List<Brand> selectBrandByCid(@Param("cid")Long cid);
}
