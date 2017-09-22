package com.ukefu.ask.web.handler.admin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ukefu.ask.service.repository.TopicCommentRepository;
import com.ukefu.ask.service.repository.TopicRepository;
import com.ukefu.ask.service.repository.UserRepository;
import com.ukefu.ask.web.handler.Handler;
import com.ukefu.ask.web.model.Topic;
import com.ukefu.ask.web.model.User;
import com.ukefu.util.Menu;

@Controller
public class AdminController extends Handler{
	
	@Autowired
	private UserRepository userRes;
	
	@Autowired
    private TopicRepository topicRes;
	
	@Autowired
    private TopicCommentRepository topicCommentRes;
	
    @RequestMapping("/admin")
    @Menu(type = "admin" , subtype = "content" , access = false , admin = true)
    public ModelAndView index(HttpServletRequest request) {
    	ModelAndView view = request(super.createRequestPageTempletResponse("/admin/index"));
        return view;
    }
    
    @RequestMapping("/admin/content")
    @Menu(type = "admin" , subtype = "content" , access = false , admin = true)
    public ModelAndView content(ModelMap map) {
    	ModelAndView view = request(super.createAdminTempletResponse("/admin/content"));
    	NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder() ;
    	searchQuery.withSort(new FieldSortBuilder("updatetime").unmappedType("date").order(SortOrder.DESC)) ;
    	Page<Topic> newTopicList = processTopicCreater(topicRes.findByCon(searchQuery, null, 0, 10)) ; 
    	view.addObject("newTopicList", newTopicList) ;
    	
    	NativeSearchQueryBuilder countSearchQuery = new NativeSearchQueryBuilder() ;
    	countSearchQuery.withQuery(QueryBuilders.rangeQuery("createtime").from("now-30d").to("now+1d")).addAggregation(AggregationBuilders.dateHistogram("createtime").field("createtime").interval(DateHistogram.Interval.DAY)) ;
    	Page<Topic> page = topicRes.countByCon(countSearchQuery, "createtime" , null, 0, 30) ;
    	
    	Calendar calendar = Calendar.getInstance();  
    	int day = calendar.get(Calendar.DATE) ;
    	int month = calendar.get(Calendar.MONTH)+1 ;
    	int year = calendar.get(Calendar.YEAR) ;
    	List<Topic> countTopicList = new ArrayList<Topic>();
    	int todayTopics = 0 ;
    	for(int i=0 ; i<30 ; i++){
    		String date = null ;
    		day = calendar.get(Calendar.DATE) ;
    		month = calendar.get(Calendar.MONTH) + 1;
    		date = year+"-"+format(month)+"-"+format(day) ;
    		boolean exist = false ;
    		Topic topic = new Topic() ;
    		for(Topic tp : page.getContent()){
    			if(tp.getKey().equals(date)){
    				exist = true ;topic = tp ; 
    				if(i ==0){
    					todayTopics = tp.getRowcount();
    				}
    				break ;
    			}
    		}
    		if(!exist){
    			topic.setKey(date);
    		}
    		countTopicList.add(0 , topic);
    		calendar.set(Calendar.DAY_OF_MONTH, day - 1);
    	}
    	view.addObject("todayTopics", todayTopics) ;
    	
    	view.addObject("countTopicList", countTopicList) ;
    	
    	view.addObject("countTopic", newTopicList.getTotalElements()) ;
    	
    	view.addObject("newUserList", userRes.findAll(new PageRequest(0, 20, Sort.Direction.DESC, "createtime"))) ;
    	
    	view.addObject("topicCommentList", topicCommentRes.findAll(new PageRequest(0, 1))) ;
        return view;
    }
    
    private String format(int num){
    	String str = null ;
    	if(num>9){
    		str = String.valueOf(num) ;
    	}else{
    		str = "0"+num ;
    	}
    	return str ;
    }
    
    /**
	 * 处理 创建人
	 * @param defaultTopicList
	 */
	protected Page<Topic> processTopicCreater(Page<Topic> defaultTopicList){
		List<String> users = new ArrayList<String>();
		for(Topic topic : defaultTopicList.getContent()){
			users.add(topic.getCreater()) ;
		}
		List<User> userList = userRes.findAll(users) ;
		for(Topic topic : defaultTopicList.getContent()){
			for(User user : userList){
				if(topic.getCreater().equals(user.getId())){
					topic.setUser(user); break ;
				}
			}
		}
		return defaultTopicList ;
	}
}