/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.core.stream.input.order.simulator.threads;

import com.google.common.base.Splitter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.time.ZoneId;

/**
 * This data loader thread loads the DEBS Grand Challenge data set hosted in
 * http://www.debs2015.org/call-grand-challenge.html
 */
public class EDGARDataLoaderThread implements DataLoaderThread {
    private String fileName;
    private static Splitter splitter = Splitter.on(",");
    private LinkedBlockingQueue<Object> eventBufferList;
    private BufferedReader br;
    private int count;
    private long eventLimit;// This many events will be read from the stream data set

    /**
     * The data loader just uses a particular file to load data. Since an event
     * count is not specified in this constructor
     * the full amount of events will be read from the file line by line.
     * 
     * @param fileName - input data file
     */
    public EDGARDataLoaderThread(String fileName) {
        this(fileName, -1l);
    }

    /**
     * The data loader reads from a file upto a specified number of events.
     * 
     * @param fileName   - the input data file
     * @param eventCount - number of events to read
     */
    public EDGARDataLoaderThread(String fileName, long eventCount) {
        this.fileName = fileName;
        eventBufferList = new LinkedBlockingQueue<Object>();
        this.eventLimit = eventCount;
    }

    /**
     * Note that the first field listed in the events output from the data loader
     * must correspond to the timestamp used
     * for ordering the events. This is an assumption which must be followed in all
     * use cases. The timestamp must be a
     * long value.
     */
    public void run() {
        try {
            br = new BufferedReader(new FileReader(fileName), 10 * 1024 * 1024);
            String line = br.readLine();
            if (line.startsWith("time")){
                line = br.readLine();
            }

            while (line != null) {
                // We make an assumption here that we do not get empty strings due to missing
                // values that may present in the input data set.
                Iterator<String> dataStrIterator = splitter.split(line).iterator();
                String timestamp = dataStrIterator.next();
                String url = dataStrIterator.next();

                Object[] eventData = null;

                try {
                    eventData = new Object[] { convertLocalDateTime(timestamp), // The data set is ordered based on the drop-off
                                                                   // timestamp.
                            url }; // We need to attach the time when we are injecting an event to the query
                                   // network. For that we have to set a separate field which will be populated
                                   // when we are injecting an event to the input stream.
                } catch (NumberFormatException e) {
                    // e.printStackTrace();
                    // If we find a discrepancy in converting data, then we have to discard that
                    // particular event.
                    line = br.readLine();
                    continue;
                }

                // We keep on accumulating data on to the event queue.
                // This will get blocked if the space required is not available.
                eventBufferList.put(eventData);
                line = br.readLine();
                count++;

                if (count >= eventLimit) {
                    break;
                }
            }
            System.out.println("Total amount of events read : " + count);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Now exiting from data loader thread.");
    }

    public LinkedBlockingQueue<Object> getEventBuffer() {
        return eventBufferList;
    }

    protected long convertLocalDateTime(String data) {
        if (data.startsWith("\"")) {
            data = data.substring(1, data.length());
        }
        if (data.endsWith("\"")) {
            data = data.substring(0, data.length() - 1);
        }
//        LocalDateTime localDateTime = LocalDateTime.parse(data);
//        long actualTimestamp = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        try {
            java.util.Date temp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
                    .parse(data.replace("T", " "));
            return temp.getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    protected long convert(String data) {
        if (data.startsWith("\"")) {
            data = data.substring(1, data.length());
        }
        if (data.endsWith("\"")) {
            data = data.substring(0, data.length() - 1);
        }
        String dateTime = (String) data;
        try {
            int y = Integer.parseInt(dateTime.substring(0, 4));
            int m = Integer.parseInt(dateTime.substring(5, 7));
            --m;
            int d = Integer.parseInt(dateTime.substring(8, 10));
            int h = Integer.parseInt(dateTime.substring(11, 13));
            int mm = Integer.parseInt(dateTime.substring(14, 16));
            int s = Integer.parseInt(dateTime.substring(17));

            CachedCalendar.set(y, m, d, h, mm, s);

            if (CachedCalendar.get(Calendar.YEAR) != y) {
                return 0;
            }
            if (CachedCalendar.get(Calendar.MONTH) != m) {
                return 0;
            }
            if (CachedCalendar.get(Calendar.DATE) != d) {
                return 0;
            }

            if (h < 0 || m > 23) {
                return 0;
            }
            if (mm < 0 || mm > 59) {
                return 0;
            }
            if (s < 0 || s > 59) {
                return 0;
            }

            return CachedCalendar.getTime().getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static final Calendar CachedCalendar = new GregorianCalendar();
    static {
        CachedCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        CachedCalendar.clear();
    }
}
