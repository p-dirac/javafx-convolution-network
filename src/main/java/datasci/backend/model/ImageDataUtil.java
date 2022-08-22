package datasci.backend.model;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Loads the Mnist dataset
 * Note: downlaod the handwritten digit image files from here: https://github.com/myleott/mnist_png
 * and save in directory structure like this:
 * mnist_png
 * ├───testing
 * │   ├───0
 * │   ├───1
 * │   ├───2
 * │   ├───3
 * │   ├───4
 * │   ├───5
 * │   ├───6
 * │   ├───7
 * │   ├───8
 * │   └───9
 * └───training
 * ├───0
 * ├───1
 * ├───2
 * ├───3
 * ├───4
 * ├───5
 * ├───6
 * ├───7
 * ├───8
 * └───9
 * <p>
 * The subDirectories 0 to 9 contain hundreds of digit images. After the image files are read
 * sequentially, and stored in matrix format, the matrix list is then randomly shuffled to avoid
 * skewed results.
 */
public class ImageDataUtil {
    private static final Logger LOG = Logger.getLogger(ImageDataUtil.class.getName());


    /**
     * Load image data into list of network data.
     *
     * @param parentDir directory to image input data
     * @return list of network data
     */
    public static List<List<NetData>> loadData(String parentDir, int numClassestoLoad, int numEachClass) {
        List<List<NetData>> dataList = new ArrayList<>();
        try {
            double pixelMin = 0.75;
            LOG.info("parentDir: " + parentDir + ", numClassestoLoad: " + numClassestoLoad
                    + ", numEachClass: " + numEachClass);
            // numClasses subdirectories, one for each image class
            // k = class index
            for (int k = 0; k < numClassestoLoad; k++) {
                // sub directory to one set of image files
                String subDir = String.valueOf(k);
                // full directory path to one set of image files
                String fullDir = parentDir + File.separator + subDir;
                //      LOG.info("loadData, fullDir: " + fullDir);
                File dirFile = new File(fullDir);
                if (dirFile.isDirectory()) {
                    // one set of image files
                    File[] files = dirFile.listFiles();
                    int numFiles = files.length;
                    if (numFiles > numEachClass) {
                        numFiles = numEachClass;
                    }
                    LOG.info("fullDir: " + fullDir + ", numFiles: " + numFiles);
                    // k = class index (subdirectory for files)
                    List<NetData> classDataList = loadClassData(files, k, numFiles);
                    dataList.add(classDataList);
                } else {
                    String msg = "Not a directory: " + fullDir;
                    throw new RuntimeException(new Exception(msg));
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dataList;
    }

    /**
     * Load image data into list of network data.
     *
     * @param parentDir directory to image input data
     * @return list of network data
     */
    public static List<List<NetData>> loadSmallSet(String parentDir, int numClassestoLoad, int numEachClass) {
        List<List<NetData>> dataList = new ArrayList<>();
        try {
            double pixelMin = 0.75;
            LOG.info("parentDir: " + parentDir + ", numClassestoLoad: " + numClassestoLoad
                    + ", numEachClass: " + numEachClass);
            // numClasses subdirectories, one for each image class
            // k = class index
            for (int k = 0; k < numClassestoLoad; k++) {
                // sub directory to one set of image files
                String subDir = String.valueOf(k);
                // full directory path to one set of image files
                String fullDir = parentDir + File.separator + subDir;
                //      LOG.info("loadData, fullDir: " + fullDir);
                File dirFile = new File(fullDir);
                if (dirFile.isDirectory()) {
                    // one set of image files
                    File[] files = dirFile.listFiles();
                    int numFiles = files.length;
                    if (numFiles > numEachClass) {
                        numFiles = numEachClass;
                    }
                    LOG.info("fullDir: " + fullDir + ", numFiles: " + numFiles);
                    // k = class index (subdirectory for files)
                    List<NetData> classDataList = loadClassData(files, k, numFiles);
                    dataList.add(classDataList);
                } else {
                    String msg = "Not a directory: " + fullDir;
                    throw new RuntimeException(new Exception(msg));
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dataList;
    }

    /**
     * Load image data for one class into list
     *
     * @param files      full directory to image input data
     * @param classIndex data class index (subdirectory name)
     * @param numFiles   number of file to load for this data class
     * @return list of image data for one class
     */
    public static List<NetData> loadClassData(File[] files, int classIndex, int numFiles) {
        List<NetData> classDataList = new ArrayList<>();
        try {
            for (int n = 0; n < numFiles; n++) {
                // one image file
                File f = files[n];
                if (f != null) {
                    // convert file to matrix of normalized pixel values
                    Matrix imageData = loadImageData(f);
                    // imageData = normalized pixels; k = actual output index
                    NetData data = new NetData(imageData, classIndex);
                    classDataList.add(data);
                } else {
                    LOG.info("File null for n: " + n);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return classDataList;
    }

    /**
     * Load image data into matrix of normalized pixel values
     * Note: assumes original image is white(255) on a black(0) background,
     * and normalizes the pixel values to between 0.0(black) and 1.0(white)
     *
     * @param f file input
     * @return the matrix of normalized pixel values
     */
    public static Matrix loadImageData(File f) {
        Matrix data = null;
        try {
            BufferedImage image = ImageIO.read(f);
            if (image != null) {
                int w = image.getWidth();
                int h = image.getHeight();
                data = new Matrix(h, w);
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        // Reads a 32-bit integer representation of the color of a pixel
                        int p = image.getRGB(y, x);
                        // extract each color (r,g,b), 0 to 255; ignore alpha a
                        // int a = (p>>24)&0xff;
                        int r = (p >> 16) & 0xff;
                        int g = (p >> 8) & 0xff;
                        int b = p & 0xff;

                        //calculate average, 0 to 255
                        double avg = (r + g + b) / 3.0;

                        // normalizes value to gray 0 to 1.0
                        double d = (avg / 255.0);

                        // translate to create - and + values : -0.5 to +0.5
                       // d = d - 0.5;

                        // digitize to small values
/*
                        if (d < 0.5) {
                            d = 0.0;
                        } else {
                            d = 0.1;
                        }

*/

                        /*
                        if (d < 0.5) {
                            d = 0.0;
                        } else if (d < 0.75) {
                            d = 0.1;
                        } else {
                            d = 0.8;
                        }

                         */
                        //   LOG.info("loadImageData, x: " + x + ", y: " + y + ", d: " + d);
                        MTX.setCell(data, x, y, d);
                    }
                }
            } else {
                LOG.log(Level.INFO, "image is null");
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return data;
    }


}  //end class
