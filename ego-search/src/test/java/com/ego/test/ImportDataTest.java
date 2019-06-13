package com.ego.test;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.bo.SpuBO;
import com.ego.search.EgoSearchService;
import com.ego.search.client.GoodsClient;
import com.ego.search.dao.GoodsRepository;
import com.ego.search.pojo.Goods;
import com.ego.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EgoSearchService.class)  //它要找程序的入口,由于你的包名没有对应上“入口”的包名
//@SpringBootTest
public class ImportDataTest {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void createIndex(){
        //创建索引
        this.elasticsearchTemplate.createIndex(Goods.class);
        //配置映射
        this.elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void importData(){
        int size =0;
        int pageNo = 1;
        int pageSize = 10;

        do {
            PageResult<SpuBO> pageResult = goodsClient.page("", true, pageNo++, pageSize).getBody();
            List<SpuBO> spuBOList = pageResult.getItems();
            size = spuBOList.size();

            ArrayList<Goods> goodList = new ArrayList<>();
            spuBOList.forEach(spuBO -> {
                Goods goods = searchService.buildGoods(spuBO);
                goodList.add(goods);
            });
            goodsRepository.saveAll(goodList);
        }while(size==10);

    }


}
