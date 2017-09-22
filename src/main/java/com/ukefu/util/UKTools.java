package com.ukefu.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.lmax.disruptor.dsl.Disruptor;
import com.ukefu.core.UKDataContext;
import com.ukefu.util.event.MultiUpdateEvent;
import com.ukefu.util.event.UserDataEvent;
import com.ukefu.util.event.UserEvent;


@SuppressWarnings("deprecation")
public class UKTools {
	private static MD5 md5 = new MD5();
	
	public static SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
	
	public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd") ;
	
	public static SimpleDateFormat timeRangeDateFormat = new SimpleDateFormat("HH:mm");
	
	private static HttpClient httpclient = initHttpClientPool();
	/**
	 * 当前时间+已过随机生成的 长整形数字
	 * @return
	 */
	public static String genID(){
		return Base62.encode(getUUID()).toLowerCase() ;
	}
	
	public static String genIDByKey(String key){
		return Base62.encode(key).toLowerCase() ;
	}
	
	public static String getUUID(){
		return UUID.randomUUID().toString().replace("-", "") ;
	}
	
	public static String getContextID(String session){
		return session.replaceAll("-", "") ;
	}
	
	public static String md5(String str) {
		return md5.getMD5ofStr(md5.getMD5ofStr(str));
	}
	
	public static String md5(byte[] bytes) {
		return md5.getMD5ofByte(bytes);
	}
	
