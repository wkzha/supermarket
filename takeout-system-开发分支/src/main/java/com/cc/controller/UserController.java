package com.cc.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cc.common.Result;
import com.cc.pojo.User;
import com.cc.service.UserService;
import com.cc.utils.SMSUtils;
import com.cc.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * <p>
 * 用户信息 前端控制器
 * </p>
 *
 * @author cc
 * @since 2022-05-30
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

//
//    /**
//     * 验证码发送
//     * @param user 接收用户电话号码
//     * @param session 把验证码存入session，后续登陆验证要用
//     * @return
//     */
//    @PostMapping("/sendMsg")
//    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
//        //获取手机号
//        String userPhone = user.getPhone();
//        //判断手机号是否为空
//        if (StringUtils.isNotEmpty(userPhone)) {
//            //利用验证码生成类来生成验证码
//            String code = ValidateCodeUtils.generateValidateCode4String(4);
//            //这里不太可能去真的发验证码，所以把生成的验证码在后台看一眼就好
//            log.info("手机号Phone:{}   验证码Code:{}",userPhone,code);
//            //如果要发短信应该出现的代码
//            //SMSUtils.sendMessage("外卖", "模板", userPhone, code);
//            //把验证码存入Session，验证用，phone为Key，code为value
//            //session.setAttribute(userPhone, code);
//            //将验证码存入redis，并设置好失效时间为5分钟
//            redisTemplate.opsForValue().set(userPhone, code, 5, TimeUnit.MINUTES);
//
//            return Result.success("验证码发送成功，有效时间为5分钟");
//        }
//
//        return Result.error("验证码发送失败");
//    }
//
//    /**
//     * 用户登录发送验证码
//     *
//     * @param user
//     * @param session
//     * @return
//     */
//    @PostMapping("/sendMsg")
//    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
//        //TODO: 2024/6/11 "验证码短信服务待开发，问题：前端没有发送请求,没有注册阿里云短信套餐";
//        // 获取要登录的手机号。
//        String phone = user.getPhone();
//        // 生成随机验证码。
//        String code = ValidateCodeUtils.generateValidateCode(4).toString();
//        // 通过阿里云api向用户发送短信。
//        SMSUtils.sendMessage("", "", phone, code);
//        // 把验证码存入session域中。
//        session.setAttribute(phone, code);
//        return null;
//    }
//
//
//
//
//    /**
//     * 前台登陆功能
//     * @param userDto 对User类进行了扩展，原有user类没有code属性
//     * @param codeInSession 从session中拿code（验证码），方便后需验证
//     * @return
//     */
//    @PostMapping("/login")
//    public Result<String> login(@RequestBody UserDto userDto, HttpSession codeInSession) {
//        //拿到验证码和手机号
//        String code = userDto.getCode();
//        String phone = userDto.getPhone();
//        //从session中拿到对应的验证码
//        //String tempCode = (String) codeInSession.getAttribute(phone);
//
//        //从Redis中拿验证
//        String tempCode = (String) redisTemplate.opsForValue().get(phone);
//
//        //验证码相等
//        if (code.equals(tempCode) && codeInSession != null) {
//            //是否为新用户，如果是新用户顺手注册了
//            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(User::getPhone, phone);
//            //只能用getOne来匹配，不能用getById，因为没有Id给你匹配，都是空的
//            User user = userService.getOne(lambdaQueryWrapper);
//            if (user==null){
//                //用户不存在，注册一下，注册完放行
//                //用户的ID是有自动生成策略的，不用管
//                user = new User();
//                user.setPhone(phone);
//                user.setStatus(1);
//                userService.save(user);
//            }
//            //把用户的ID存入Session，留给过滤器进行验证放行
//            //codeInSession.setAttribute("user", user.getId());
//
//            //此时已经登陆成功，向Redis中存入userId的信息留给过滤器进行验证放行
//            redisTemplate.opsForValue().set("user", user.getId());
//            //再删掉验证码
//            redisTemplate.delete(phone);
//
//
//            return Result.success("登陆成功，欢迎~");
//        }
//        return Result.error("验证码错误");
//    }

    /**
     * 发送验证码
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestBody User user, HttpSession session){
        //获取移动端输入手机号
        String phone = user.getPhone();
        //手机号不为空，则发送验证码。
        if (StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //log.info("code:{}", code);
            //保存验证码 session
            session.setAttribute(phone, code);
            return Result.success("验证码发送成功");
        }
        return Result.error("验证码发送失败");
    }


//    /**
//     * 用户登录发送验证码
//     *
//     * @param user
//     * @param session
//     * @return
//     */
//    @PostMapping("/sendMsg")
//    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
//        //TODO: 2024/6/11 验证码短信服务待开发，问题：前端没有发送请求,没有注册阿里云短信套餐。
//        // 获取要登录的手机号。
//        String phone = user.getPhone();
//        // 生成随机验证码。
//        String code = ValidateCodeUtils.generateValidateCode(4).toString();
//        // 通过阿里云api向用户发送短信。
//        SMSUtils.sendMessage("", "", phone, code);
//        // 把验证码存入session域中。
////    session.setAttribute(phone, code);
//        //把验证码存入redis中。
//        redisTemplate.opsForValue().set(phone,code,5L, TimeUnit.MINUTES);
//        return null;
//
//    }

    /**
     * 发送手机短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的 4 位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("code={}", code);

            //调用阿里云提供的短信服务 API 完成发送短信
            SMSUtils.sendMessage("今天你吃了吗外卖","SMS_300255223",phone,code);

            //需要将生成的验证码保存到 Session
            session.setAttribute(phone, code);

            return Result.success("手机验证码短信发送成功");
        }

        return Result.error("短信发送失败");
    }


//    /**
//     * 移动端客户登录
//     *
//     * @param map
//     * @param session
//     * @return
//     */
//    @PostMapping("/login")
//    public Result<User> login(@RequestBody Map map, HttpSession session) {
//        log.info(map.toString());
//        // 取出电话号。
//        String phone = map.get("phone").toString();
//        // 取出map中的验证码。
//        // 判断验证码是否与sesstion中的验证码相等，
//        // 如果相等,则登录成功。
//        // 进一步判断手机号是否为新用户，
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(User::getPhone, phone);
//        User user = userService.getOne(queryWrapper);
//        session.setAttribute("user", user.getId());
//        // 如果为新用户则自动为此新用户进行注册。
//        if (user == null) {
//            User user1 = new User();
//            user1.setPhone(phone);
//            userService.save(user1);
//            // 把id存入session域中，方便过滤器筛选。
//            session.setAttribute("user", user1.getId());
//            //如果成功就删除验证码。
//            redisTemplate.delete(phone);
//            return Result.success(user1);
//        }
//        //从redis中获取验证码。
//        String code = (String) redisTemplate.opsForValue().get(phone);
//
//        // 如果不相等返回登录失败信息
//        return Result.error("登录失败");
//    }
//
//    @PostMapping("/login")
//    public Result<String> login(@RequestBody Map map){
//        //获取手机号与验证码
//        log.info("map:{}", map);
//        return Result.success("");
//    }

    //6.21注释
