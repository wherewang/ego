package com.ego.gateway.filter;

import com.ego.auth.utils.JwtUtils;
import com.ego.common.utils.CookieUtils;
import com.ego.gateway.config.FilterProperties;
import com.ego.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 〈鉴权过滤器〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/12
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Component
@EnableConfigurationProperties({FilterProperties.class, JwtProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private FilterProperties filterProperties;

    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        //判断是否需要过滤(除了白名单之外的uri都要过滤)
        //获取当前请求uri
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        // api/auth/verify   /api/auth
        String requestURI = request.getRequestURI();
        boolean result = filterProperties.getAllowPaths().stream().anyMatch(path -> requestURI.startsWith(path));
        return !result;
    }

    @Override
    public Object run() throws ZuulException {
        //鉴权
        //判断token是否正确
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //正确-->放行
        try {
           JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        } catch (Exception e) {
            //e.printStackTrace();
            //不正确 --> 提示没有权限
            //停止访问微服务
            context.setSendZuulResponse(false);  //关闭网关的自动响应
//            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        return null;
    }
}