	public static HttpClient getHttpClient(){
		return httpclient ;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String postData(String url, List<NameValuePair> nvps) {
		HttpPost httpost = new HttpPost(url);
		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			ResponseHandler responseHandler = new BasicResponseHandler();
			return (String) httpclient.execute(httpost, responseHandler);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}
	
	
	private static HttpClient initHttpClientPool() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 40000);
		HttpConnectionParams.setSoTimeout(params, 40000);
		ConnManagerParams.setMaxTotalConnections(params, 5);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		return new DefaultHttpClient(cm, params);
	}

	public static void copyProperties(Object source, Object target,String... ignoreProperties)  
	        throws BeansException {  
	  
	    Assert.notNull(source, "Source must not be null");  
	    Assert.notNull(target, "Target must not be null");  
	  
	    Class<?> actualEditable = target.getClass();  
	    PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(actualEditable);  
	    List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;  
	  
	    for (PropertyDescriptor targetPd : targetPds) {  
	        Method writeMethod = targetPd.getWriteMethod();  
	        if (writeMethod != null && (ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {  
	            PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());  
	            if (sourcePd != null && !targetPd.getName().equalsIgnoreCase("id")) {  
	                Method readMethod = sourcePd.getReadMethod();  
	                if (readMethod != null &&  
	                        ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {  
	                    try {  
	                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {  
	                            readMethod.setAccessible(true);  
	                        }  
	                        Object value = readMethod.invoke(source);  
	                        if(value != null){  //只拷贝不为null的属性 by zhao  
	                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {  
	                                writeMethod.setAccessible(true);  
	                            }  
	                            writeMethod.invoke(target, value);  
	                        }  
	                    }  
	                    catch (Throwable ex) {  
	                        throw new FatalBeanException(  
	                                "Could not copy property '" + targetPd.getName() + "' from source to target", ex);  
	                    }  
	                }  
	            }  
	        }  
	    }  
	}  
	
	public static long ipToLong(String ipAddress) {
		long result = 0;
		String[] ipAddressInArray = ipAddress.split("\\.");
		if(ipAddressInArray!=null && ipAddressInArray.length == 4){
			for (int i = 3; i >= 0; i--) {
				long ip = Long.parseLong(ipAddressInArray[3 - i]);
	
				// left shifting 24,16,8,0 and bitwise OR
	
				// 1. 192 << 24
				// 1. 168 << 16
				// 1. 1 << 8
				// 1. 2 << 0
				result |= ip << (i * 8);
	
			}
		}
		return result;
	}

	public static String longToIp2(long ip) {

		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
				+ ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}
	/***
	 * ID编码 ， 发送对话的时候使用
	 * @param id
	 * @param nid
	 * @return
	 */
	public static String genNewID(String id , String nid){
		StringBuffer strb = new StringBuffer();
		if(id!=null && nid!=null){
			int length = Math.max(id.length(), nid.length()); 
			for(int i=0 ; i<length ; i++){
				if(nid.length() > i && id.length() > i){
					int cur = (id.charAt(i) + nid.charAt(i)) / 2 ;
					strb.append((char)cur) ;
				}else if(nid.length() > i){
					strb.append(nid.charAt(i)) ;
				}else{
					strb.append(id.charAt(i)) ;
				}
			}
		}
		return strb.toString() ;
	}
	
	public static void published(UserEvent event){
		@SuppressWarnings("unchecked")
		Disruptor<UserDataEvent> disruptor = (Disruptor<UserDataEvent>) UKDataContext.getContext().getBean("disruptor") ;
		long seq = disruptor.getRingBuffer().next();
		disruptor.getRingBuffer().get(seq).setEvent(event); ;
		disruptor.getRingBuffer().publish(seq);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void multiupdate(MultiUpdateEvent event){
		Disruptor<UserDataEvent> disruptor = (Disruptor<UserDataEvent>) UKDataContext.getContext().getBean("multiupdate") ;
		long seq = disruptor.getRingBuffer().next();
		disruptor.getRingBuffer().get(seq).setEvent(event); ;
		disruptor.getRingBuffer().publish(seq);
	}
	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getParameter(HttpServletRequest request){
		Enumeration<String> names = request.getParameterNames() ;
		StringBuffer strb = new StringBuffer();
		while(names.hasMoreElements()){
			String name = names.nextElement() ;
			if(name.indexOf("password") < 0){	//不记录 任何包含 password 的参数内容
				if(strb.length() > 0){
					strb.append(",") ;
				}
				strb.append(name).append("=").append(request.getParameter(name)) ;
			}
		}
		return strb.toString() ;
		
	}
	
	/**
	 * 获取一天的开始时间
	 * @return
	 */
	public static Date getStartTime(){
		Calendar todayStart = Calendar.getInstance();  
        todayStart.set(Calendar.HOUR_OF_DAY, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime();  
	}
	
	/**
	 * 获取一天的开始时间
	 * @return
	 */
	public static Date getWeekStartTime(){
		Calendar weekStart = Calendar.getInstance();  
		weekStart.set(weekStart.get(Calendar.YEAR), weekStart.get(Calendar.MONDAY), weekStart.get(Calendar.DAY_OF_MONTH), 0, 0, 0);  
		weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); 
        return weekStart.getTime();  
	}
	
	/**
	 * 获取一天的开始时间
	 * @return
	 */
	public static Date getLast30Day(){
		Calendar todayStart = Calendar.getInstance();  
		todayStart.set(Calendar.DAY_OF_MONTH, -30);
		todayStart.set(Calendar.HOUR_OF_DAY, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime();  
	}
	
	/**
	 * 获取一天的开始时间
	 * @return
	 */
	public static Date getLastDay(int days){
		Calendar todayStart = Calendar.getInstance();  
		todayStart.set(Calendar.DAY_OF_MONTH, -days);
		todayStart.set(Calendar.HOUR_OF_DAY, 0);  
        todayStart.set(Calendar.MINUTE, 0);  
        todayStart.set(Calendar.SECOND, 0);  
        todayStart.set(Calendar.MILLISECOND, 0);  
        return todayStart.getTime();  
	}
	
	/**
	 * 获取一天的结束时间
	 * @return
	 */
	public static Date getEndTime(){
		Calendar todayEnd = Calendar.getInstance();  
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);  
        todayEnd.set(Calendar.MINUTE, 59);  
        todayEnd.set(Calendar.SECOND, 59);  
        todayEnd.set(Calendar.MILLISECOND, 999);  
        return todayEnd.getTime();  
	}
	
	/**
	 * 获取一天的结束时间
	 * @return
	 */
	public static Date getLastTime(int secs){
		Calendar todayEnd = Calendar.getInstance();
        todayEnd.add(Calendar.SECOND, secs*-1);  
        return todayEnd.getTime();  
	}
	
	public static void noCacheResponse(HttpServletResponse response){
		response.setDateHeader("Expires",0);
        response.setHeader("Buffer","True");
        response.setHeader("Cache-Control","no-cache");
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Expires","0");
        response.setHeader("ETag",String.valueOf(System.currentTimeMillis()));
        response.setHeader("Pragma","no-cache");
        response.setHeader("Date",String.valueOf(new Date()));
        response.setHeader("Last-Modified",String.valueOf(new Date()));
	}
	
	public static BrowserClient parseClient(HttpServletRequest request){
		BrowserClient client = new BrowserClient() ;
		String  browserDetails  =   request.getHeader("User-Agent");
	    String  userAgent       =   browserDetails;
	    String  user   =   userAgent.toLowerCase();
		String os = "";
        String browser = "" , version = "";
        

        //=================OS=======================
         if (userAgent.toLowerCase().indexOf("windows") >= 0 )
         {
             os = "windows";
         } else if(userAgent.toLowerCase().indexOf("mac") >= 0)
         {
             os = "mac";
         } else if(userAgent.toLowerCase().indexOf("x11") >= 0)
         {
             os = "unix";
         } else if(userAgent.toLowerCase().indexOf("android") >= 0)
         {
             os = "android";
         } else if(userAgent.toLowerCase().indexOf("iphone") >= 0)
         {
             os = "iphone";
         }else{
             os = "UnKnown";
         }
         //===============Browser===========================
        if (user.contains("msie") || user.indexOf("rv:11") > -1)
        {
        	if(user.indexOf("rv:11") >= 0){
        		browser = "IE11" ;
        	}else{
	            String substring=userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
	            browser=substring.split(" ")[0].replace("MSIE", "IE")+substring.split(" ")[1];
        	}
        }else if (user.contains("trident"))
        {
            browser= "IE 11" ;
        }else if (user.contains("edge"))
        {
            browser= "Edge" ;
        }  else if (user.contains("safari") && user.contains("version"))
        {
            browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0];
            version = (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1] ;
        } else if ( user.contains("opr") || user.contains("opera"))
        {
            if(user.contains("opera"))
                browser=(userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]+"-"+(userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
            else if(user.contains("opr"))
                browser=((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-")).replace("OPR", "Opera");
        } else if (user.contains("chrome"))
        {
            browser = "Chrome";
        } else if ((user.indexOf("mozilla/7.0") > -1) || (user.indexOf("netscape6") != -1)  || (user.indexOf("mozilla/4.7") != -1) || (user.indexOf("mozilla/4.78") != -1) || (user.indexOf("mozilla/4.08") != -1) || (user.indexOf("mozilla/3") != -1) )
        {
            //browser=(userAgent.substring(userAgent.indexOf("MSIE")).split(" ")[0]).replace("/", "-");
            browser = "Netscape-?";

        }else if ((user.indexOf("mozilla") > -1))
        {
            //browser=(userAgent.substring(userAgent.indexOf("MSIE")).split(" ")[0]).replace("/", "-");
        	if(browserDetails.indexOf(" ") > 0){
        		browser = browserDetails.substring(0 , browserDetails.indexOf(" "));
        	}else{
        		browser = "Mozilla" ;
        	}

        } else if (user.contains("firefox"))
        {
            browser=(userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
        } else if(user.contains("rv"))
        {
            browser="ie";
        } else
        {
            browser = "UnKnown";
        }
        client.setUseragent(browserDetails);
        client.setOs(os);
        client.setBrowser(browser);
        client.setVersion(version);
        
        return client ;
	}
	/**
	 * 活动JPA统计结果
	 * @param values
	 * @return
	 */
	public static WebIMReport getWebIMReport(List<Object> values){
		WebIMReport report = new WebIMReport();
		if(values!=null && values.size()>0){
			Object[] value = (Object[]) values.get(0) ;
			if(value.length>=2){
				report.setIpnums((long) value[0]);
				report.setPvnums((long) value[1]);
			}
		}
		return report ;
	}
	
	/**
	 * 活动JPA统计结果
	 * @param values
	 * @return
	 */
	public static WebIMReport getWebIMInviteStatus(List<Object> values){
		WebIMReport report = new WebIMReport();
		if(values!=null && values.size()>0){
			
			for(int i= 0 ; i<values.size() ; i++){
				Object[] value = (Object[]) values.get(i) ;
				if(value.length>=2){
					String invitestatus = (String) value[0] ;
					if(UKDataContext.OnlineUserInviteStatus.DEFAULT.toString().equals(invitestatus) || invitestatus == null){
						report.setUsers((long) value[1]);
					}else if(UKDataContext.OnlineUserInviteStatus.INVITE.toString().equals(invitestatus)){
						report.setInviteusers((long) value[1]);
					}else if(UKDataContext.OnlineUserInviteStatus.REFUSE.toString().equals(invitestatus)){
						report.setRefuseusers((long) value[1]);
					}
				}
			}
		}
		return report ;
	}
	
	/**
	 * 活动JPA统计结果
	 * @param values
	 * @return
	 */
	public static List<WebIMReport> getWebIMInviteAgg(List<Object> values){
		List<WebIMReport> webIMReportList = new ArrayList<WebIMReport>() ;
		if(values!=null && values.size()>0){
			for(int i= 0 ; i<values.size() ; i++){
				Object[] value = (Object[]) values.get(i) ;
				WebIMReport report = new WebIMReport();
				if(value.length==3){
					report.setData((String) value[0]);
					report.setIpnums((long) value[1]);
					report.setPvnums((long) value[2]);
				}
				webIMReportList.add(report) ;
			}
		}
		return webIMReportList ;
	}
	
	/**
	 * 活动JPA统计结果
	 * @param values
	 * @return
	 */
	public static List<WebIMReport> getWebIMDataAgg(List<Object> values){
		List<WebIMReport> webIMReportList = new ArrayList<WebIMReport>() ;
		if(values!=null && values.size()>0){
			for(int i= 0 ; i<values.size() ; i++){
				Object[] value = (Object[]) values.get(i) ;
				WebIMReport report = new WebIMReport();
				if(value.length==2){
					if(value[0] == null || value[0].toString().equalsIgnoreCase("null")){
						report.setData("未知");
					}else{
						report.setData((String) value[0]);
					}
					report.setUsers((long) value[1]);
				}
				webIMReportList.add(report) ;
			}
		}
		return webIMReportList ;
	}
	
	/**
	 * 活动JPA统计结果
	 * @param values
	 * @return
	 */
	public static WebIMReport getWebIMInviteResult(List<Object> values){
		WebIMReport report = new WebIMReport();
		if(values!=null && values.size()>0){
			
			for(int i= 0 ; i<values.size() ; i++){
				Object[] value = (Object[]) values.get(i) ;
				if(value.length>=2){
					String invitestatus = (String) value[0] ;
					if(UKDataContext.OnlineUserInviteStatus.DEFAULT.toString().equals(invitestatus) || invitestatus == null){
						report.setUsers((long) value[1]);
					}else if(UKDataContext.OnlineUserInviteStatus.ACCEPT.toString().equals(invitestatus)){
						report.setInviteusers((long) value[1]);
					}else if(UKDataContext.OnlineUserInviteStatus.REFUSE.toString().equals(invitestatus)){
						report.setRefuseusers((long) value[1]);
					}
				}
			}
		}
		return report ;
	}
	
	/**
	 * 活动JPA统计结果
	 * @param values
	 * @return
	 */
	public static WeiXinReport getWeiXinReportResult(List<Object> values){
		WeiXinReport report = new WeiXinReport();
		if(values!=null && values.size()>0){
			for(int i= 0 ; i<values.size() ; i++){
				Object[] value = (Object[]) values.get(i) ;
				if(value.length>=2){
					String event = (String) value[0] ;
					if(UKDataContext.WeiXinEventTypeEnum.SUB.toString().equals(event)){
						report.setSubs((long) value[1]);
					}else if(UKDataContext.WeiXinEventTypeEnum.UNSUB.toString().equals(event)){
						report.setUnsubs((long) value[1]);
					}
				}
			}
		}
		return report ;
	}
	
	public static Map<String, Object> transBean2Map(Object obj) {  
		  
        if(obj == null){  
            return null;  
        }          
        Map<String, Object> map = new HashMap<String, Object>();  
        try {  
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                // 过滤class属性  
                if (!key.equals("class")) {  
                    // 得到property对应的getter方法 
                	
                    Method readMethod = property.getReadMethod(); 
                    
                    if (readMethod != null) {  
                    	Object value = readMethod.invoke(obj);  
                    	if(value instanceof Date){
                    		value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value) ;
                    	}
	                    map.put(key, value);
	                }  
                }  
            }  
        } catch (Exception e) {  
            System.out.println("transBean2Map Error " + e);  
        }  
  
        return map;  
  
    }
	
	public static void populate(Object bean , Map<Object , Object> properties) throws IllegalAccessException, InvocationTargetException{
		ConvertUtils.register(new Converter()    
		{    
			@SuppressWarnings("rawtypes")    
			@Override    
			public Object convert(Class arg0, Object arg1)    
			{    
				if(arg1 == null)    
				{    
					return null;    
				}    
				if(!(arg1 instanceof String)){    
					throw new ConversionException("只支持字符串转换 !");    
				}    
				String str = (String)arg1;    
				if(str.trim().equals(""))    
				{    
					return null;    
				}    

				SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    

				try{    
					return sd.parse(str);    
				} catch(Exception e)    
				{    
					throw new RuntimeException(e);    
				}    

			}    

		}, java.util.Date.class);  
		if (properties == null || bean == null) {    
			return;    
		}    
		try {    
			BeanUtilsBean.getInstance().populate(bean, properties);
		} catch (Exception e) {
			e.printStackTrace();
		}    
	}
	
	/**
     * 
     * @param str
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public static String encryption(String str) throws NoSuchAlgorithmException{
    	BasicTextEncryptor  textEncryptor = new BasicTextEncryptor ();
    	textEncryptor.setPassword(UKDataContext.getSystemSecrityPassword());
    	return textEncryptor.encrypt(str);
    }
    
    /**
     * 
     * @param str
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public static String decryption(String str) throws NoSuchAlgorithmException{
    	BasicTextEncryptor  textEncryptor = new BasicTextEncryptor ();
    	textEncryptor.setPassword(UKDataContext.getSystemSecrityPassword());
    	return textEncryptor.decrypt(str);
    }
    
    public static String getTopic(String snsid ,String msgtype , String eventype , String eventkey , String msg){
    	StringBuffer strb = new StringBuffer() ;
		strb.append(snsid) ;
		strb.append(".").append(msgtype) ;
		if(msgtype.equals("text")){
			strb.append(".").append(msg) ;
		}else if(msgtype.equals("event")){
			strb.append(".").append(eventype.toLowerCase()) ;
			if(!StringUtils.isBlank(eventkey)){
				strb.append(".").append(eventkey) ;
			}
		}else{
			strb.append(".").append(msgtype) ;
		}
		return strb.toString() ;
    }
    
    public static String getTopic(String snsid ,String msgtype , String eventype){
    	StringBuffer strb = new StringBuffer() ;
		strb.append(snsid) ;
		strb.append(".").append(msgtype) ;
		if(msgtype.equals("text")){
			strb.append(".").append(msgtype) ;
		}else if(msgtype.equals("event")){
			strb.append(".").append(eventype.toLowerCase()) ;
		}else{
			strb.append(".").append(msgtype) ;
		}
		return strb.toString() ;
    }
	/**
	 * 处理 对话消息中的图片
	 * @param message
	 * @return
	 */
    public static String filterChatMessage(String message){
    	Document document = Jsoup.parse(message) ;
    	Elements pngs = document.select("img[src]");
    	for (Element element : pngs) {
    		String imgUrl = element.attr("src");
    		if(imgUrl.indexOf("/res/image") >= 0){
    			element.attr("class", "ukefu-media-image") ;
    		}
    	}
    	return document.html() ;
    }
    
    /**
     * 检查当前时间是否是在 时间范围内 ，时间范围的格式为 ： 08:30~11:30,13:30~17:30
     * @param timeRanges
     * @return
     */
    public static boolean isInWorkingHours(String timeRanges){
    	boolean workintTime = true ;
    	String timeStr = timeRangeDateFormat.format(new Date()) ;
    	if(!StringUtils.isBlank(timeRanges)){		//设置了 工作时间段
    		workintTime = false ;					//将 检查结果设置为 False ， 如果当前时间是在 时间范围内，则 置为 True
    		String[] timeRange = timeRanges.split(",") ;
    		for(String tr : timeRange){
    			String[] timeGroup = tr.split("~") ;
    			if(timeGroup.length == 2){
    				if(timeGroup[0].compareTo(timeGroup[1]) >= 0){
    					if(timeStr.compareTo(timeGroup[0]) >= 0 || timeStr.compareTo(timeGroup[1]) <= 0){
	    					workintTime = true ;
	    				}
    				}else{
	    				if(timeStr.compareTo(timeGroup[0]) >= 0 && timeStr.compareTo(timeGroup[1]) <= 0){
	    					workintTime = true ;
	    				}
    				}
    			}
    		}
    	}
    	return workintTime ;
    }
}
