package com.ukefu.ask.service.repository;

import com.ukefu.ask.web.model.TopicView;



public interface TopicViewEsCommonRepository {
	public long findByIpcode(String id , String ip , String optype)  ;
	
	public TopicView findByCreaterAndDataid(String creater , String dataid) ;
}
