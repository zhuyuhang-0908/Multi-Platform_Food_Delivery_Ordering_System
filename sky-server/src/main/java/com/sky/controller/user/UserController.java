package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

//告诉 Spring：这是一个 Web 控制器类。
//作用
//这个类中的方法返回的对象会自动转成 JSON 响应给前端。
@RestController
//给这个控制器统一加上访问路径前缀。
@RequestMapping("/user/user")
//给 Swagger 文档中的这个控制器分组、命名。
@Api(tags = "C端用户相关接口")
//自动生成日志对象 log。   不加这个不能使用log.
@Slf4j
public class UserController {

//    调用业务层完成微信登录。
    @Autowired
    private UserService userService;
//    拿到 JWT 配置信息，比如：
//    用户端密钥
//    用户端 token 过期时间
    @Autowired
    private JwtProperties jwtProperties;

    /**
     *微信登陆
     * @param userLoginDTO
     * @return
     */
//    因为登录一般会提交数据，而且账号凭证不适合直接拼到 URL 上，所以通常用 POST。
    @PostMapping("/login")
//    在 Swagger 文档里说明这个接口是“微信登录”。
    @ApiOperation("微信登录")
//    为什么要加 @RequestBody？
//因为登录参数通常在请求体中以 JSON 方式传递，不加的话 Spring 不会按 JSON 去解析。
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信用户登录：{}",userLoginDTO.getCode());

        //微信登陆
        User user = userService.wxLogin(userLoginDTO);

        //为微信用户生成jwt令牌
        //为什么要 token？
        //登录成功后，后续请求不能每次都重新登录，所以服务器会给前端发一个 token。前端以后带着 token 来请求，服务器据此识别用户身份。
//        claims 是 JWT 里面存放的“载荷数据”，也就是 token 中保存的信息。
//        claims中为什么要放用户 id？
//        因为以后前端带着 token 访问接口时，后端解析 token 就能知道“当前是谁”。
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(),claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }
}
