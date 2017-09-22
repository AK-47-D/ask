package com.ukefu.ask.service.repository;

import java.io.Serializable;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.ukefu.ask.web.model.Message;


public interface UKeFuEsCommonRepository {
	@SuppressWarnings("rawtypes")
	public Page<Serializable> findByCon(Class clazz,NativeSearchQueryBuilder searchQueryBuilder, String q, int p,int ps);
	
	@SuppressWarnings("rawtypes")
	public void deleteByQuery(QueryBuilder query , Class type) ;
	
	public Page<Message> findByUserid(String userid, int p, int ps);
}
