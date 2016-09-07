package org.wso2.siddhi.core.stream.input.order.simulator.threads;

/**
 * Created by miyurud on 9/24/15.
 */
public interface DataPersistor {
    public void persistEvent(Object[] data);
    public void close();
}
