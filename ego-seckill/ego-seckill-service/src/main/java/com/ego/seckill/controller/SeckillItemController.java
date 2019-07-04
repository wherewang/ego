package com.ego.seckill.controller;

import com.ego.seckill.service.SeckillService;
import com.ego.seckill.vo.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/4/25
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Controller
public class SeckillItemController {
    @Autowired
    SeckillService seckillService;

    /**
     * 需要通过nginx 直接跳转
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/seckill-item/{id}.html")
    public String item(@PathVariable("id") Long id, Model model) {
        SeckillGoods seckillGoods = seckillService.queryById(id);
        model.addAttribute("seckillGoods", seckillGoods);
        return "seckill-item";
    }



}
