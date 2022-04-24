package database;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Jiyansh Agarwal
 */
public class StorageHashTable {

    final public int NUMBER_OF_BLOCKS;
    final public int MAX_KEY_LENGTH;
    final public int[] RECORD_SIZES;

    final static private int DELETED = Integer.MAX_VALUE;           //Integer.MAX_VALUE is a flag for a deleted element.
    final private int BLOCK_SIZE;
    final private DataSaver SAVE;
    final private DataSaver KEY_SAVE;
    private int numOfEntries = 0;
    private String[] keys;

    /**
     * Takes amount of entries and entry length to create a file of that length with blocks of data representing each entry.
     *
     * @param filePath Directory to store the hash table.
     * @param fileName Name of file. Also used for the key file.
     * @param numOfEntries The maximum number of entries allowed in the file.
     * @param maxKeyLength The maximum length that a key can be.
     * @param numOfRecords The number of records stored per entry.
     * @param recordLengths An array of the lengths of each record in the order they will be stored.
     */
    public StorageHashTable(String filePath, String fileName, int numOfEntries, int maxKeyLength, int numOfRecords, int[] recordLengths) {
        String error = "StorageHashTable Constructor: ";

        //-------------------------------[Input Validation]------------------------    
        try {
            if (numOfEntries <= 0 || numOfRecords <= 0 || maxKeyLength <= 0) {
                throw new IOException(error + "All input lengths must be greater than 0!");
            }
            if (numOfRecords != recordLengths.length) {
                throw new IOException(error + "Each record must have a length given.");
            }
            if (numOfEntries > Integer.MAX_VALUE - 1) {
                throw new IndexOutOfBoundsException(error + "Too many entries!");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        //-------------------------------------------------------------------------

        int blockLength = 0;

        //Allows two byte space for storing length information and counts total bytes needed.
        for (int i = 0; i < recordLengths.length; i++) {
            recordLengths[i] = recordLengths[i] + 2;
            blockLength += recordLengths[i];
        }

        NUMBER_OF_BLOCKS = numOfEntries;
        this.MAX_KEY_LENGTH = maxKeyLength + 2;                           //Adds two byte space for length information.
        BLOCK_SIZE = blockLength + this.MAX_KEY_LENGTH + 4;               //Adds space needed to accomadate hash code's extra four byte space.
        RECORD_SIZES = recordLengths.clone();

        SAVE = new DataSaver(filePath + File.separator + fileName, NUMBER_OF_BLOCKS * BLOCK_SIZE);
        KEY_SAVE = new DataSaver(filePath + File.separator + fileName.substring(0, fileName.indexOf('.')) + ".keys", NUMBER_OF_BLOCKS * this.MAX_KEY_LENGTH);

        loadKeys();
    }

    /**
     * Takes a key and hashes it to find which block to store entry in. Writes the records to that block along with the hash.
     *
     * @param key The string used to find the storage location. Note: Duplicate keys not allowed..
     * @param records The records to store. These must match the size parameters defined in the constructor in both length and number.
     * @return The location where entry is stored. Returns -1 if no location is found or if entry is a duplicate.
     */
    public int addEntry(String key, String[] records) {
        String error = "StorageHashTable addEntry: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (key == null || key.length() == 0 || records.length == 0) {
                throw new IOException("Input lengths must be greater than 0!");
            }
            if (key.length() > MAX_KEY_LENGTH - 2) {
                throw new IndexOutOfBoundsException(error + "Key length cannot exceed max key length!");
            }
            if (this.containsKey(key)) {
                throw new IOException("Duplicate keys not allowed. All keys must be unique");
            }
            if (records.length != RECORD_SIZES.length) {
                throw new IOException(error + "Amount of records in array does not match previously allocated amount!");
            }

            for (int i = 0; i < records.length; i++) {
                if (records[i].length() + 2 > RECORD_SIZES[i]) {
                    throw new IOException("Record " + i + "in array (records[" + i + "] = " + records[i] + ") is too long!");
                }
            }
        } catch (IOException e) {
            System.out.println(error + e);
            return -1;
        }
        //-------------------------------------------------------------------------

        int block = this.getHashCode(key);
        int blockProbe = block;

        do {
            if (blockIsEmpty(blockProbe) || blockIsDeleted(blockProbe)) {
                if (writeData(blockProbe, key, records, true)) {
                    return blockProbe;
                }
                return -1;

            } else if (blockProbe == NUMBER_OF_BLOCKS) {
                blockProbe = 1;                                         //If at the end of file, start at the top again.
            } else {
                blockProbe++;
            }
        } while (blockProbe != block);                                  //Keep searching for empty block unitl back to the start.

        return -1;
    }

