package com.ego.item.api;

        import com.ego.common.pojo.CartDto;
        import com.ego.common.pojo.PageResult;
        import com.ego.item.pojo.Sku;
        import com.ego.item.pojo.Spu;
        import com.ego.item.pojo.bo.SpuBO;
        import com.ego.item.pojo.SpuDetail;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

        import java.net.CacheRequest;
        import java.util.List;

@RequestMapping("/goods")
public interface GoodsApi {

    //    http://api.ego.com/api/item/goods/spu/page?key=&saleable=1&page=1&rows=5
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBO>> page(
            @RequestParam("key") String key,
            @RequestParam("saleable") Boolean saleable,
            @RequestParam(name = "page",defaultValue = "1") Integer pageNo,
            @RequestParam(name = "rows",defaultValue = "5") Integer pageSize
    );

    //    http://api.ego.com/api/item/goods
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody SpuBO spuBO);


    //api.ego.com/api/item/goods?id=2
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("id") Long id);


    //    http://api.ego.com/api/item/goods/spu/detail/245
    @GetMapping("/spu/detail/{spu_id}")
    public ResponseEntity<SpuDetail> detail(@PathVariable("spu_id") Long id);


    //    Request URL: http://api.ego.com/api/item/goods
//    Request Method: PUT
    @PutMapping
    public ResponseEntity<Void> put(@RequestBody SpuBO spuBO);


    //    Request URL: http://api.ego.com/api/item/goods/sku/list?id=245
//    Request Method: GET
    @GetMapping("/sku/list")            /*@PathParam*/
    public ResponseEntity<List<Sku>> getSku(@RequestParam("id") Long spuId);

    @GetMapping("/list/{spuId}")
    public ResponseEntity<List<Sku>> selectSkuListBySpuBOId(@PathVariable("spuId") Long spuId);

    @GetMapping("/skuDetail/{spuId}")
    public ResponseEntity<String> selectSkuDetailBySpuBOId(@PathVariable("spuId") Long id);

    @GetMapping("/spuBo/{spuId}")
    public ResponseEntity<SpuBO> queryGoodsById(@PathVariable("spuId")Long spuId);


    @GetMapping("/sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId")Long skuId);

    /**
     * 减库存
     * @param cartDTOS
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDto> cartDTOS);

    /**
     * 减秒杀库存
     * @param cartDTO
     */
    @PostMapping("stock/seckill/decrease")
    void decreaseSeckillStock(@RequestBody CartDto cartDTO);
}
