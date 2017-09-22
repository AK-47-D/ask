package com.ukefu.ask.util.disruptor;

import com.lmax.disruptor.EventHandler;
import com.ukefu.ask.service.repository.UserEventRepository;
import com.ukefu.ask.web.model.UserHistory;
import com.ukefu.core.UKDataContext;
import com.ukefu.util.event.UserDataEvent;

public class UserEventHandler implements EventHandler<UserDataEvent>{

	@Override
	public void onEvent(UserDataEvent arg0, long arg1, boolean arg2)
			throws Exception {
		UserEventRepository userEventRes = UKDataContext.getContext().getBean(UserEventRepository.class) ;
		userEventRes.save((UserHistory)arg0.getEvent()) ;
	}

}
