/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011 Institute for Information and Communication Technologies
 * 
 * The development of this program was funded by the EuroMatrixPlus Project as 
 * part of the Seventh Framework Program of the European Commission.
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

import org.bultreebank.labpipe.utils.CommandLineUtils;
import org.bultreebank.labpipe.utils.XmlUtils;

/**
 *
 * @author Aleksandar Savkov
 */
public class Test {

    public static void main(String[] args) throws Exception {

        String[] tokenizationS = new String[]{"-p", "stok", "-itype", "txt", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.txt", "-out", "sample/out/test.stok.out.line"};
        String[] tokenizationR = new String[]{"-p", "rtok", "-itype", "txt", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.txt", "-out", "sample/out/test.rtok.out.line"};
        String[] tokenizationC = new String[]{"-p", "ctok", "-itype", "txt", "-otype", "ctok", "-o", "conf/conf.xml", "-in", "sample/test.txt", "-out", "sample/out/test.ctok.out.ctok"};
        String[] svmt = new String[]{"-p", "stag", "-itype", "line", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.tok.line", "-out", "sample/out/test.stag.out.line"};
        String[] gaze = new String[]{"-p", "gtag", "-itype", "gaze", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.tag.gaze", "-out", "sample/out/test.gtag.out.line"};
        String[] ctag = new String[]{"-p", "ctag", "-itype", "line", "-otype", "ctag", "-o", "conf/conf.xml", "-in", "sample/test.tok.line", "-out", "sample/out/test.ctag.out.ctag"};
        String[] clem = new String[]{"-p", "clem", "-itype", "ctag", "-otype", "ctag", "-o", "conf/conf.xml", "-in", "sample/test.tag.ctag", "-out", "sample/out/test.lemma.out.ctag"};
        String[] correct = new String[]{"-p", "ccor", "-itype", "line", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.tag.line", "-out", "sample/out/test.ctag.out.line"};
        String[] parse = new String[]{"-p", "mpar", "-itype", "line", "-otype", "conll", "-o", "conf/conf.xml", "-in", "sample/test.lemma.line", "-out", "sample/out/test.conll.out.line"};

        String[] pipe = new String[]{"-p", "stok;stag;ctag;gtag;clem;mpar", "-itype", "txt", "-otype", "wl", "-o", "conf/conf.xml", "-in", "sample/test.txt", "-out", "sample/out/test.pipe.out"};

        String[] convertL2CO = new String[]{"-c", "true", "-itype", "line", "-otype", "conll", "-o", "conf/conf.xml", "-in", "sample/test.lemma.line", "-out", "sample/out/test.line.2.conll"};
        String[] convertL2CTO = new String[]{"-c", "true", "-itype", "line", "-otype", "ctok", "-o", "conf/conf.xml", "-in", "sample/test.tok.line", "-out", "sample/out/test.line.2.ctok"};
        String[] convertL2CTA = new String[]{"-c", "true", "-itype", "line", "-otype", "ctag", "-o", "conf/conf.xml", "-in", "sample/test.lemma.line", "-out", "sample/out/test.line.2.ctag"};
        String[] convertL2G = new String[]{"-c", "true", "-itype", "line", "-otype", "gaze", "-o", "conf/conf.xml", "-in", "sample/test.lemma.line", "-out", "sample/out/test.line.2.gaze"};
        String[] convertL2WL = new String[]{"-c", "true", "-itype", "line", "-otype", "wl", "-o", "conf/conf.xml", "-in", "sample/test.lemma.line", "-out", "sample/out/test.line.2.wl"};

        String[] convertCTO2L = new String[]{"-c", "true", "-itype", "ctok", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.tok.ctok", "-out", "sample/out/test.ctok.2.line"};
        String[] convertCTO2WL = new String[]{"-c", "true", "-itype", "ctok", "-otype", "wl", "-o", "conf/conf.xml", "-in", "sample/test.tok.ctok", "-out", "sample/out/test.ctok.2.wl"};
        String[] convertCTA2L = new String[]{"-c", "true", "-itype", "ctag", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.lemma.ctag", "-out", "sample/out/test.ctag.2.line"};
        String[] convertCTA2G = new String[]{"-c", "true", "-itype", "ctag", "-otype", "gaze", "-o", "conf/conf.xml", "-in", "sample/test.lemma.ctag", "-out", "sample/out/test.ctag.2.gaze"};
        String[] convertCTA2CO = new String[]{"-c", "true", "-itype", "ctag", "-otype", "conll", "-o", "conf/conf.xml", "-in", "sample/test.lemma.ctag", "-out", "sample/out/test.ctag.2.conll"};
        String[] convertCTA2WL = new String[]{"-c", "true", "-itype", "ctag", "-otype", "wl", "-o", "conf/conf.xml", "-in", "sample/test.lemma.ctag", "-out", "sample/out/test.ctag.2.wl"};

        String[] convertWL2L = new String[]{"-c", "true", "-itype", "wl", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.lemma.wl", "-out", "sample/out/test.wl.2.line"};
        String[] convertWL2G = new String[]{"-c", "true", "-itype", "wl", "-otype", "gaze", "-o", "conf/conf.xml", "-in", "sample/test.lemma.wl", "-out", "sample/out/test.wl.2.gaze"};
        String[] convertWL2CTO = new String[]{"-c", "true", "-itype", "wl", "-otype", "ctok", "-o", "conf/conf.xml", "-in", "sample/test.lemma.wl", "-out", "sample/out/test.wl.2.ctok"};
        String[] convertWL2CTA = new String[]{"-c", "true", "-itype", "wl", "-otype", "ctag", "-o", "conf/conf.xml", "-in", "sample/test.lemma.wl", "-out", "sample/out/test.wl.2.ctag"};
        String[] convertWL2CO = new String[]{"-c", "true", "-itype", "wl", "-otype", "conll", "-o", "conf/conf.xml", "-in", "sample/test.lemma.wl", "-out", "sample/out/test.wl.2.conll"};

        String[] convertCO2L = new String[]{"-c", "true", "-itype", "conll", "-otype", "line", "-o", "conf/conf.xml", "-in", "sample/test.parse.conll", "-out", "sample/out/test.conll.2.line"};
        String[] convertCO2G = new String[]{"-c", "true", "-itype", "conll", "-otype", "gaze", "-o", "conf/conf.xml", "-in", "sample/test.parse.conll", "-out", "sample/out/test.conll.2.gaze"};
        String[] convertCO2CTO = new String[]{"-c", "true", "-itype", "conll", "-otype", "ctok", "-o", "conf/conf.xml", "-in", "sample/test.parse.conll", "-out", "sample/out/test.conll.2.ctok"};
        String[] convertCO2CTA = new String[]{"-c", "true", "-itype", "conll", "-otype", "ctag", "-o", "conf/conf.xml", "-in", "sample/test.parse.conll", "-out", "sample/out/test.conll.2.ctag"};
        String[] convertCO2WL = new String[]{"-c", "true", "-itype", "conll", "-otype", "wl", "-o", "conf/conf.xml", "-in", "sample/test.parse.conll", "-out", "sample/out/test.conll.2.wl"};

        Main.main(pipe);
        Main.main(tokenizationS);
        Main.main(tokenizationR);
        Main.main(tokenizationC);
        Main.main(svmt);
//        Main.main(gaze);
        Main.main(ctag);
        Main.main(clem);
        Main.main(correct);
        Main.main(parse);
        Main.main(convertL2CO);
        Main.main(convertL2CTO);
        Main.main(convertL2CTA);
        Main.main(convertL2G);
        Main.main(convertL2WL);
        Main.main(convertCTO2L);
        Main.main(convertCTO2WL);
        Main.main(convertCTA2L);
        Main.main(convertCTA2G);
        Main.main(convertCTA2CO);
        Main.main(convertCTA2WL);
        Main.main(convertWL2L);
        Main.main(convertWL2G);
        Main.main(convertWL2CTO);
        Main.main(convertWL2CTA);
        Main.main(convertWL2CO);
        Main.main(convertCO2L);
        Main.main(convertCO2G);
        Main.main(convertCO2CTO);
        Main.main(convertCO2CTA);
        Main.main(convertCO2WL);

        String[] testFiles = CommandLineUtils.getFilesInDir("sample/out/");

        int count = 0;
        for (String file : testFiles) {
            if (file.endsWith("line") || file.endsWith("gaze") || file.endsWith("conll")) {
                count += CommandLineUtils.diffLines(file, file.replaceFirst("test\\.", "gold.").replaceFirst("out/", "gold/")) ? 0 : 1;
            } else {
                count += XmlUtils.xmlDiff(file, file.replaceFirst("test\\.", "gold.").replaceFirst("out/", "gold/")) ? 0 : 1;
            }
        }
        
        System.out.println(count + " of " + testFiles.length);


    }
}
