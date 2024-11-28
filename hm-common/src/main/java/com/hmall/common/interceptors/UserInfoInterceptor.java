package com.hmall.common.interceptors;

import cn.hutool.core.util.StrUtil;
import com.hmall.common.utils.UserContext;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在网关已近进行过登陆的拦截校验了，能到这里说明已经登陆成功或当前页面无需登陆
 * @author axno
 * @date 2024/10/26 21:36
 * @DESCRIPTION
 */
public class UserInfoInterceptor implements HandlerInterceptor {
    /**
     * Controller之前保存userId到ThreadLocal里
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取登陆用户信息  网关存储的header的name是user-info
        String userInfo = request.getHeader("user-info");
        //2.判断是否获取了用户，有的话存入ThreadLocal
        if (StrUtil.isNotBlank(userInfo)) {
            UserContext.setUser(Long.valueOf(userInfo));
        }
        //3.放行
        return true;
    }
    /**
     * controller业务执行完，把userId从ThreadLocal里清除
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清理用户
        UserContext.removeUser();
    }
}
