package com.ego.item.pojo.bo;

import com.ego.item.pojo.Sku;
import com.ego.item.pojo.Spu;
import com.ego.item.pojo.SpuDetail;
import lombok.Data;

import java.util.List;

@Data
public class SpuBO extends Spu {

    private String categoryName;
    private String brandName;

    private List<Sku> skus;
    private SpuDetail spuDetail;
}
