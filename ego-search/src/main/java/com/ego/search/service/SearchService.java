package com.ego.search.service;

import com.ego.common.utils.NumberUtils;
import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import com.ego.item.pojo.Category;
import com.ego.item.pojo.Sku;
import com.ego.item.pojo.bo.SpuBO;
import com.ego.search.client.BrandClient;
import com.ego.search.client.CategoryClient;
import com.ego.search.client.GoodsClient;
import com.ego.search.client.SpecClient;
import com.ego.search.dao.GoodsRepository;
import com.ego.search.pojo.Goods;
import com.ego.search.pojo.bo.SearchRequest;
import com.ego.search.pojo.bo.SearchResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {
    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /*import com.fasterxml.jackson.databind.ObjectMapper;*/
    private ObjectMapper objectMapper = new ObjectMapper();

    public Goods buildGoods(SpuBO spuBO) {
        Goods goods = new Goods();
        try {
            goods.setId(spuBO.getId());
            String cnames = categoryClient.selectCnamesByCid(Arrays.asList(spuBO.getCid1(), spuBO.getCid2(), spuBO.getCid3())).getBody();
            String bname = brandClient.selectBnameByBid(spuBO.getBrandId()).getBody().getName();
            String all = spuBO.getTitle() + cnames + bname;

            goods.setAll(all);
            goods.setSubTitle(spuBO.getSubTitle());
            goods.setBrandId(spuBO.getBrandId());
            goods.setCid1(spuBO.getCid1());
            goods.setCid2(spuBO.getCid2());
            goods.setCid3(spuBO.getCid3());
            goods.setCreateTime(spuBO.getCreateTime());

            List<Long> prices = new ArrayList<>();
            List<Sku> skuList = goodsClient.selectSkuListBySpuBOId(spuBO.getId()).getBody();
            skuList.forEach(sku -> {
                prices.add(sku.getPrice());
            });
            //skuList --> json
            String skus = objectMapper.writeValueAsString(skuList);

            goods.setPrice(prices);
            goods.setSkus(skus);

            Map<String,Object> specsMap = new HashMap<>();
            String specifications = goodsClient.selectSkuDetailBySpuBOId(spuBO.getId()).getBody();
            List<Map<String,Object>> specsList = objectMapper.readValue(specifications,new TypeReference<List<Map<String,Object>>>(){});
            specsList.forEach(spec->{
                List<Map<String,Object>> params = (List<Map<String,Object>>)spec.get("params");
                params.forEach(param->{
                        if((boolean)param.get("searchable"))
                        {
                            if(param.get("v")!=null){
                                specsMap.put(param.get("k").toString(),param.get("v"));
                            }
                            else {
                                specsMap.put(param.get("k").toString(),param.get("options"));
                            }
                        }
                });
            });

            goods.setSpecs(specsMap);
        }catch (Exception e)
        {
            log.error("SpuBO转Goods出现了什么异常:{}",e.getMessage());
//            e.p
        }
        return goods;
    }

    public PageResult<Goods> searchPage(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        Integer page = searchRequest.getPage();
        if(StringUtils.isBlank(key))
        {
            return null;
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //指定字段查询
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id","skus","subTitle"},null));
        //分词查询 allskus
        QueryBuilder queryBuilder = this.buildBasicQueryWithFilter(searchRequest);
        nativeSearchQueryBuilder.withQuery(queryBuilder);
//        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("all",key));
        //分页查询
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page-1,searchRequest.getSize()));

        //聚合
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("分类").field("cid3"));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("品牌").field("brandId"));

        AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>)goodsRepository.search(nativeSearchQueryBuilder.build());

        List<Category> categories = getCategoryAggResult(aggregatedPage);
        List<Brand> brands = getBrandAggResult(aggregatedPage);

        //聚合查询其他规格参数
        List<Map<String,Object>> specs = null;
        if(categories != null && categories.size()>0)
        {
             specs = getSpecs(categories.get(0),queryBuilder);
        }


        return new SearchResponse(aggregatedPage.getTotalElements(),Long.valueOf(aggregatedPage.getTotalPages()),
                aggregatedPage.getContent(),categories,brands,specs);
    }

    /**
     * 查询其他规格参数以及选项
     * @param category
     * @param queryBuilder
     * @return
     */
    private List<Map<String, Object>> getSpecs(Category category, QueryBuilder queryBuilder) {
        List<Map<String,Object>> result = null;
        try {
                //1.根据类别id查询到对应的规格参数
                String specjson = specClient.querySpecificationByCid(category.getId()).getBody();
                //将json  -->  对象 List<Map<String,Object>>
                List<Map<String, Object>> specMap = objectMapper.readValue(specjson, new TypeReference<List<Map<String, Object>>>() {
                });

                //2.区分出字符 ，数据的参数
                Set<String> strSpecs= new HashSet<>();
                    //数字类型，k:参数名称   v:单位
                Map<String,String> numSpecs = new HashMap<>();
                specMap.forEach(param->{
                    List<Map<String,Object>> specs = (List<Map<String,Object>>)param.get("params");
                    specs.forEach(spec->{
                        if((boolean)spec.get("searchable"))
                        {
                            String k = (String)spec.get("k");
                            if(spec.get("numerical")!=null && (boolean)spec.get("numerical"))  //“内存”就是false
                            {
                                numSpecs.put(k,(String)spec.get("unit"));
                            }
                            else
                            {
                                strSpecs.add(k);
                            }
                        }

                    });
                });
                //3.字符-->term词条聚合
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

            nativeSearchQueryBuilder.withQuery(queryBuilder);//加上查询条件

            nativeSearchQueryBuilder.withPageable(PageRequest.of(1,1));
            strSpecs.forEach(spec->{
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(spec).field("specs."+spec+".keyword"));
            });
            //4.数字-->histogram阶梯聚合    计算间隔
                Map<String,Double> numIntervalMap = getNumIntervalMap(numSpecs);
                numSpecs.forEach((k,v)->{
                    nativeSearchQueryBuilder.addAggregation(AggregationBuilders.histogram(k).field("specs."+k).interval(numIntervalMap.get(k)).minDocCount(1));
                });
                //5.查询聚合结果
            Map<String, Aggregation> allAggResult = elasticsearchTemplate.query(nativeSearchQueryBuilder.build(), searchResponse -> searchResponse.getAggregations().asMap());
            //6.解析聚合结果（数字型需要单独处理，要有间隔  0-100）
                result = parseAggResult(allAggResult,strSpecs,numSpecs,numIntervalMap);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析聚合结果（包括数据和字符）
     * @param allAggResult
     * @param strSpecs
     * @param numSpecs
     * @param numIntervalMap
     * @return
     */
    private List<Map<String, Object>> parseAggResult(Map<String, Aggregation> allAggResult, Set<String> strSpecs, Map<String, String> numSpecs, Map<String, Double> numIntervalMap) {
        List<Map<String,Object>> result = new ArrayList<>();
        //1.解析字符型
        strSpecs.forEach(spec->{
            Map<String,Object> map = new HashMap<>();
            StringTerms stringTerms = (StringTerms) allAggResult.get(spec);
            List<String> options = stringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            map.put("k",spec);
            map.put("options",options);
            result.add(map);
        });
        //2.解析数字型
        numSpecs.forEach((spec,unit)->{
            Map<String,Object> map = new HashMap<>();
            InternalHistogram internalHistogram = (InternalHistogram)allAggResult.get(spec);
            //0 400 800  0-400 400-800 800-1200
            List<String> list = internalHistogram.getBuckets().stream().map(bucket -> {
                Double begin = (Double) bucket.getKey();
                Double end = begin + numIntervalMap.get(spec);
                //判断是否是整型
                if (NumberUtils.isInt(begin) && NumberUtils.isInt(end)) {
                    return begin + "-" + end;
                } else {
                    //小数点，就保留一位小数点
                    return NumberUtils.scale(begin, 1) + "-" + NumberUtils.scale(end, 1);
                }

            }).collect(Collectors.toList());
            map.put("k",spec);
            map.put("options",list);
            map.put("unit",unit);
            result.add(map);
        });
        return result;
    }

    /**
     * 获取数字型参数的间隔
     * @param numSpecs
     * @return
     */
    private Map<String, Double> getNumIntervalMap(Map<String, String> numSpecs) {
        Map<String,Double> result = new HashMap<>();
        //1.去es中查询每个数字参数的min,max,sum
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withPageable(PageRequest.of(1,1));//不要数据 0条数据

        //聚合
        numSpecs.keySet().forEach(spec->{
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.stats(spec).field("specs."+spec));
        });
        Map<String, Aggregation> aggMap = elasticsearchTemplate.query(nativeSearchQueryBuilder.build(), searchResponse -> {
            return searchResponse.getAggregations().asMap();
        });
        //2.单独计算每个数字参数的间隔
        numSpecs.keySet().forEach(spec->{
            InternalStats internalStats = (InternalStats) aggMap.get(spec);
            Double interval = NumberUtils.getInterval(internalStats.getMin(), internalStats.getMax(), internalStats.getSum());
            result.put(spec,interval);
        });
        return result;
    }

    /**
     *获取分类聚合结果
     * @param aggregatedPage
     * @return
     */
    private List<Category> getCategoryAggResult(AggregatedPage<Goods> aggregatedPage) {
        LongTerms longterms = (LongTerms) aggregatedPage.getAggregation("分类");
        List<Long> cids = longterms.getBuckets().stream().map(bucket -> (Long) bucket.getKey()).collect(Collectors.toList());
        //到微服务中通过cids去查询List<Category>
        String[] cnames = categoryClient.selectCnamesByCid(cids).getBody().split(",");

        //在多线程环境，保证不被其他线程干扰（原子操作）
        AtomicInteger i = new AtomicInteger();
        List<Category> categoryList = cids.stream().map(cid -> {
            Category category = new Category();
            category.setId(cid);
            category.setName(cnames[i.getAndIncrement()]);
            return category;
        }).collect(Collectors.toList());
        return categoryList;
    }

    /**
     * 获取品牌聚合结果
     * @param aggregatedPage
     * @return
     */
    private List<Brand> getBrandAggResult(AggregatedPage<Goods> aggregatedPage) {
        LongTerms longTerms = (LongTerms)aggregatedPage.getAggregation("品牌");
        List<Long> bids = longTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        return brandClient.queryListByBids(bids).getBody();
    }

    /**
     * 创建单个商品的索引
     * @param id
     */
