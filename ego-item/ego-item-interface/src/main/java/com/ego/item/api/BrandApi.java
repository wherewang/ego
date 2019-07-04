package com.ego.item.api;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.CacheRequest;
import java.util.List;

@RequestMapping("/brand")
public interface BrandApi {

    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> page(
            @RequestParam(name = "pageNo",defaultValue = "1")Integer pageNo,
            @RequestParam(name = "pageSize",defaultValue = "5")Integer pageSize,
            @RequestParam(name = "descending",defaultValue = "true")Boolean descending,
            @RequestParam(name = "sortBy")String sortBy,
            @RequestParam(name = "key",required = false)String key
    );

    @PostMapping
    public ResponseEntity<Void> save(Brand brand, @RequestParam("cids") List<Long>cids);

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("id") Long id);

    @PutMapping
    public ResponseEntity<Void> put(Brand brand, @RequestParam("cids") List<Long> cids);

//    http://api.ego.com/api/item/brand/cid/80
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> getBrand( @PathVariable("cid") Long cid);

    @GetMapping("bid/{bid}")
    public ResponseEntity<Brand> selectBnameByBid(@PathVariable("bid") Long brandId);

    /**
     * 根据品牌id查询品牌列表
     * @param bids
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Brand>> queryListByBids(@RequestParam("/bids") List<Long> bids);



}
