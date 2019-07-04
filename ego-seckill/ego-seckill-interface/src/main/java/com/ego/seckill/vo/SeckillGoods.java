package com.ego.seckill.vo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @Feature: 秒杀商品对象
 */
@Data
@Table(name = "tb_seckill_sku")
public class SeckillGoods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 秒杀商品的id
     */
    private Long skuId;
    /**
     * 秒杀开始时间
     */
    private Date startTime;
    /**
     * 秒杀结束时间
     */
    private Date endTime;
    /**
     * 原价价格
     */
    private Long price;
    /**
     * 秒杀价格
     */
    private Long seckillPrice;
    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 是否可以秒杀
     */
    private Boolean enable;

    /**
     * 秒杀库存
     */
//    @JsonIgnore
    @Transient
    private Integer stock;

//    @JsonIgnore
    @Transient
    private Integer seckillTotal;

    /**
     * 当前服务器时间
     */
    @Transient
    private Date currentTime;

    @Override
    public String toString() {
        return "SeckillGoods{" +
                "id=" + id +
                ", skuId=" + skuId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", seckillPrice=" + seckillPrice +
                ", title='" + title + '\'' +
                ", image='" + image + '\'' +
                ", enable=" + enable +
                ", stock=" + stock +
                ", seckillTotal=" + seckillTotal +
                '}';
    }
}
