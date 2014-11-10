package com.spullen.ntfs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

public class NTFSParser {

    private String ntImageFile;
    private int bytesToNTFSPartition;

    // boot sector values
    private int bytesPerSector;
    private int sizeOfMFTEntry;
    private int sizeOfIndexRecord;
    private int sectorsPerCluster;
    private long MFTStart;
    private String serialNumber;
    private String signature;
    private long totalSectors;
    private long MFTMirrorStart;
    private String oem;
    private int mediaDescriptor;

    // MFT entries
    private List<MFTEntry> MFTEntries = new LinkedList<MFTEntry>();

    /**
     * Initialize the parser object
     *
     * @param ntImageFile
     */
    public NTFSParser(String ntImageFile, int offsetToPartition) {
        this.ntImageFile = ntImageFile;
        this.bytesToNTFSPartition = offsetToPartition;
    }

    /**
     * Parse in the boot sector
     * Retrieve necessary values to parse the MFT
     */
    public void parseBootSector() {
        byte[] bootSector = new byte[512];
        bootSector = FileReader.seekAndRead(bootSector, this.bytesToNTFSPartition, this.ntImageFile); // read in the boot sector

        // wrap the bootSector into a ByteBuffer
        ByteBuffer bootSectorBuf = ByteBuffer.wrap(bootSector);
        bootSectorBuf.order(ByteOrder.LITTLE_ENDIAN);

        int offset = 0;

        // OEM
        byte[] oemBytes = new byte[7];
        offset = 3;
        for (int i = 0; i < oemBytes.length; i++) {
            oemBytes[i] = bootSectorBuf.get(offset);
            offset++;
        }
        this.oem = new String(oemBytes);

        // Bytes Per Sector
        this.bytesPerSector = bootSectorBuf.getShort(11);

        // sectors per cluster
        this.sectorsPerCluster = bootSectorBuf.get(13);

        // media descriptor
        this.mediaDescriptor = 0xFF & (int) bootSectorBuf.get(21);

        // total sectors in file system
        this.totalSectors = bootSectorBuf.getLong(40);

        // starting cluster address of MFT
        this.MFTStart = bootSectorBuf.getLong(48);

        // starting cluster address of MFT mirror $DATA attribute
        this.MFTMirrorStart = bootSectorBuf.getLong(56);

        // size of file record (MFT entry)
        this.sizeOfMFTEntry = bootSectorBuf.get(64);
        System.out.format("%02X\n", bootSectorBuf.get(64));
        System.out.println(this.sizeOfMFTEntry);
        if (this.sizeOfMFTEntry < 0) {
            this.sizeOfMFTEntry = (int) Math.pow(2, Math.abs(this.sizeOfMFTEntry));
        }

        // size of index record
        this.sizeOfIndexRecord = bootSectorBuf.get(68);
        if (this.sizeOfIndexRecord < 0) {
            this.sizeOfIndexRecord = (int) Math.pow(2, Math.abs(this.sizeOfIndexRecord));
        }

        // serial number
        this.serialNumber = String.format("%08X", bootSectorBuf.getLong(72));

        // signature (0xAA55)
        this.signature = String.format("%02X%02X", bootSectorBuf.get(511), bootSectorBuf.get(510));
    }

    /**
     * Parses the MFT entry (This is the first entry of the MFT!)
     */
    public void parseMFTEntry() {
        // figure out how far to seek into the image to get the MFT
        int seek = (int) this.MFTStart * this.sectorsPerCluster * this.bytesPerSector;

        // create a new byte array the size of an MFT entry
        byte[] mft = new byte[this.sizeOfMFTEntry];
        // read in the MFT MFT entry
        mft = FileReader.seekAndRead(mft, seek + this.bytesToNTFSPartition, this.ntImageFile);

        // create a new MFTEntry object and allow it to parse this entry
        MFTEntry mftEntry = new MFTEntry(mft);

        // print out the MFT Entry information
        mftEntry.printMFTEntry();
        mftEntry.parseAttributes();

        // add the mft entry to the list of mft entries
        this.MFTEntries.add(mftEntry);
    }

    /**
     * Print out all of the stuff from the boot sector
     */
    public void printBootSector() {
        System.out.println("Bytes per sector\t" + this.bytesPerSector);
        System.out.println("Size of MFT entry\t" + this.sizeOfMFTEntry);
        System.out.println("Size of index record\t" + this.sizeOfIndexRecord);
        System.out.println("Sectors per cluster\t" + this.sectorsPerCluster);
        System.out.println("\tMFT start\t" + this.MFTStart);
        System.out.println("\tSerial number\t" + this.serialNumber);
        System.out.println("Signature(0xAA55)\t" + this.signature);
        System.out.println("\tTotal sectors\t" + this.totalSectors);
        System.out.println("MFT Mirror start\t" + this.MFTMirrorStart);
        System.out.println("\tOEM\t\t" + this.oem);
        System.out.println("Media descriptor\t" + this.mediaDescriptor);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String ntImageFile = "";
        int offsetToPartition = 0;

        // read in the parameters and options
        if (args.length > 1) {
            String arg = args[0];
            if (arg.equals("-o")) {
                offsetToPartition = (new Integer(args[1])).intValue() * 512;
                ntImageFile = args[2];
            } else {
                ntImageFile = args[0];
                offsetToPartition = (new Integer(args[2])).intValue() * 512;
            }
        } else if (args.length == 1) {
            String arg = args[0];
            if (arg.equals("-h")) {
                NTFSParser.instructions();
                System.exit(0);
            } else {
                ntImageFile = args[0];
            }
        } else { // else no arguments so print out the instructions and exit
            NTFSParser.instructions();
            System.exit(0);
        }

        // Time to parse!
        NTFSParser ntParser = new NTFSParser(ntImageFile, offsetToPartition);
        ntParser.parseBootSector(); // parse the boot sector
        ntParser.printBootSector(); // print the information from the boot sector
        ntParser.parseMFTEntry(); // parse the MFT entry of the MFT !
    }

    /**
     * Print out the correct usage of the program
     */
    public static void instructions() {
        System.out.println("utility NTFS_IMAGE_PATH [-o OFFSET]");
        System.out.println("utility -h");
        System.out.println("Lists meta information about the NTFS file system image.");
        System.out.println("This tool does not parse partition tables such as MBR or GPT so offset describes the " +
                "number of sectors (512 byte units) into the image that the NTFS partition exists. For normal disk " +
                "images (MBR with single partition), this value is typically 128 or for direct images it will " +
                "typically be 0.");
        System.out.println("\t-o\tOffset sector count (Optional, default is 0)");
        System.out.println("\t-h\tPrints this message.");
    }
}
