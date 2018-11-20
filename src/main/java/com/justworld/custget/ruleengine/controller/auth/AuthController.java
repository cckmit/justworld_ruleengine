package com.justworld.custget.ruleengine.controller.auth;

import com.justworld.custget.ruleengine.common.BaseResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @RequestMapping("/loginSuccess")
    @ResponseBody
    public BaseResult<String> loginSuccess(){
        return BaseResult.build((p)->{return "token";},null);
    }
}