    /**
     * Reads data from the block that matches the given key.
     *
     * @param key The key to used to find data.
     * @return The data for that key. Null if key not found.
     */
    public String[] readEntry(String key) {
        String error = "StorageHashTable readEntry: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (key == null || key.length() == 0) {
                throw new IOException(error + "Key length must be greater than 0!");
            }
            if (key.length() > MAX_KEY_LENGTH - 2) {
                throw new IndexOutOfBoundsException(error + "Key length cannot exceed max key length!");
            }
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
        //-------------------------------------------------------------------------

        int block = this.getHashCode(key);
        int blockProbe = block;

        do {
            if (blockIsEmpty(blockProbe)) {
                return null;
            } else if (blockIsDeleted(blockProbe)) {
                blockProbe++;
            } else if (getKey(blockProbe).equals(key)) {
                return readData(blockProbe);
            } else if (blockProbe == NUMBER_OF_BLOCKS) {
                blockProbe = 1;                                         //If at the end of file, start at the top again.
            } else {
                blockProbe++;
            }
        } while (blockProbe != block);                                  //Keep searching for requested block unitl back to the start.

        return null;
    }

    /**
     * Replaces data in the block that matches the given key with the given data.
     *
     * @param key The key to used to find entry.
     * @param records The data to replace existing entry with.
     * @return The previous data for that key. Null if key not found.
     */
    public String[] changeRecords(String key, String[] records) {
        String error = "StorageHashTable replaceEntry: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (key == null || key.length() == 0 || records.length == 0) {
                throw new IOException(error + "Input lengths must be greater than 0!");
            }
            if (key.length() > MAX_KEY_LENGTH - 2) {
                throw new IndexOutOfBoundsException(error + "Key length cannot exceed max key length!");
            }
            if (records.length != RECORD_SIZES.length) {
                throw new IOException(error + "Amount of records in array does not match previously allocated amount!");
            }

            for (int i = 0; i < records.length; i++) {
                if (records[i].length() + 2 > RECORD_SIZES[i]) {
                    throw new IOException(error + "Record " + i + "in array (records[" + i + "] = " + records[i] + ") is too long!");
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
        //-------------------------------------------------------------------------

        int block = this.getHashCode(key);
        int blockProbe = block;

        do {
            if (blockIsEmpty(blockProbe)) {
                return null;
            } else if (blockIsDeleted(blockProbe)) {
                blockProbe++;
            } else if (getKey(blockProbe).equals(key)) {
                String[] oldData = readData(blockProbe);

                if (writeData(blockProbe, key, records, false)) {
                    return oldData;
                }
                return null;

            } else if (blockProbe == NUMBER_OF_BLOCKS) {
                blockProbe = 1;                                         //If at the end of file, start at the top again.
            } else {
                blockProbe++;
            }
        } while (blockProbe != block);                                  //Keep searching for requested block unitl back to the start.

        return null;
    }

    /**
     * Deletes an entry that matches the key.
     *
     * @param key The key used to find entry to delete.
     * @return The deleted data.
     */
    public String[] deleteEntry(String key) {
        String error = "StorageHashTable deleteEntry: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (key == null || key.length() == 0) {
                throw new IOException(error + "Key length must be greater than 0!");
            }
            if (key.length() > MAX_KEY_LENGTH - 2) {
                throw new IndexOutOfBoundsException(error + "Key length cannot exceed max key length!");
            }
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
        //-------------------------------------------------------------------------

        int block = this.getHashCode(key);
        int blockProbe = block;

        do {
            if (blockIsEmpty(blockProbe)) {
                return null;
            } else if (getKey(blockProbe).equals(key)) {
                String[] data = readData(blockProbe);

                if (deleteData(blockProbe, key)) {
                    return data;
                }

                return null;

            } else if (blockProbe == NUMBER_OF_BLOCKS) {
                blockProbe = 1;                                         //If at the end of file, start at the top again.
            } else {
                blockProbe++;
            }
        } while (blockProbe != block);                                  //Keep searching for requested block unitl back to the start.

        return null;
    }

    /**
     * Gets the key stored in a block.
     *
     * @param blockNum The block to read from.
     * @return The stored key.
     */
    public String getKey(int blockNum) {
        if (blockIsEmpty(blockNum) || blockIsDeleted(blockNum)) {
            return null;
        }

        int positionToRead = ((blockNum - 1) * BLOCK_SIZE) + 4;
        return SAVE.readUTF(positionToRead);
    }

    /**
     * Returns the keys stored in the table.
     *
     * @return the keys in an array.
     */
    public String[] getKeys() {
        return keys;
    }

    /**
     * Checks if a key is in the table.
     *
     * @param key The key to find.
     * @return True only if key is found.
     */
    public boolean containsKey(String key) {
        String error = "StorageHashTable containsKey: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (key == null || key.length() == 0) {
                throw new IOException(error + "Key length must be greater than 0!");
            }
            if (key.length() > MAX_KEY_LENGTH - 2) {
                throw new IndexOutOfBoundsException(error + "Key length cannot exceed max key length!");
            }
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        //-------------------------------------------------------------------------

        int block = this.getHashCode(key);
        int blockProbe = block;

        do {
            if (blockIsEmpty(blockProbe) || blockIsDeleted(blockProbe)) {
                return false;
            } else if (getKey(blockProbe).equals(key)) {
                return true;
            } else if (blockProbe == NUMBER_OF_BLOCKS) {
                blockProbe = 0;                                         //If at the end of file, start at the top again.
            } else {
                blockProbe++;
            }
        } while (blockProbe != block);                                  //Keep searching for requested key unitl back to the start.

        return false;
    }

    /**
     * Gets the hashCode for a specified key.
     *
     * @param key The key to hash.
     * @return The hash of the key. Cannot be zero.
     */
    public int getHashCode(String key) {
        String error = "StorageHashTable getHashCode: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (key == null || key.length() == 0) {
                throw new IOException(error + "Key length must be greater than 0!");
            }
            if (key.length() > MAX_KEY_LENGTH - 2) {
                throw new IndexOutOfBoundsException(error + "Key length cannot exceed max key length!");
            }
        } catch (IOException e) {
            System.out.println(e);
            return -1;
        }
        //-------------------------------------------------------------------------

        int notNegativeHash = key.hashCode() & 0x7FFFFFFF;                  //Makes the hashcode positive by removing the sign bit.              
        return (notNegativeHash % NUMBER_OF_BLOCKS) + 1;                    //Hash can't be 0 because that is used to denote empty block.
    }

    /**
     * Gets the number of entries in the file.
     *
     * @return The number of entries.
     */
    public int size() {
        return numOfEntries;
    }

    /**
     * Tells if the file is empty.
     *
     * @return True if file is empty.
     */
    public boolean isEmpty() {
        return numOfEntries == 0;
    }

    /**
     * Returns the length of the file
     *
     * @return The length of the file in bytes. Returns -1 if file does not exist.
     */
    public long getFileLength() {
        return SAVE.getFileLength();
    }
    
    /**
     * @return the full file path.
     */
    public String getFilePath() {
        return SAVE.getFilePath();
    }

    /**
     * Clears all the data in the table.
     *
     */
    public void clearFile() {
        SAVE.clearFile();
        KEY_SAVE.clearFile();
        keys = new String[NUMBER_OF_BLOCKS];
        numOfEntries = 0;
    }
    
    /**
     * Clones current hashTable parameters. Does NOT clone data.
     * @param filePath Directory to store the hash table.
     * @param fileName Name of file. Also used for the key file.
     * @return the new, empty hashTable with the same parameters.
     */
    public StorageHashTable clone(String filePath, String fileName) {
        return new StorageHashTable(filePath, fileName, this.NUMBER_OF_BLOCKS, this.MAX_KEY_LENGTH, this.RECORD_SIZES.length, this.RECORD_SIZES);
    }

    /**
     * Writes each record to the file.
     *
     * @param blockNum The block to write in.
     * @param key The key to store.
     * @param data The records to write.
     * @param newKey Set to true if key hasn't been added. False if only data is being replaced.
     * @return True if write successful.
     */
    private boolean writeData(int blockNum, String key, String[] data, boolean newKey) {
        int realPosition = (blockNum - 1) * BLOCK_SIZE;

        if (SAVE.writeInt(getHashCode(key), realPosition) == false) {
            return false;
        }
        realPosition += 4;

        SAVE.writeUTF(key, realPosition);
        realPosition += MAX_KEY_LENGTH;

        for (int i = 0; i < RECORD_SIZES.length; i++) {
            SAVE.writeUTF(data[i], realPosition);
            realPosition += RECORD_SIZES[i];                            //Moves the position that the file writes to the next record location.
        }

        if (newKey) {
            numOfEntries++;
            keys[blockNum - 1] = key;

            KEY_SAVE.writeUTF(key, blockNum * MAX_KEY_LENGTH);
        }
        return true;
    }

    /**
     * Reads each record from the file.
     *
     * @param blockNum The block to read from.
     * @return Records as a String array. Null if block is empty.
     */
    private String[] readData(int blockNum) {
        String error = "StorageHashTable readData: ";

        //-------------------------------[Input Validation]------------------------
        if (blockIsEmpty(blockNum)) {
            return null;
        }
        if (blockNum < 1 || blockNum > NUMBER_OF_BLOCKS) {
            throw new IndexOutOfBoundsException(error + "Block number out of bounds!");
        }
        //-------------------------------------------------------------------------

        int realPosition = ((blockNum - 1) * BLOCK_SIZE) + MAX_KEY_LENGTH + 4;
        String[] records = new String[RECORD_SIZES.length];

        for (int i = 0; i < RECORD_SIZES.length; i++) {
            records[i] = SAVE.readUTF(realPosition);
            realPosition += RECORD_SIZES[i];                            //Moves the position that the file writes to the next record location.
        }
        return records;
    }

    /**
     * Marks a block as empty. This does not actually remove the stored data.
     *
     * @param blockNum The block to mark as empty.
     * @param key The key that will be deleted. Only used in case data must be restored.
     * @return True only if block was emptied. Attempts to restore data if unsuccessful.
     */
    private boolean deleteData(int blockNum, String key) {
        String error = "StorageHashTable deleteData: ";

        //-------------------------------[Input Validation]------------------------
        if (blockIsEmpty(blockNum)) {
            return false;
        }
        if (blockNum < 1 || blockNum > NUMBER_OF_BLOCKS) {
            throw new IndexOutOfBoundsException(error + "Block number out of bounds!");
        }
        //-------------------------------------------------------------------------

        int realPosition = (blockNum - 1) * BLOCK_SIZE;

        if (SAVE.writeInt(DELETED, realPosition) && KEY_SAVE.writeUTF("", blockNum * MAX_KEY_LENGTH) != -1) {
            numOfEntries--;
            keys[blockNum - 1] = null;
            return true;
        }

        SAVE.writeInt(getHashCode(key), realPosition);
        KEY_SAVE.writeUTF(key, blockNum * MAX_KEY_LENGTH);
        return false;
    }

    /**
     * Loads all keys from file into key array.
     */
    private void loadKeys() {
        long readPosition = 0;
        keys = new String[NUMBER_OF_BLOCKS];

        for (int i = 0; i < keys.length; i++) {
            String key = KEY_SAVE.readUTF(readPosition);
            readPosition += this.MAX_KEY_LENGTH;

            if (key != null && key.length() > 0) {
                keys[i] = key;
                this.numOfEntries++;
            }
        }
    }

    /**
     * Checks if a block is empty.
     *
     * @param blockNum The block to check.
     * @return True only if it is empty.
     */
    private boolean blockIsEmpty(int blockNum) {
        int realPosition = (blockNum - 1) * BLOCK_SIZE;
        int value = SAVE.readInt(realPosition);
        return value == 0;
    }

    /**
     * Checks if a block is marked DELETED.
     *
     * @param blockNum The block to check.
     * @return True only if block is marked DELETED.
     */
    private boolean blockIsDeleted(int blockNum) {
        int realPosition = (blockNum - 1) * BLOCK_SIZE;
        int value = SAVE.readInt(realPosition);
        return value == DELETED;
    }
}
