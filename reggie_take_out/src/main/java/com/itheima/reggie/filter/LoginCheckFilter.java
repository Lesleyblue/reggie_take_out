package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
// 检查用户是否已经完成登录
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")  // /* 拦截所有路径
public class LoginCheckFilter implements Filter {
    // 路径匹配器 支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // log.info("拦截到请求:{}",request.getRequestURI());
        // doFilter 方法的作用是将请求和响应对象传递给过滤器链中的下一个过滤器，或在链的最后将请求转发给目标资源
        // filterChain.doFilter(request, response );
    // 1、获取本次请求的URI
        String requestURI =  request.getRequestURI();
        // 定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/Logout",
                "/backend/**",  // **是通配符的方式 希望匹配到/backend/index.html
                "/front/**",
                "/common/**",
                "/user/sendMsg",  // 移动端发送短信
                "/user/login",  // 移动端登录
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs",
                "/doc.html"
        };

        log.info("拦截到请求：{}", requestURI);
    // 2、判断本次请求是否需要处理  --判断请求的路径是否在urls里面
        boolean check = check(urls, requestURI);
    // 3、如果不需要处理，则直接放行
        if (check){
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
    // 4-1、判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") !=null) {
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("employee"));
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
//            long id = Thread.currentThread().getId();
//            log.info("线程id为：{}",id);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-2、判断移动端登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") !=null) {
            log.info("用户已登录，用户id为{}", request.getSession().getAttribute("user"));
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
//            long id = Thread.currentThread().getId();
//            log.info("线程id为：{}",id);
            filterChain.doFilter(request, response);
            return;
        }


        log.info("用户未登录");
    // 5、如果未登录则返回未登录结果,通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN"))); // 前端会收到未登录的提示跳转到登录页
        return;
    }

//    路径匹配，检查本次请求是否需要放行
    public boolean check(String[] urls, String requestURI){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
