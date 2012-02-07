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
package org.bultreebank.labpipe.utils;

/**
 * <code>RandomName</code> generates random file names.
 *
 * @author Aleksandar Savkov
 */
public class RandomName {
    
    /*
     * Generates a random String of letters and numbers 10 signs long
     */
    private static String makeRandomString() {
        
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder random = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            random.append(alphabet.charAt(genRandomNumber(0,61)));
        }
        
        return random.toString();
        
    }
    
    /*
     * Generates a random number within the given limits
     */
    private static int genRandomNumber(int low, int high) {
        int random = (int)(Math.random() * 100);
        if (random > low && random < high) 
            return random;
        else
            return genRandomNumber(low, high);
    }
    
    /**
     * Generates a random name (10 signs) with the suffix <code>.tmp</code>
     * 
     * @return  String  random file name
     */
    public static String makeRandomName() {
        
        return makeRandomString() + ".tmp";
    }
    
    /**
     * Generates a random name (10 signs) with the provided prefix and suffix
     * 
     * @param   prefix  prefix to the random name
     * @param   suffix  suffix to the random name
     * 
     * @return  String  random name with prefix and suffix
     */
    public static String makeRandomName(String prefix, String suffix) {
        
        return prefix + makeRandomString() + suffix;
    }

}