//    public void createIndex(Long id) {
//        SpuBO spuBo = goodsClient.queryGoodsById(id).getBody();
//        Goods goods = buildGoods(spuBo);
//        goodsRepository.save(goods);
//    }


    /**
     * 构建带过滤条件的基本查询
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        //过滤条件构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //整理过滤条件
        Map<String,String> filter = searchRequest.getFilter();
        for (Map.Entry<String,String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String regex = "^(\\d+\\.?\\d*)-(\\d+\\.?\\d*)$";
            if (!"key".equals(key)) {
                if ("price".equals(key)){
                    if (!value.contains("元以上")) {
                        String[] nums = StringUtils.substringBefore(value, "元").split("-");
                        filterQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(Double.valueOf(nums[0]) * 100).lt(Double.valueOf(nums[1]) * 100));
                    }else {
                        String num = StringUtils.substringBefore(value,"元以上");
                        filterQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(Double.valueOf(num)*100));
                    }
                }else {
                    if (value.matches(regex)) {
                        Double[] nums = NumberUtils.searchNumber(value, regex);
                        //数值类型进行范围查询   lt:小于  gte:大于等于
                        filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + key).gte(nums[0]).lt(nums[1]));
                    } else {
                        //商品分类和品牌要特殊处理
                        if (key.equals("分类"))
                        {
                            key = "cid3";
                        }
                        else if(key.equals("品牌"))
                        {
                            key = "brandId";
                        }
                        else{
                            key = "specs." + key + ".keyword";
                        }
                        //字符串类型，进行term查询
                        filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
                    }
                }
            } else {
                break;
            }
        }
        //添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }


}
