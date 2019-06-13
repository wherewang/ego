package com.ego.item.service;

import com.ego.item.pojo.*;
import com.ego.common.pojo.PageResult;
import com.ego.item.mapper.SkuMapper;
import com.ego.item.mapper.SpuDetailsMapper;
import com.ego.item.mapper.SpuMapper;
import com.ego.item.mapper.StockMapper;
import com.ego.item.pojo.bo.SpuBO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
        @Autowired
        private SpuMapper spuMapper;

        @Autowired
        private CategoryService categoryService;

        @Autowired
        private BrandService brandService;

        @Autowired
        private SpuDetailsMapper spuDetailMapper;

        @Autowired
        private SkuMapper skuMapper;

        @Autowired
        private StockMapper stockMapper;

        @Autowired
        private AmqpTemplate amqpTemplate;

    public PageResult<SpuBO> page(String key, Boolean saleable, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        if(!StringUtils.isBlank(key))
        {
            criteria.andLike("title","%"+key+"%").orLike("subTitle","%"+key+"%");
        }
        if(saleable!=null)
        {
            criteria.andEqualTo("saleable",saleable);
        }
        Page<Spu> pageinfo = (Page<Spu>)spuMapper.selectByExample(example);

//将List<Spu> --> List<SpuBo>
        List<SpuBO> spuBOList = pageinfo.stream().map(spu -> {
            SpuBO spuBO = new SpuBO();
            //拷贝已有属性
            BeanUtils.copyProperties(spu, spuBO);

            //查询类别名字&品牌名字
            List<Category> categoryList = categoryService.getListByCids(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
            List<String> names = categoryList.stream().map(category -> category.getName()).collect(Collectors.toList());
            spuBO.setCategoryName(StringUtils.join(names,"/"));

            Brand brand = brandService.getBrandByBid(spu.getBrandId());
            spuBO.setBrandName(brand.getName());

            return spuBO;
        }).collect(Collectors.toList());

        return new PageResult<>(pageinfo.getTotal(),spuBOList);
    }

    @Transactional
    public void save(SpuBO spuBO) {
        //添加业务需要的字段值，保存SpuBo
        spuBO.setSaleable(true);
        spuBO.setValid(true);
        spuBO.setCreateTime(new Date());
        spuBO.setLastUpdateTime(spuBO.getCreateTime());

        spuMapper.insertSelective(spuBO);
        //设置关系，保存SpuDetail
        spuBO.getSpuDetail().setSpuId(spuBO.getId());
        spuDetailMapper.insertSelective(spuBO.getSpuDetail());
        //设置关系，保存Sku
        if(spuBO.getSkus()!=null)
        {
            spuBO.getSkus().forEach(sku -> {
                sku.setSpuId(spuBO.getId());

                sku.setCreateTime(new Date());
                sku.setLastUpdateTime(sku.getCreateTime());

                skuMapper.insertSelective(sku);

                //设置关系，保存Stock
                Stock stock = sku.getStock();
                stock.setSkuId(sku.getId());
                stockMapper.insertSelective(stock);
            });
        }

        //item操作了数据，就发送消息到交换机(通知其他微服务做更新)
        amqpTemplate.convertAndSend("item.insert",spuBO.getId());

    }
    @Transactional
    public void delete(Long id) {
        //删除Spu
        spuMapper.deleteByPrimaryKey(id);
        //删除SpuDetail
        spuDetailMapper.deleteByPrimaryKey(id);

        //删除Stock
        stockMapper.deleteBySkuId(id);
        //删除Sku之前，先删除库存stock  （现实中，可能添加商品时，搞错了信息，就必须把刚才添加的所有信息删除）
        skuMapper.deleteBySpuId(id);

    }

//    public SpuBO getDetail(Long id) {
//        //准备好SpuBO,用于装好几个数据(完整数据)，都是相关联的
//        SpuBO spuBO = new SpuBO();
//
//        //查出Spu信息,并设置和SpuBO的关系
//        Spu spu = spuMapper.selectByPrimaryKey(id);
//        BeanUtils.copyProperties(spu,spuBO);
//        //查出SpuDetail信息,并设置和SpuBO的关系
//        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
//        spuBO.setSpuDetail(spuDetail);
//
//        //查出Sku信息,并设置和SpuBO的关系
////      Sku sku = skuMapper.selectByPrimaryKey(id);
//        Example example = new Example(Sku.class);
//        Example.Criteria criteria = example.createCriteria();
////        criteria.andEqualTo(id);
////        criteria.andEqualTo("spu_id",id);
//        criteria.andEqualTo("spuId",id);
//        List<Sku> skuList = skuMapper.selectByExample(example);
////                                                                Sku sku = new Sku();
////                                                                sku.setSpuId(id);
////                                                                List<Sku> skuList = skuMapper.selectByExample(sku);
//        skuList.forEach(sku -> {
//            //查询Stock信息,设置关系
//            Long skuId = sku.getId();
//            Stock stock = stockMapper.selectByPrimaryKey(skuId);
//            sku.setStock(stock);
//        });
//        spuBO.setSkus(skuList);
//        return spuBO;
//    }

    public SpuDetail getDetail(Long id) {

        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);

        return spuDetail;
    }

    @Transactional
    public void put(SpuBO spuBO) {
        Spu spu = new Spu();
        BeanUtils.copyProperties(spuBO,spu);
        spu.setLastUpdateTime(new Date());
        spuMapper.updateByPrimaryKeySelective(spu);

        SpuDetail spuDetail = spuBO.getSpuDetail();
        spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

        List<Sku> skuList = spuBO.getSkus();
        //先清空，根据spuId
        stockMapper.deleteBySkuId(spuBO.getId());
        //必须在根据spuid删除那几行之前，先删除stock。
        // 又犯了个大傻逼，时间太长了，大脑记忆力，反应都很慢了
        skuMapper.deleteBySpuId(spuBO.getId());
//        stockMapper.deleteBySkuId(spuBO.getId());
        //再重新设置关系，添加
        if(skuList != null)
        {
            skuList.forEach(sku -> {
                sku.setSpuId(spuBO.getId());
                sku.setCreateTime(new Date());
                sku.setLastUpdateTime(sku.getCreateTime());
                skuMapper.insertSelective(sku);

                //还得把以前的stock删掉，因为skuid还在
//                stockMapper.deleteBySkuId(sku.getId());  //你得从久数据中删除，在外面删除才行,上面已经搞定了

                Long skuId = sku.getId();
                Stock stock = sku.getStock();
                stock.setSkuId(skuId);
                stockMapper.insertSelective(stock);
            });
        }

//        Example example = new Example(Sku.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("spuId",spuBO.getId());
//        List<Sku> skuList = skuMapper.selectByExample(example);

//        skuList.forEach(sku -> {
//            //你的根据spuId到数据库查出Sku，才有主键
////            Long skuId = sku.getId();
////            Stock stock = stockMapper.selectByPrimaryKey(skuId);
////            sku.setStock(stock);
////            sku.setSpuId(spu.getId());
//            List<Sku> skuList2 = spuBO.getSkus();
//            skuList2.forEach(sku2 -> {
////表单提交是没有skuId和stockId的，要么想办法把原来id设置进去，再update；要么根据spuId直接删除,设置关系再保存，重新生成id
//                    }
//                    skuMapper.selectByExample()
//                    sku.setCreateTime(new Date());
//            sku.setLastUpdateTime(sku.getCreateTime());
//            skuMapper.updateByPrimaryKeySelective(sku);//因为这里根本没有主键，传过来就是空的，所以数据库没有插入
//
//            Stock stock = sku.getStock();
//            stock.setSkuId(sku.getId());
//            stockMapper.updateByPrimaryKeySelective(stock);
//        });

    }

    public List<Sku> getSkuListBySpuId(Long spuId) {
//        Example example = new Example(Sku.class);
//        Example.Criteria criteria = example.createCriteria();
//        criteria.andEqualTo("spuId",spuId);
//        List<Sku> skuList = skuMapper.selectByExample(example);

        List<Sku> skuList = skuMapper.selectSkuListBySpuId(spuId);

        skuList.forEach(sku -> {
            Long skuId = sku.getId();

            Stock stock = stockMapper.selectByPrimaryKey(skuId);
            sku.setStock(stock);
        });
        return skuList;
    }

    public List<Sku> getSkuListBySpuBOId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);

        List<Sku> skuList = skuMapper.select(sku);
        return skuList;
    }
}
