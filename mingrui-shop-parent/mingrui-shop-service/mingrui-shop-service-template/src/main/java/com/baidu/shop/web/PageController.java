package com.baidu.shop.web;

import com.baidu.shop.service.PageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @ClassName PageController
 * @Description: TODO
 * @Author wangyue
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Controller
//@RequestMapping(value = "/item")
public class PageController {

    //@Resource
    private PageService pageService;

    //@GetMapping(value = "/{spuId}.html")
    public String test(@PathVariable Integer spuId, ModelMap modelMap){

        Map<String,Object> map = pageService.getGoodsInfo(spuId);
        modelMap.putAll(map);

        return "item";
    }

}
