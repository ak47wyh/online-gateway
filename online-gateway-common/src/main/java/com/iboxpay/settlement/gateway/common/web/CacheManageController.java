package com.iboxpay.settlement.gateway.common.web;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.cache.ICacheService;

//缓存管理
@Controller
@RequestMapping("/manage/cache")
public class CacheManageController {

    @Resource(name = "localCacheService")
    ICacheService localCacheService;

    @Resource(name = "memcachedService")
    ICacheService remoteCacheService;

    @RequestMapping(value = "index.htm", method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/cache/index");
        return mv;
    }

    @RequestMapping(value = "get.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> get(@RequestParam(value = "key") String key, @RequestParam(value = "local") boolean local) {
        Object o;
        if (local) {
            o = localCacheService.get(key);
        } else {
            o = remoteCacheService.get(key);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("object", o == null ? null : o.toString());
        return result;
    }

    @RequestMapping(value = "delete.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(value = "key") String key, @RequestParam(value = "local") boolean local) {
        boolean success;
        if (local) {
            success = localCacheService.delete(key);
        } else {
            success = remoteCacheService.delete(key);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", success);
        return result;
    }

}
