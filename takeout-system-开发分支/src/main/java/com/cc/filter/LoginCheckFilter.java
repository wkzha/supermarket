//package com.cc.filter;
//
//import com.alibaba.fastjson.JSON;
//import com.cc.common.Result;
//import com.cc.utils.BaseContext;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.util.AntPathMatcher;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * 检查用户是否登陆，登陆了才给访问
// */
//@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
//@Slf4j
//public class LoginCheckFilter implements Filter {
//
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    //调用Spring核心包的字符串匹配类
//    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        //强转一下,向下转型
//        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
//        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
//        //获取url
//        String requestUrl = httpServletRequest.getRequestURI();
//        //定义可以放行的请求url
//        String[] urls = {
//            "/employee/login",
//            "/employee/login",
//            "/backend/**",
//            "/front/**",
//            "/user/sendMsg",
//            "/user/login"
//        };
//        //判断这个路径是否直接放行
//        Boolean cheakUrl = checkUrl(urls, requestUrl);
//        //不需要处理直接放行
//        if (cheakUrl){
//            log.info("匹配到了{}",requestUrl);
//            filterChain.doFilter(httpServletRequest, httpServletResponse);
//            //放行完了直接结束就行
//            return;
//        }
//        //判断用户已经登陆可以放行（PC后台版）
//
//        if (redisTemplate.opsForValue().get("employee")!= null){
//            log.info("后台用户已登录");
//            filterChain.doFilter(httpServletRequest, httpServletResponse);
//            //获取当前新增操作人员的id
//            Long empId= (Long) redisTemplate.opsForValue().get("employee");
//            //存入LocalThread
//            BaseContext.setCurrentId(empId);
//            //放行完了直接结束就行
//            return;
//        }//判断用户已经登陆可以放行（移动端前台版）
//        if (redisTemplate.opsForValue().get("user") != null){
//            log.info("前台用户已登录");
//            filterChain.doFilter(httpServletRequest, httpServletResponse);
//            //获取当前新增操作人员的id
//            Long userId= (Long) redisTemplate.opsForValue().get("user");
//            //存入LocalThread
//            BaseContext.setCurrentId(userId);
//            //放行完了直接结束就行
//            return;
//        }
//        //没有登陆，跳转到登陆页面
//        //前端有拦截器完成跳转页面，所以我们用输入流写个json来触发前端的拦截器完成跳转
//        httpServletResponse.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
//        log.info("拦截，交由前端跳转");
//        return;
//    }
//
//    /**
//     * @param urls 之前定义的可以放行的url地址数组
//     * @param requestUrl 客户端打来的url地址
//     * @return  返回值boolean值，true的话就是我们可以放行的目标
//     */
//    public boolean checkUrl(String []urls,String requestUrl){
//        Boolean matchUrlResult = true;
//        //遍历的同时调用PATH_MATCHER来对路径进行匹配
//        for (String currUrl : urls) {
//            matchUrlResult=PATH_MATCHER.match(currUrl, requestUrl);
//            if (matchUrlResult){
//                //匹配到了可以放行的路径，直接放行
//                return true;
//            }
//        }
//        //否则就是没有匹配到，不予放行
//        return false;
//    }
//
//}
package com.cc.filter;

import com.alibaba.fastjson.JSON;
import com.cc.common.Result;
import com.cc.utils.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 检查用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();// /backend/index.html

        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",//登录不拦截
                "/employee/logout",//退出登录不拦截
                "/backend/**",//backend的静态资源不拦截
                "/front/**",//front的静态资源不拦截
                "/user/sendMsg",//移动端发送短信不拦截
                "/user/sendCode",//移动端发送短信不拦截
                "/user/login",//移动端登录不拦截
                "/alipay/**",
                "/alipay/pay",

        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4-1、判断后端登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户后端已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            //将用户id放入线程
            long empId = (long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4-2、判断移动端登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户移动端已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }



        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        //未登录的跳转页面在前端的request.js中实现，将R对象转成JSON后传给这个js文件
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
        return;




    }

    @Override
    public void destroy() {

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }

}