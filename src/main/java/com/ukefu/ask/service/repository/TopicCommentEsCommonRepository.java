package com.ukefu.ask.service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.ukefu.ask.web.model.TopicComment;


public interface TopicCommentEsCommonRepository {
	public Page<TopicComment> findByDataid(String id , int p , int ps) ;
	
	public List<TopicComment> findByOptimal(String dataid) ;
	
	public Page<TopicComment> findByCon(NativeSearchQueryBuilder searchQueryBuilder, String q, int p,int ps);
	
	public Page<TopicComment> findByCon(NativeSearchQueryBuilder searchQueryBuilder, String field , String aggname, String q, int p,int ps);
	
	public Page<TopicComment> countByCon(NativeSearchQueryBuilder searchQuery,String q, int p, int ps);
}
