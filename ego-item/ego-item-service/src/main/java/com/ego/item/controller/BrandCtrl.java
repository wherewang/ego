package com.ego.item.controller;

import com.ego.common.pojo.PageResult;
import com.ego.item.pojo.Brand;
import com.ego.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandCtrl {
    @Autowired
    private BrandService brandService;

    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> page(
            @RequestParam(name = "pageNo",defaultValue = "1")Integer pageNo,
            @RequestParam(name = "pageSize",defaultValue = "5")Integer pageSize,
            @RequestParam(name = "descending",defaultValue = "true")Boolean descending,
            @RequestParam(name = "sortBy")String sortBy,
            @RequestParam(name = "key",required = false)String key
    ){
        PageResult<Brand> pageResult = brandService.page(pageNo,pageSize,descending,sortBy,key);

        if(pageResult==null || pageResult.getItems().size() ==0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping
    public ResponseEntity<Void> save(Brand brand, @RequestParam("cids") List<Long>cids){
        brandService.save(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("id") Long id){
        brandService.delete(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> put(Brand brand, @RequestParam("cids") List<Long>cids){
        brandService.put(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    http://api.ego.com/api/item/brand/cid/80

    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> getBrand( @PathVariable("cid") Long cid){
        List<Brand> brandList = brandService.getBrandListBycid(cid);
        if(brandList ==null || brandList.size()==0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brandList);
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<Brand> selectBnameByBid(@PathVariable("bid") Long brandId)
    {
        Brand brand = brandService.getBrandByBid(brandId);
        if(brand ==null)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Brand>> queryListByBids(@RequestParam("/bids") List<Long> bids)
    {
        List<Brand> brandList = brandService.getBrandListBybids(bids);
        if(brandList ==null || brandList.size()==0)
        {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brandList);
    }
}
