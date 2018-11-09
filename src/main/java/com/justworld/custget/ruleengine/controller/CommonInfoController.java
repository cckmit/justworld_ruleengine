package com.justworld.custget.ruleengine.controller;

import com.justworld.custget.ruleengine.common.BaseResult;
import com.justworld.custget.ruleengine.dao.DicDAO;
import com.justworld.custget.ruleengine.dao.RegionDAO;
import com.justworld.custget.ruleengine.service.bo.Dic;
import com.justworld.custget.ruleengine.service.bo.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基础信息
 */
@Slf4j
@Controller
@RequestMapping(value = "/commoninfo")
@CrossOrigin
public class CommonInfoController {

    @Autowired
    private RegionDAO regionDAO;

    @Autowired
    private DicDAO dicDAO;


    @ResponseBody
    @PostMapping(value = "/region/queryRegionList/{maxLevel}")
    public BaseResult<List<Region>> queryRegionList(@PathVariable("maxLevel") int maxLevel){

        return BaseResult.build(l->regionDAO.queryList(l),maxLevel);
    }

    @PostMapping(value = "/dic/getDic/{dicGroup}")
    @ResponseBody
    public BaseResult<List<Dic>> getDic(@PathVariable("dicGroup")String dicGroup){
        return BaseResult.build(g->dicDAO.queryDicGroup(g),dicGroup);
    }

    @PostMapping(value = "/dic/addAiUsername")
    @ResponseBody
    public BaseResult addDicAiUsername(@RequestParam("dicName") String dicName){
        Dic dic = new Dic();
        dic.setDicGroup("AI_USERNAME");
        dic.setGroupName("AI用户名");
        dic.setDicValue(dicName);
        dic.setDicName(dicName);
        dicDAO.insert(dic);
        return BaseResult.build(d -> dicDAO.insert(d),dic);
    }


}