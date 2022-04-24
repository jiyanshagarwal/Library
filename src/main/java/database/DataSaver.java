package database;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * Uses RandomAccessFile to read and write data to file in a modified UTF-8 encoding. Reads only based on this encoding.
 *
 * @author Jiyansh Agarwal
 */
public class DataSaver {

    final private File path;
    private long length = 0;

    /**
     * Asks user for file path and name. Creates file if it doesn't already exist.
     *
     * @param path Directory of file from root.
     * @param fileName Name of file
     * @param length The length to make the file.
     */
    public DataSaver(String path, String fileName, long length) {
        String error = "DataSaver Constructor: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (path == null) {
                throw new NullPointerException(error + "Null directory");
            }
            if (fileName == null) {
                throw new NullPointerException(error + "Null file name");
            }
            if (fileName.length() == 0 || fileName.trim().length() == 0
                    || path.length() == 0 || path.trim().length() == 0) {
                throw new IOException(error + "Empty file name or directory not allowed!");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
        //-------------------------------------------------------------------------

        this.path = new File(path + File.separator + fileName);
        this.length = length;

        try {
            if (!this.path.exists()) {
                this.path.createNewFile();
                RandomAccessFile file = new RandomAccessFile(path, "rw");
                file.setLength(length);
            }

            //Check if file is writable and readable.        
            if (!this.path.canRead() || !this.path.canWrite()) {
                throw new IOException(error + "File can't be written to and/or read from.");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
    }
    
    /**
     * Asks user for file path and name. Creates file if it doesn't already exist.
     *
     * @param path Directory of file from root.
     * @param fileName Name of file
     */
    public DataSaver(String path, String fileName) {
        String error = "DataSaver Constructor: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (path == null) {
                throw new NullPointerException(error + "Null directory");
            }
            if (fileName == null) {
                throw new NullPointerException(error + "Null file name");
            }
            if (fileName.length() == 0 || fileName.trim().length() == 0
                    || path.length() == 0 || path.trim().length() == 0) {
                throw new IOException(error + "Empty file name or directory not allowed!");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
        //-------------------------------------------------------------------------

        this.path = new File(path + File.separator + fileName);

        try {
            if (!this.path.exists()) {
                this.path.createNewFile();
            }

            //Check if file is writable and readable.        
            if (!this.path.canRead() || !this.path.canWrite()) {
                throw new IOException(error + "File can't be written to and/or read from.");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
    }

    /**
     * Asks user for file path with name. Creates file if it doesn't already exist.
     *
     * @param path Directory of file from root.
     * @param length The length to make the file.
     */
    public DataSaver(String path, long length) {
        String error = "DataSaver Constructor: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (path == null) {
                throw new NullPointerException(error + "Null directory");
            }
            if (path.length() == 0 || path.trim().length() == 0) {
                throw new IOException(error + "Empty file name or directory not allowed!");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
        //-------------------------------------------------------------------------

        this.path = new File(path);
        this.length = length;

        try {
            if (!this.path.exists()) {
                this.path.createNewFile();
                RandomAccessFile file = new RandomAccessFile(path, "rw");
                file.setLength(length);
            }

            //Check if file is writable and readable.
            if (!this.path.canRead() || !this.path.canWrite()) {
                throw new IOException(error + "File can't be written to and/or read from.");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
    }
    
    /**
     * Asks user for file path with name. Creates file if it doesn't already exist.
     *
     * @param path Directory of file from root.
     */
    public DataSaver(String path) {
        String error = "DataSaver Constructor: ";

        //-------------------------------[Input Validation]------------------------
        try {
            if (path == null) {
                throw new NullPointerException(error + "Null directory");
            }
            if (path.length() == 0 || path.trim().length() == 0) {
                throw new IOException(error + "Empty file name or directory not allowed!");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
        //-------------------------------------------------------------------------

        this.path = new File(path);

        try {
            if (!this.path.exists()) {
                this.path.createNewFile();
            }

            //Check if file is writable and readable.
            if (!this.path.canRead() || !this.path.canWrite()) {
                throw new IOException(error + "File can't be written to and/or read from.");
            }
        } catch (IOException e) {
            System.out.println(error + e);
        }
    }

    /**
     * Writes book data to file and returns the length of written data if successfully written.
     *
     * @param text Data to write.
     * @param position Location to write at.
     * @return the length of written data if successfully written, -1 otherwise.
     */
    public long writeUTF(String text, long position) {
        String error = "DataSaver writeUTF: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "rw");
            long writtenLength;

            file.seek(position);
            file.writeUTF(text);
            writtenLength = file.getFilePointer();

            file.close();

            return writtenLength - position;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return -1;
    }

    /**
     * Reads file data from start index until the end of the specific data.
     *
     * @param start Position in file to start reading from.
     * @return The requested data as a String. Null if data not read.
     */
    public String readUTF(long start) {
        String error = "DataSaver readUTF: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");

            file.seek(start);
            String text = file.readUTF();

            file.close();

            return text;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return null;
    }

    /**
     * Reads <code>length</code> number of bytes from start index.
     *
     * @param start Position in file to start reading from.
     * @param length How many bytes to read.
     * @return The requested data as a String.
     */
    public String read(int start, int length) {
        String error = "DataSaver read: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            byte[] encodedText = new byte[length];

            file.seek(start);
            file.readFully(encodedText, 0, length);
            String text = new String(encodedText, "UTF-8");
            file.close();

            return text;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return null;
    }

    /**
     * Writes data from a byte array to the specified position in the file.
     *
     * @param text The data to write.
     * @param position The position to write from.
     * @return True only if successfully written.
     */
    public boolean writeBytes(byte[] text, int position) {
        String error = "DataSaver writeBytes: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "rw");

            file.seek(position);
            file.write(text);
            file.close();

            return true;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return false;
    }

    /**
     * Writes data from a byte to the specified position in the file.
     *
     * @param text The data to write.
     * @param position The position to write from.
     * @return True only if successfully written.
     */
    public boolean writeByte(byte text, int position) {
        String error = "DataSaver writeByte: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "rw");

            file.seek(position);
            file.writeByte(text);
            file.close();

            return true;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return false;
    }

    /**
     * Writes number to the specified position in the file.
     *
     * @param num The number to be written.
     * @param position The position to write from.
     * @return True only if successfully written.
     */
    public boolean writeInt(int num, int position) {
        String error = "DataSaver writeByte: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "rw");

            file.seek(position);
            file.writeInt(num);
            file.close();

            return true;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return false;
    }

    /**
     * Reads <code>length</code> number of bytes from start index.
     *
     * @param start Position in file to start reading from.
     * @param length How many bytes to read.
     * @return The requested data as a byte array.
     */
    public byte[] readBytes(int start, int length) {
        String error = "DataSaver readBytes: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            byte[] text = new byte[length];

            file.seek(start);
            file.readFully(text, 0, length);
            file.close();

            return text;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return null;
    }

    /**
     * Reads and integer (i.e. 4 bytes) from the file at the start index. NOTE: Will return max integer value if there is an error!
     *
     * @param start Position in file to start reading from.
     * @return The requested data as an integer.
     */
    public int readInt(int start) {
        String error = "DataSaver readInt: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");

            file.seek(start);
            int integer = file.readInt();
            file.close();

            return integer;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Reads the short value before each data set to get the length of that data.
     *
     * @param start Position of data.
     * @return The length of the data.
     */
    public short readDataLength(long start) {
        String error = "DataSaver readDataLength: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");

            file.seek(start);
            short length = file.readShort();

            file.close();

            return length;
        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return -1;
    }

    /**
     * Gets the end of a UTF-8 string
     *
     * @param start Start point of the string.
     * @return The end index of the string.
     */
    public long getEndPosition(long start) {
        String error = "DataSaver getEndPosition: ";
        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");

            file.seek(start);
            file.readUTF();

            long pos = file.getFilePointer();

            file.close();

            return pos;

        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return -1;
    }

    /**
     * Returns the length of the file.
     *
     * @return length of file in number of bytes. Returns -1 if file does not exist.
     */
    public long getFileLength() {
        String error = "DataSaver getFileLength: ";
        try {
            return new RandomAccessFile(path, "r").length();
        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return -1;
    }

    /** 
     * @return the full path of the file.
     */
    public String getFilePath() {
        return path.getPath();
    }
    
    /**
     * Sets the file length. If new length is less than previous length, data may be truncated.
     * @param length 
     * @return true if length is successfully changed.
     */
    public boolean setLength(long length) {        
        String error = "DataSaver setLength: ";
        
        this.length = length;
        
        try {
            new RandomAccessFile(path, "rw").setLength(length);
            return true;
        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return false;
    }
    
    /**
     * Deletes all the data in the file.
     *
     * @return True if operation successful.
     */
    public boolean clearFile() {
        String error = "DataSaver clearFile: ";
        try {
            new RandomAccessFile(path, "rw").setLength(0);
            new RandomAccessFile(path, "rw").setLength(length);
            return true;
        } catch (FileNotFoundException e) {
            System.out.println(error + e);
        } catch (IOException e) {
            System.out.println(error + e);
        }
        return false;
    }
}
