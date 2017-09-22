package com.ukefu.ask.util.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.ukefu.util.event.UserEvent;
import com.ukefu.util.event.UserDataEvent;

public class UserHistoryEventProducer {
	private final RingBuffer<UserDataEvent> ringBuffer;

    public UserHistoryEventProducer(RingBuffer<UserDataEvent> ringBuffer)
    {
        this.ringBuffer = ringBuffer;
    }

    public void onData(UserEvent userEvent)
    {
        long id = ringBuffer.next();  // Grab the next sequence
        try{
        	UserDataEvent event = ringBuffer.get(id);
        	event.setEvent(userEvent);
        }finally{
            ringBuffer.publish(id);
        }
    }
}
