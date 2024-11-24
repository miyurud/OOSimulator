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

package org.wso2.siddhi.core.stream.input.order.simulator;

import org.wso2.siddhi.core.stream.input.order.simulator.threads.EDGARDataLoaderThread;
import org.wso2.siddhi.core.stream.input.order.simulator.threads.DEBS2013DataLoderThread;
import org.wso2.siddhi.core.stream.input.order.simulator.threads.DataLoaderThread;
import org.wso2.siddhi.core.stream.input.order.simulator.threads.DataPersistor;
import org.wso2.siddhi.core.stream.input.order.simulator.threads.GeneralDataPersistor;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        int numRecords = 100000;
        // String inputFile = "/home/miyurud/DEBS2015/dataset/20M-dataset.csv";
        String inputFile = "/media/miyurud/OS/Data/log20240101.csv";

        DataLoaderThread thrd = new EDGARDataLoaderThread(inputFile, numRecords);
        DataPersistor persistor = new GeneralDataPersistor("/home/miyurud/data/test2.csv");

        OOSimulator simulator = new OOSimulator(thrd, 5);
        simulator.init();
        simulator.setRunOO();

        for (int i = 0; i < numRecords; i++) {
            persistor.persistEvent(simulator.getNextEvent());
        }

        // Data Sorter
        // thrd.run();
        // DataSorter sorter = new DataSorter(thrd);
        // sorter.init();
        // ArrayList<Object[]> sortedItems = sorter.sort();
        //
        // for (int i = 0; i < numRecords; i++) {
        // persistor.persistEvent(sortedItems.get(i));
        // }

        persistor.close();
    }
}
