/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011 Institute for Information and Communication Technologies
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bultreebank.labpipe.main;

import java.util.ArrayList;
import org.bultreebank.labpipe.utils.CommandLineUtils;
import org.bultreebank.labpipe.utils.XmlUtils;

/**
 *
 * @author Aleksandar Savkov
 */
public class Diff {
    
    public static void main(String[] args) {
        
        if (args.length < 2 || args.length % 2 > 0) {
            return;
        }
        
        boolean odd = true;
        ArrayList<String> test = new ArrayList(args.length/2);
        ArrayList<String> gold = new ArrayList(args.length/2);
        
        for (String file : args) {
            
            if (odd) {
                test.add(file);
                odd = false;
            } else {
                gold.add(file);
                odd = true;
            }
            
        }
        
        int count = 0;
        for (int i = 0; i < test.size(); i++) {
            String testFile = test.get(i);
            String goldFile = gold.get(i);
            if (testFile.endsWith("line") || testFile.endsWith("gaze") || testFile.endsWith("conll")) {
                count += CommandLineUtils.diffLines(testFile, goldFile) ? 0 : 1;
            } else {
                count += XmlUtils.xmlDiff(testFile, goldFile) ? 0 : 1;
            }
        }
        
        System.out.println(count + " of " + test.size());
        
    }
    
}
