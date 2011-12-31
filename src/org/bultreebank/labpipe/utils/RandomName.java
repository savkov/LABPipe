/*
 * LABPipe - Natural Language Processing Pipeline for Bulgarian
 * Copyright (C) 2011  Aleksandar Savkov
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
 *
 * @author Aleksandar Savkov
 */
public class RandomName {

    private static String makeRandomString() {
        
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder random = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            random.append(alphabet.charAt(genRandomNumber(0,61)));
        }
        
        return random.toString();
        
    }
    
    private static int genRandomNumber(int low, int high) {
        int random = (int)(Math.random() * 100);
        if (random > low && random < high) 
            return random;
        else
            return genRandomNumber(low, high);
    }
    
    public static String makeRandomName() {
        
        return makeRandomString() + ".tmp";
    }
    
    public static String makeRandomName(String prefix, String suffix) {
        
        return prefix + makeRandomString() + suffix;
    }

}
