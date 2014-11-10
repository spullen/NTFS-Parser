package com.spullen.ntfs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

/**
 * Structure holds all of the parsed information for a MFT Entry
 */
public class MFTEntry {

    // MFT entry data
    private String signature;
    private int offsetToFixupArray;
    private int entriesInFixupArray;
    private long lsn; // $LogFile Sequence Number
    private int sequenceValue;
    private int linkCount;
    private int offsetToFirstAttr;
    private int flags;
    private int usedSizeOfMFTEntry;
    private int allocatedSizeOfMFTEntry;
    private long referenceToBaseRecord;
    private int nextAttrID;
    // private byte[] attributesRawData;
    private byte[] entry;

    private List<Attribute> attributes = new LinkedList<Attribute>();

    /**
     * Constructor for MFTEntry
     *
     * @param rawEntryData
     */
    public MFTEntry(byte[] rawEntryData) {
        this.entry = rawEntryData;
        this.parseMFTEntry(rawEntryData);
    }

    /**
     * Parses each attribute creating an Attribute object for each
     * Only saves the resident ones
     * And only FILE_NAME, STANDARD_INFO, and DATA
     * DATA is not parsed, it is just presented
     */
    public void parseAttributes() {
        int offset = this.offsetToFirstAttr;

        int nextByte = 0;
        // while the next bytes are not 0xFF then get the next attributes
        while (nextByte != 0xFF) {
            Attribute attr = new Attribute();
            attr.offsetToAttributeFromEntryStart = offset;

            // read in the first 16 bytes as the attribute header
            byte[] header = new byte[16];
            for (int i = 0; i < header.length; i++) {
                header[i] = this.entry[offset + i];
            }
            // offset += 16;

            // wrap the header in a ByteBuffer
            ByteBuffer headerBuf = ByteBuffer.wrap(header);
            headerBuf.order(ByteOrder.LITTLE_ENDIAN);

            // get the attribute type identifier (bytes 0 - 3)
            attr.attributeTypeIdentifier = headerBuf.getInt(0);

            // get the length of the attribute (bytes 4 - 7)
            attr.lenOfAttr = headerBuf.getInt(4);

            // get the non resident flag (byte 8)
            attr.nonResisdentFlag = (int) headerBuf.get(8);

            // get the length of name (byte 9)
            attr.lenOfName = (int) headerBuf.get(9);

            // offset to name (byte 10 - 11)
            attr.offsetToName = headerBuf.getShort(10);

            // flags (bytes 12 - 13)
            attr.flags = headerBuf.getShort(12);

            // attribute identifier (bytes 14 - 15)
            attr.attrIdentifier = headerBuf.getShort(14);

            // if the non resident flag is set to 0 then it is resident
            if (attr.nonResisdentFlag == 0) {
                // get the data structure of the resident attribute
                int offsetToDS = attr.offsetToAttributeFromEntryStart + 16;

                // read bytes 16 - 21 into a ByteBuffer
                byte[] residentBytes = new byte[6];
                for (int i = 0; i < residentBytes.length; i++) {
                    residentBytes[i] = this.entry[offsetToDS + i];
                }
                ByteBuffer resident = ByteBuffer.wrap(residentBytes);
                resident.order(ByteOrder.LITTLE_ENDIAN);

                // get the size of the content
                int sizeOfContent = resident.getInt(0);

                // get the offset to content from the starting offset of the attribute
                int offsetToContent = resident.getShort(4);

                // read in the attribute based on the previous two values and create a byte buffer
                int offsetToAttribute = attr.offsetToAttributeFromEntryStart + offsetToContent;
                byte[] attributeBytes = new byte[sizeOfContent];
                for (int i = 0; i < attributeBytes.length; i++) {
                    attributeBytes[i] = this.entry[offsetToAttribute + i];
                }
                ByteBuffer attribute = ByteBuffer.wrap(attributeBytes);
                attribute.order(ByteOrder.LITTLE_ENDIAN);

                // get the attribute
                if (attr.attributeTypeIdentifier == 16) { // STANDARD_INFO attribute
                    // parse the standard info attribute

                } else if (attr.attributeTypeIdentifier == 48) { // FILE_NAME attribute
                    // parse the file name attribute

                } else if (attr.attributeTypeIdentifier == 128) { // DATA attribute
                    // parse the data attribute (just print it out?)

                }
            }

            // add this attribute to the list of attributes for the MFT entry
            this.attributes.add(attr);

            // set the new offset to the length of the attribute + current
            offset += attr.lenOfAttr;

            // get the next byte
            nextByte = this.entry[offset + 1];
        }
    }

    /**
     * Prints all of the data for the MFT entry
     */
    public void printMFTEntry() {
        System.out.println("Signature\t\t\t" + this.signature);
        System.out.println("Offset to fixup array\t\t" + this.offsetToFixupArray);
        System.out.println("Entries in fixup array\t\t" + this.entriesInFixupArray);
        System.out.println("$LogFile sequence number\t" + this.lsn);
        System.out.println("Sequence Value\t\t\t" + this.sequenceValue);
        System.out.println("Link count\t\t\t" + this.linkCount);
        System.out.println("Offset to first attribute\t" + this.offsetToFirstAttr);
        System.out.println("Flags\t\t\t\t" + this.flags);
        System.out.println("Used size of the MFT entry\t" + this.usedSizeOfMFTEntry);
        System.out.println("Allocated size of MFT entry\t" + this.allocatedSizeOfMFTEntry);
        System.out.println("Reference to base record\t" + this.referenceToBaseRecord);
        System.out.println("Next attribute ID\t\t" + this.nextAttrID);
    }

    /**
     * Parses the MFT entry into each of the variables
     *
     * @param rawEntryData
     */
    private void parseMFTEntry(byte[] rawEntryData) {
        ByteBuffer buf = ByteBuffer.wrap(rawEntryData);
        buf.order(ByteOrder.LITTLE_ENDIAN); // set the order to little endian

        // signature (bytes 0 - 3)
        byte[] sigBytes = new byte[4];
        for (int i = 0; i < sigBytes.length; i++) {
            sigBytes[i] = buf.get(i);
        }
        this.signature = new String(sigBytes);

        // offset to fixup array (bytes 4 - 5)
        this.offsetToFixupArray = buf.getShort(4);

        // number of entries in fixup array (bytes 6 - 7)
        this.entriesInFixupArray = buf.getShort(6);

        // $LogFile sequence number (bytes 8 - 15)
        this.lsn = buf.getLong(8);

        // sequence value (bytes 16 - 17)
        this.sequenceValue = buf.getShort(16);

        // link count (bytes 18 - 19)
        this.linkCount = buf.getShort(18);

        // offset to first attribute (bytes 20 - 21)
        this.offsetToFirstAttr = buf.getShort(20);

        // flags (bytes 22 - 23)
        this.flags = buf.getShort(22);

        // used size of MFT entry (bytes 24 - 27)
        this.usedSizeOfMFTEntry = buf.getInt(24);

        // allocated size of MFT entry (bytes 28 - 31)
        this.allocatedSizeOfMFTEntry = buf.getInt(28);

        // file reference to base record (bytes 32 - 39)
        this.referenceToBaseRecord = buf.getLong(32);

        // next attribute id (bytes 40 - 41)
        this.nextAttrID = buf.getShort(40);

		/*
		// attributes (Raw Data)
		int offset = 42;
		this.attributesRawData = new byte[981];
		for(int i = 0; i < this.attributesRawData.length; i++) {
			this.attributesRawData[i] = buf.get(offset);
			offset++;
		}
		*/
    }
}
