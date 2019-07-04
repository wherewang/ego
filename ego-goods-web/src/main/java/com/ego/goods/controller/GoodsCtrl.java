package com.ego.goods.controller;

import com.ego.goods.service.GoodsService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/item")
public class GoodsCtrl {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/{id}.html")
    public String item(@PathVariable("id") Long id, Model model)
    {
        Map<String,Object> modelMap = goodsService.loadModel(id);
        model.addAttribute(modelMap);
        //异步生成静态页面
        goodsService.buildStaticHmtl(modelMap,id);
        return "item";
    }
}
