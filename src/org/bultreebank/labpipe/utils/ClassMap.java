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

import java.util.HashMap;
import java.util.List;

/**
 * <code>ClassMap</code> creates a <code>Map</code> from a list of objects using
 * their classes as keys and them as values. In case there are objects of the 
 * same class entries are overwritten.
 *
 * @author Aleksandar Savkov
 */
public class ClassMap extends HashMap<Class, Object> {

    /**
     * Creates a <code>ClassMap</code> object from a <code>list</code>.
     * 
     * @param   list    list of objects of different classes
     * 
     */
    public ClassMap (List<Object> list) {
        for (Object item : list)  {
            this.put(item.getClass(), item);
        }
    }

}
