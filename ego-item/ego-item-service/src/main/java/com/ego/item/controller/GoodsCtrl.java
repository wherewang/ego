package com.ego.item.controller;

import com.ego.common.pojo.CartDto;
import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.bo.SpuBO;
import com.ego.item.pojo.Sku;
import com.ego.item.pojo.SpuDetail;
import com.ego.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsCtrl {
    @Autowired
    private GoodsService goodsService;
//    http://api.ego.com/api/item/goods/spu/page?key=&saleable=1&page=1&rows=5
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBO>> page(
        @RequestParam("key") String key,
        @RequestParam("saleable") Boolean saleable,
        @RequestParam(name = "page",defaultValue = "1") Integer pageNo,
        @RequestParam(name = "rows",defaultValue = "5") Integer pageSize
    )
    {
            PageResult<SpuBO> pageResult = goodsService.page(key,saleable,pageNo,pageSize);
            if(pageResult ==null || pageResult.getItems().size()==0)
            {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(pageResult);
    }
//    http://api.ego.com/api/item/goods
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody SpuBO spuBO){
        goodsService.save(spuBO);
        return ResponseEntity.ok().build();
    }

    //api.ego.com/api/item/goods?id=2
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("id") Long id){
        goodsService.delete(id);
        return ResponseEntity.ok().build();
    }
//    http://api.ego.com/api/item/goods/spu/detail/245
//    @GetMapping("/spu/detail/{spu_id}")
//    public ResponseEntity<SpuBO> detail(@PathVariable("spu_id") Long id)
//    {
//       SpuBO spuBO  = goodsService.getDetail(id);
//       if(spuBO == null)
//       {
//           return ResponseEntity.notFound().build();
//       }
//       return ResponseEntity.ok(spuBO);
//    }
    @GetMapping("/spu/detail/{spu_id}")
    public ResponseEntity<SpuDetail> detail(@PathVariable("spu_id") Long id)
    {
        SpuDetail spuDetail  = goodsService.getDetail(id);
        if(spuDetail == null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

//    Request URL: http://api.ego.com/api/item/goods
//    Request Method: PUT
    @PutMapping
    public ResponseEntity<Void> put(@RequestBody SpuBO spuBO)
    {
        goodsService.put(spuBO);

        return ResponseEntity.ok(null);
    }

//    Request URL: http://api.ego.com/api/item/goods/sku/list?id=245
//    Request Method: GET
    @GetMapping("/sku/list")            /*@PathParam*/
    public ResponseEntity<List<Sku>> getSku(@RequestParam("id") Long spuId)
    {
        List<Sku> skuList = goodsService.getSkuListBySpuId(spuId);
        if(skuList == null || skuList.size()==0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skuList);
    }

    @GetMapping("/list/{spuId}")
    public ResponseEntity<List<Sku>> selectSkuListBySpuBOId(@PathVariable("spuId") Long spuId)
    {
        List<Sku> skuList = goodsService.getSkuListBySpuBOId(spuId);
        if(skuList==null || skuList.size()==0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skuList);
    }

    @GetMapping("/skuDetail/{spuId}")
    public ResponseEntity<String> selectSkuDetailBySpuBOId(@PathVariable("spuId") Long id)
    {
        SpuDetail spuDetail = goodsService.getDetail(id);
        if(spuDetail==null )
        {
            return ResponseEntity.notFound().build();
        }
        String specifications = spuDetail.getSpecifications();
        return ResponseEntity.ok(specifications);
    }


    @GetMapping("/spuBo/{spuId}")
    public ResponseEntity<SpuBO> queryGoodsById(@PathVariable("spuId")Long spuId)
    {
        SpuBO spuBO = goodsService.queryGoodsById(spuId);
        if(spuBO==null )
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuBO);
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId")Long skuId)
    {
        Sku sku = goodsService.querySkuBySkuId(skuId);

        if(sku==null)
        {
            return  ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(sku);
    }


    /**
     * 减库存
     * @param cartDtos
     * @return
     */
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDto> cartDtos){
    goodsService.decreaseStock(cartDtos);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("stock/seckill/decrease")
    public ResponseEntity<Void> decreaseSeckillStock(@RequestBody CartDto cartDTO){
        goodsService.decreaseSeckillStock(cartDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
