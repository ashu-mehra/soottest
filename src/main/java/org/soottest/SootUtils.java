package org.soottest;

import soot.SootClass;
import soot.SootField;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class SootUtils {
    public static Set<SootField> getStaticFields(SootClass sclass) {
        Iterator<SootField> iterator = sclass.getFields().iterator();
        Set<SootField> fields = new LinkedHashSet<>();
        while (iterator.hasNext()) {
            SootField field = iterator.next();
            if (field.isStatic()) {
                fields.add(field);
            }
        }
        return fields;
    }
}
