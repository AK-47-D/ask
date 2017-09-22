package com.ukefu.ask.util.query;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.ukefu.ask.web.model.Topic;
import com.ukefu.ask.web.model.TopicComment;


public class UKAggResultExtractor extends UKResultMapper{
	
	private String term ;
	
	public UKAggResultExtractor(String term){
		this.term = term ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Page<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
		Aggregations aggregations = response.getAggregations();
		List<T> results = new ArrayList<T>();
		long total = 0 ;
		if(aggregations.get(term) instanceof Terms){
			Terms agg = aggregations.get(term) ;
			if(agg!=null){
				total = agg.getSumOfOtherDocCounts() ;
				if(agg.getBuckets()!=null && agg.getBuckets().size()>0){
					for (Terms.Bucket entry : agg.getBuckets()) {
						if(clazz.equals(Topic.class)){
							Topic topic = new Topic();
							topic.setCreater(entry.getKey());
							topic.setRowcount((int) entry.getDocCount());
							results.add((T) topic) ;
						}else if(clazz.equals(TopicComment.class)){
							TopicComment topicComment = new TopicComment();
							topicComment.setCreater(entry.getKey());
							topicComment.setRowcount((int) entry.getDocCount());
							results.add((T) topicComment) ;
						}
					}
				}
			}
		}else if(aggregations.get(term) instanceof InternalDateHistogram){
			InternalDateHistogram agg = aggregations.get(term) ;
			total = response.getHits().getTotalHits() ;
			if(agg!=null){
				if(agg.getBuckets()!=null && agg.getBuckets().size()>0){
					for (DateHistogram.Bucket entry : agg.getBuckets()) {
						if(clazz.equals(Topic.class)){
							Topic topic = new Topic();
							topic.setKey(entry.getKey().substring(0 , 10));
							topic.setRowcount((int) entry.getDocCount());
							results.add((T) topic) ;
						}	
					}
				}
			}
		}
		return new PageImpl<T>(results, pageable, total);
	}
}
