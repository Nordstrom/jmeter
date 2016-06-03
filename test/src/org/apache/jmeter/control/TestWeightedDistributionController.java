/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.control;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.junit.Test;

public class TestWeightedDistributionController extends JMeterTestCase {
    
    ChiSquareTest cst = new ChiSquareTest();
    
    @Test
    public void testDistribution() {
        int no_of_iters = 1030;
        
        String[] names = { "Zero - 90%", "One - 9%", "Two - 1%" };
        int[] wgts = { 90, 9, 1 };
        int[] exps = SequentialNumberGenerator.findExpectedResults(wgts, no_of_iters);
                       
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setRandomizer(new SequentialNumberGenerator(findWeightSum(wgts)));

        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[1]));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[2]));
        wdc.addTestElement(sub_2);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }

    @Test
    public void testDistributionTwoWdcs() {
        int no_of_iters = 1000;
        
        String[] names = { "A-Zero - 90%", "A-One - 9%", "A-Two - 1%", "B-Three 33%", "B-Four 66%" };
        int[] wgts = { 90, 9, 1, 33, 66 };
        
        int[] exps_A = SequentialNumberGenerator.findExpectedResults(Arrays.copyOfRange(wgts, 0, 3), no_of_iters);
        int[] exps_B = SequentialNumberGenerator.findExpectedResults(Arrays.copyOfRange(wgts, 3, 5), no_of_iters);
        
        int[] exps = ArrayUtils.addAll(exps_A, exps_B);
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        
        GenericController control = new GenericController();
        
        WeightedDistributionController wdc_A = new WeightedDistributionController();
        wdc_A.setRandomizer(new SequentialNumberGenerator(findWeightSum(Arrays.copyOfRange(wgts, 0, 3))));
        
        TestSampler sub_A_0 = new TestSampler(names[0]);
        sub_A_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        wdc_A.addTestElement(sub_A_0);
        
        TestSampler sub_A_1 = new TestSampler(names[1]);
        sub_A_1.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[1]));
        wdc_A.addTestElement(sub_A_1);
        
        TestSampler sub_A_2 = new TestSampler(names[2]);
        sub_A_2.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[2]));
        wdc_A.addTestElement(sub_A_2);
        
        WeightedDistributionController wdc_B = new WeightedDistributionController();
        wdc_B.setRandomizer(new SequentialNumberGenerator(findWeightSum(Arrays.copyOfRange(wgts, 3, 5))));
        
        TestSampler sub_B_3 = new TestSampler(names[3]);
        sub_B_3.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[3]));
        wdc_B.addTestElement(sub_B_3);
        
        TestSampler sub_B_4 = new TestSampler(names[4]);
        sub_B_4.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[4]));
        wdc_B.addTestElement(sub_B_4);
        
        control.addTestElement(wdc_A);
        control.addTestElement(wdc_B);
                
        control.setRunningVersion(true);
        wdc_A.setRunningVersion(true);
        wdc_B.setRunningVersion(true);
        sub_A_0.setRunningVersion(true);
        sub_A_1.setRunningVersion(true);
        sub_A_2.setRunningVersion(true);
        sub_B_3.setRunningVersion(true);
        sub_B_4.setRunningVersion(true);
        control.initialize();
        
        int[] results = executeTest(control, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithDisabled() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - 1000 - disabled", "One - 1% - enabled", "Two - 9% - enabled", "Three - 1% - disabled", "Four - 90% - enabled", "Five - 10% - disabled" };
        int[] wgts = { 1000, 1, 9, 1, 90, 10 };
        
        boolean[] enbs = { false, true, true, false, true, false };
        
        int exp_wgts[] = new int[wgts.length];
        for (int i = 0; i < exp_wgts.length; i++) {
            exp_wgts[i] = enbs[i] ? wgts[i] : 0;
        }
        
        int[] exps = SequentialNumberGenerator.findExpectedResults(exp_wgts, no_of_iters);
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setRandomizer(new SequentialNumberGenerator(findWeightSum(exp_wgts)));
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        sub_0.setEnabled(enbs[0]);
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[1]));
        sub_1.setEnabled(enbs[1]);
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[2]));
        sub_2.setEnabled(enbs[2]);
        wdc.addTestElement(sub_2);
        
        TestSampler sub_3 = new TestSampler(names[3]);
        sub_3.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[3]));
        sub_3.setEnabled(enbs[3]);
        wdc.addTestElement(sub_3);
        
        TestSampler sub_4 = new TestSampler(names[4]);
        sub_4.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[4]));
        sub_4.setEnabled(enbs[4]);
        wdc.addTestElement(sub_4);
        
        TestSampler sub_5 = new TestSampler(names[5]);
        sub_5.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[5]));
        sub_5.setEnabled(enbs[5]);
        wdc.addTestElement(sub_5);
                
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        sub_4.setRunningVersion(true);
        sub_5.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithZeroWeights() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - 0%", "One - 1%", "Two - 9%", "Three - 0%", "Four - 90%", "Five - 0%" };
        int[] wgts = { 0, 1, 9, 0, 90, 0 };
        int[] exps = SequentialNumberGenerator.findExpectedResults(wgts, no_of_iters);
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        wdc.setRandomizer(new SequentialNumberGenerator(findWeightSum(wgts)));
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[1]));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[2]));
        wdc.addTestElement(sub_2);
        
        TestSampler sub_3 = new TestSampler(names[3]);
        sub_3.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[3]));
        wdc.addTestElement(sub_3);
        
        TestSampler sub_4 = new TestSampler(names[4]);
        sub_4.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[4]));
        wdc.addTestElement(sub_4);
        
        TestSampler sub_5 = new TestSampler(names[5]);
        sub_5.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[5]));
        wdc.addTestElement(sub_5);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        sub_3.setRunningVersion(true);
        sub_4.setRunningVersion(true);
        sub_5.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithAllDisabled() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero - 90%", "One - 9%", "Two - 1%" };
        int[] wgts = { 90, 9, 1 };
        int[] exps = { 0, 0, 0 };
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        sub_0.setEnabled(false);
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[1]));
        sub_1.setEnabled(false);
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[2]));
        sub_2.setEnabled(false);
        wdc.addTestElement(sub_2);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    
    @Test
    public void testDistributionWithAllZeroWeights() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero", "One", "Two" };
        int[] wgts = { 0, 0, 0 };
        int[] exps = { 0, 0, 0 };
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        wdc.addTestElement(sub_0);
        
        TestSampler sub_1 = new TestSampler(names[1]);
        sub_1.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[1]));
        wdc.addTestElement(sub_1);
        
        TestSampler sub_2 = new TestSampler(names[2]);
        sub_2.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[2]));
        wdc.addTestElement(sub_2);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        sub_1.setRunningVersion(true);
        sub_2.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    
    @Test
    public void testDistributionWithOneSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero" };
        int[] wgts = { 1 };
        int[] exps = { no_of_iters };
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        wdc.addTestElement(sub_0);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithOneDisabledSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero" };
        int[] wgts = { 1 };
        int[] exps = { 0 };
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        sub_0.setEnabled(false);
        wdc.addTestElement(sub_0);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithOneZeroWeightSampler() {
        int no_of_iters = 1000;
        
        String[] names = { "Zero" };
        int[] wgts = { 0 };
        int[] exps = { 0 };
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        TestSampler sub_0 = new TestSampler(names[0]);
        sub_0.setProperty(new IntegerProperty(WeightedDistributionController.WEIGHT, wgts[0]));
        wdc.addTestElement(sub_0);
        
        wdc.setRunningVersion(true);
        sub_0.setRunningVersion(true);
        wdc.initialize();
        
        int[] results = executeTest(wdc, names, no_of_iters);
        
        assertArrayEquals(exps, results);
    }
    
    @Test
    public void testDistributionWithNoSamplers() {
        int no_of_iters = 1000;
        
        testLog.debug("Testing WeightedDistributionController percentage distribution");
        WeightedDistributionController wdc = new WeightedDistributionController();
        
        wdc.setRunningVersion(true);
        wdc.initialize();
        
        int actual_iters = 0;
        for (int i = 0; i < no_of_iters; i++) {
            @SuppressWarnings("unused")
            TestElement sampler;
            while((sampler = wdc.next()) != null) {
                actual_iters++;
            }
        }
        
        assertEquals(0, actual_iters);
    }
    
    static int findWeightSum(int[] wgts) {
        int wgtsum = 0;
        for (int wgt : wgts) {
            wgtsum += wgt;
        }
        return wgtsum;
    }
    
    static int[] executeTest(Controller control, String[] names, int iters) {
        Map<String, Integer> resultsMap = new HashMap<>(names.length);
        
        for (String name : names) {
            resultsMap.put(name, 0);
        }
        
        for (int i = 0; i < iters; i++) {
            TestElement sampler;
            while((sampler = control.next()) != null) {
                resultsMap.put(sampler.getName(), resultsMap.get(sampler.getName()) + 1);
            }
        }
        
        int[] results = new int[names.length];
        
        for(int i = 0; i < names.length; i++) {
            results[i] = resultsMap.get(names[i]);
        }
        
        return results;
    }
}

class SequentialNumberGenerator implements IntegerGenerator {
    
    int currVal;
    int maxVal;
    
    public SequentialNumberGenerator(int maxVal) {
        this.currVal = 0;
        this.maxVal = maxVal;
    }
    
    public int nextInt(int n) {
        int retVal = currVal++;
        if (currVal >= maxVal) {
            currVal = 0;
        }
        
        return retVal;
    }

    @Override
    public void setSeed(long seed) {
        // do nothing
    }
    
    static int[] findExpectedResults(int[] wgts, int iters) {
        int[] exps = new int[wgts.length];
        int wgtsum = TestWeightedDistributionController.findWeightSum(wgts);
        
        int completedIters = wgtsum > 0 ? iters/wgtsum : 0;
        int remainingIters = wgtsum > 0 ? iters % wgtsum : 0;
        
        for (int i = 0; i < exps.length; i++) {
            exps[i] = (completedIters * wgts[i] + (remainingIters > 0
                    ? remainingIters > wgts[i]
                            ? wgts[i]
                            : remainingIters
                    : 0));
            remainingIters -= wgts[i];                         
        }
        
        return exps;
    }
}