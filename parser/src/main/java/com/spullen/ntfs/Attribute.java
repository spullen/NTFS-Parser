package com.spullen.ntfs;

import java.util.HashMap;
import java.util.Set;

public class Attribute {

    int offsetToAttributeFromEntryStart = 0;

    // header information
    int attributeTypeIdentifier = 0;
    int lenOfAttr = 0;
    int nonResisdentFlag = 0;
    int lenOfName = 0;
    int offsetToName = 0;
    int flags = 0;
    int attrIdentifier = 0;

    HashMap<String, String> values = new HashMap<>();

    /**
     * Inserts a new attribute value into the attribute HashMap
     *
     * @param key
     * @param value
     */
    public void addValue(String key, String value) {
        values.put(key, value);
    }

    /**
     * Prints out the header information for this attribute
     */
    public void printHeaderInformation() {
        System.out.println("Attribute Type Identifier\t\t" + attributeTypeIdentifier);
        System.out.println("Length of attribute\t\t" + lenOfAttr);
        System.out.println("Non-resident flag\t\t" + nonResisdentFlag);
        System.out.println("Length of name\t\t" + lenOfName);
        System.out.println("Offset to name\t\t" + offsetToName);
        System.out.println("Flags\t\t\t" + flags);
        System.out.println("Attribute Identifier\t\t" + attrIdentifier);
    }

    /**
     * Prints out each key and value for the attribute
     */
    public void printAttributeValues() {
        Set<String> keys = values.keySet();
        for (String key : keys) {
            System.out.println(key + "\t\t" + values.get(key));
        }
    }
}
