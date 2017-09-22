package com.ukefu.ask.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.common.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ukefu.ask.web.model.User;
import com.ukefu.ask.web.model.UserHistory;
import com.ukefu.core.UKDataContext;
import com.ukefu.util.BrowserClient;
import com.ukefu.util.CheckMobile;
import com.ukefu.util.IP;
import com.ukefu.util.IPTools;
import com.ukefu.util.Menu;
import com.ukefu.util.UKTools;

public class UserInterceptorHandler extends HandlerInterceptorAdapter {
	
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
    	boolean filter = false; 
        User user = (User) request.getSession(true).getAttribute(UKDataContext.USER_SESSION_NAME) ;
        HandlerMethod  handlerMethod = (HandlerMethod ) handler ;
        Menu menu = handlerMethod.getMethod().getAnnotation(Menu.class) ;
        if(user != null || (menu!=null && menu.access()) || handlerMethod.getBean() instanceof BasicErrorController){
        	filter = true;
        }
        
        if(!filter){
        	response.sendRedirect("/login.html?referer="+java.net.URLEncoder.encode(request.getRequestURL().toString() , "UTF-8"));
        }else if(menu!=null && menu.admin() == true && !"0".equals(user.getUsertype())){	//非系统管理员 访问系统管理URL
        	response.sendRedirect("/?referer="+java.net.URLEncoder.encode(request.getRequestURL().toString() , "UTF-8"));
        }
        request.setAttribute("starttime", System.currentTimeMillis());  
        return filter ; 
    }

    public void postHandle(HttpServletRequest arg0, HttpServletResponse response, Object arg2,
            ModelAndView view) throws Exception {
    	if(view!=null){
	    	User user = (User) arg0.getSession().getAttribute(UKDataContext.USER_SESSION_NAME) ;
	    	if(user!=null && view!=null){
				view.addObject("user", user) ;
				view.addObject("schema",arg0.getScheme()) ;
				view.addObject("hostname",arg0.getServerName()) ;
				view.addObject("port",arg0.getServerPort()) ;
				
				view.addObject("orgi", user.getOrgi()) ;
			}
	    	HandlerMethod  handlerMethod = (HandlerMethod ) arg2 ;
			Menu menu = handlerMethod.getMethod().getAnnotation(Menu.class) ;
			if(menu!=null){
				view.addObject("subtype", menu.subtype()) ;
				view.addObject("maintype", menu.type()) ;
				view.addObject("typename", menu.name()) ;
			}
			
	    	view.addObject("webimport",UKDataContext.getWebIMPort()) ;
	    	view.addObject("sessionid", UKTools.getContextID(arg0.getSession().getId())) ;
			/**
			 * WebIM共享用户
			 */
			User imUser = (User) arg0.getSession().getAttribute(UKDataContext.IM_USER_SESSION_NAME) ;
			if(imUser == null && view!=null){
				imUser = new User();
				imUser.setUsername(UKDataContext.GUEST_USER) ;
				imUser.setId(UKTools.getContextID(arg0.getSession(true).getId())) ;
				imUser.setSessionid(imUser.getId()) ;
				view.addObject("imuser", imUser) ;
			}
			if(!StringUtils.isBlank(arg0.getParameter("msg"))){
				view.addObject("msg", arg0.getParameter("msg")) ;
			}
			{//记录用户行为日志
				long start = (long) arg0.getAttribute("starttime") ;
				UserHistory userHistory = new UserHistory() ;
				userHistory.setTimes((int) ((System.currentTimeMillis() - start)));
				String url = arg0.getRequestURL().toString() ;
				if(url.length() >255){
					userHistory.setUrl(url.substring( 0 , 255));
				}else{
					userHistory.setUrl(url);
				}
				userHistory.setParam(UKTools.getParameter(arg0));
				if(menu!=null){
					userHistory.setMaintype(menu.type());
					userHistory.setSubtype(menu.subtype());
					userHistory.setName(menu.name());
					userHistory.setAdmin(menu.admin());
					userHistory.setAccess(menu.access());
				}
				if(user!=null){
					userHistory.setCreater(user.getId());
					userHistory.setUsername(user.getUsername());
					userHistory.setOrgi(user.getOrgi());
				}
				userHistory.setSessionid(arg0.getSession().getId());
				userHistory.setHostname(arg0.getRemoteHost());
				userHistory.setIp(arg0.getRemoteAddr());
				IP ipdata = IPTools.getInstance().findGeography(arg0.getRemoteAddr());
				userHistory.setCountry(ipdata.getCountry());
				userHistory.setProvince(ipdata.getProvince());
				userHistory.setCity(ipdata.getCity());
			    userHistory.setIsp(ipdata.getIsp());
			    
			    BrowserClient client = UKTools.parseClient(arg0) ;
			    userHistory.setOstype(client.getOs());
			    userHistory.setBrowser(client.getBrowser());
			    userHistory.setMobile(CheckMobile.check(arg0.getHeader("User-Agent")) ? "1" : "0");
				
				UKTools.published(userHistory);
			}
    	}
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

}