//    /**
//     * 移动端登录
//     * @param map
//     * @param session
//     * @return
//     */
//    @PostMapping("/login")
//    public Result<User> login(@RequestBody Map map, HttpSession session){
//        //获取移动端输入的手机号与验证码
//        log.info("map:{}", map);
//        String phone = map.get("phone").toString();
//        String code = map.get("code").toString();
//
//        //从session中获取生成的验证码
//        //String codeVali = session.getAttribute(phone).toString();
//
//        //判断验证码是否正确
//        if (code.equals(code)){
//            //正确，成功登录 更加手机号获取用户信息
//            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(User::getPhone, phone);
//            User user = userService.getOne(queryWrapper);
//
//            //判断账号是否被禁用
//            if (user.getStatus() == 0){
//                return Result.error("账号已禁用");
//            }
//
//            //不存在，则注册
//            if (user == null){
//                user = new User();
//                user.setPhone(phone);
//                user.setStatus(1);
//                userService.save(user);
//            }
//
//            //保存登录信息,防止过滤
//            session.setAttribute("user", user.getId());
//            return Result.success(user);
//        }
//
//        return Result.error("验证码错误");
//    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取保存的验证码(页面提交的验证码和Session中保存的验证码比对)
        Object codeInSession = session.getAttribute(phone);

        //进行验证码的比对
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果能够比对成功，说明登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);

            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //判断手机号对应的用户是否为新用户，如果是新用户就自动注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);

            }
            session.setAttribute("user",user.getId());
            return Result.success(user);
        }
        return Result.error("登录失败");
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @PostMapping("loginout")
    public Result<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return Result.success("退出成功");
    }




}



