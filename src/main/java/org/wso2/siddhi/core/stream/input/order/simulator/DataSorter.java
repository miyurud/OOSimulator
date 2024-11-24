package org.wso2.siddhi.core.stream.input.order.simulator;

import org.wso2.siddhi.core.stream.input.order.simulator.threads.DataLoaderThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class DataSorter {
    ArrayList<Object[]> sortedItems = new ArrayList<>();
    LinkedBlockingQueue<Object> eventBuffer;

    public DataSorter(DataLoaderThread thrd) {
        eventBuffer = thrd.getEventBuffer();
    }

    public void init() {
        Iterator<Object> iterator = eventBuffer.iterator();

        while (iterator.hasNext()) {
            sortedItems.add((Object[]) iterator.next());
        }
    }

    public ArrayList<Object[]> sort(){
        Comparator comparator = new SortByTimestamp();
        Collections.sort(sortedItems, comparator);
        return sortedItems;
    }
}

class SortByTimestamp implements Comparator {
    public int compare(Object obj1, Object obj2) {
        // Make sure that the objects are Car objects
        Object[] a = (Object[]) obj1;
        Object[] b = (Object[]) obj2;

        // Compare the year of both objects
        if (((Long)a[0]) < ((Long)b[0])) return -1;
        if (((Long)a[0]) > ((Long)b[0]))  return 1;
        return 0;
    }
}