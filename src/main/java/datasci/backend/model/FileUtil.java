package datasci.backend.model;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ron
 */
public class FileUtil {

    private static final Logger LOG = Logger.getLogger(
            FileUtil.class.getName());
    private static final int STREAM_BUFFER = 512 * 1024;

    public FileUtil() {
    }

    public static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[STREAM_BUFFER];
            int read = 0;
            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();

        } catch (IOException ex) {
            throw new RuntimeException("Error reading input stream", ex);
        }
        return baos.toByteArray();
    }

    public static ByteArrayInputStream fileStreamToBais(InputStream is) {
        ByteArrayInputStream bais = null;
        try {
            if (is != null) {
                byte[] fileBytes = FileUtil.streamToBytes(is);
                bais = new ByteArrayInputStream(fileBytes);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error reading input stream", ex);
        }
        return bais;
    }

    /**
     * Get file input stream
     *
     * @param fileName
     * @return ByteArrayInputStream
     */
    public static InputStream getInputStream(String fileName) {
        InputStream fis = null;
        try {
            fis = ClassLoader.getSystemResourceAsStream(fileName);

            if (fis == null) {
                LOG.log(Level.INFO, "Cannot find file: " + fileName);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fis;
    }

    /**
     * Get file input stream
     *
     * @param file
     * @return ByteArrayInputStream
     */
    public static InputStream getInputStream(File file) {
        InputStream fis = null;
        try {
            fis = new FileInputStream(file);

            if (fis == null) {
                LOG.log(Level.INFO, "Cannot find file: " + file.getName());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fis;
    }
    /**
     * Get file output stream
     *
     * @param file
     * @return FileOutputStream
     */
    public static OutputStream getOutputStream(File file) {
        OutputStream fos = null;
        try {
            //LOG.log(Level.INFO, "myfile path: " + myfile.getAbsolutePath());
            fos = fileToStream(file);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fos;
    }

    /**
     * Get file output stream
     *
     * @param fileName
     * @return FileOutputStream
     */
    public static OutputStream createOutputStream(String fileName) {
        OutputStream fos = null;
        try {
            File myfile = new File(fileName);
            //LOG.log(Level.INFO, "myfile path: " + myfile.getAbsolutePath());
            fos = fileToStream(myfile);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fos;
    }

    /**
     * Get file output stream
     *
     * @param fileName
     * @return FileOutputStream
     */
    public static OutputStream createOutputStream(String parent, String fileName) {
        OutputStream fos = null;
        try {
            File myfile = new File(parent, fileName);
            //LOG.log(Level.INFO, "myfile path: " + myfile.getAbsolutePath());
            fos = fileToStream(myfile);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fos;
    }

    /**
     * Get file output stream
     *
     * @param myfile
     * @return FileOutputStream
     */
    public static OutputStream fileToStream(File myfile) {
        OutputStream fos = null;
        try {
            //LOG.log(Level.INFO, "myfile path: " + myfile.getAbsolutePath());
            if (!myfile.exists()) {
                //file does not exist, create it
                if (myfile.createNewFile()) {
                    //file was created, create output stream
                    fos = new FileOutputStream(myfile);

                    if (fos == null) {
                        LOG.log(Level.INFO, "Cannot open file: " + myfile.getName());
                    }
                }
            } else {
                //file already exists, get output stream
                fos = new FileOutputStream(myfile);

                if (fos == null) {
                    LOG.log(Level.INFO, "File exists, but cannot open: " + myfile.getName());
                }

            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fos;
    }

    public static BufferedWriter fileToWriter(File myfile) {
        BufferedWriter w = null;
        try {
            OutputStream fos = fileToStream(myfile);
            w = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8), STREAM_BUFFER);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return w;
    }

    /**
     * Get file output stream
     *
     * @param fileName
     * @return ByteArrayInputStream
     */
    public static OutputStream getOutputStream(String fileName) {
        OutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);

            if (fos == null) {
                LOG.log(Level.INFO, "Cannot open file: " + fileName);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return fos;
    }

    public static void writeSerialFile(Object serialData) {
        try {
            FileOutputStream fos = new FileOutputStream("serial");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(serialData);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // Object deserialization
    public static void readSerialFile(Object serialData) {
        try {
            FileInputStream fis = new FileInputStream("serial");
            ObjectInputStream ois = new ObjectInputStream(fis);
            serialData = ois.readObject();
            ois.close();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


}  //end class
