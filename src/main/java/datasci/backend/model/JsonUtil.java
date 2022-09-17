package datasci.backend.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON utility methods to read and write json streams
 */
public class JsonUtil {

    private static final Logger LOG = Logger.getLogger(JsonUtil.class.getName());

    /**
     * Write network configuration to json file
     *
     * @param config
     * @param out    json output stream
     */
    public static void configToJson(NetConfig config, OutputStream out) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            boolean b = objectMapper.canSerialize(NetConfig.class);
            LOG.log(Level.INFO, "Can serialize NetConfig? " + b);
            objectMapper.writeValue(out, config);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read json file into network configuration
     *
     * @param src json input stream
     * @return NetConfig
     */
    public static NetConfig jsonToConfig(InputStream src) {
        NetConfig config = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            config = objectMapper.readValue(src, NetConfig.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    /**
     * Convert network configuration into json string
     *
     * @param config network configuration to convert
     * @return json string
     */
    public static String configToJson(NetConfig config) {
        String jsonStr = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonStr = objectMapper.writeValueAsString(config);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return jsonStr;
    }

    /**
     * Convert json string into network configuration
     *
     * @param src json string to convert
     * @return NetConfig network configuration
     */
    public static NetConfig jsonToConfig(String src) {
        NetConfig config = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            config = objectMapper.readValue(src, NetConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return config;
    }

    /**
     * Write network FitParams to json file
     *
     * @param fitParams network training FitParams, including weights and biases for all layers
     * @param out      json output stream
     */
    public static void FitParamsToJson(FitParams fitParams, OutputStream out) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            //  objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, config);
            boolean b = objectMapper.canSerialize(FitParams.class);
            LOG.log(Level.INFO, "Can serialize FitParams? " + b);
            objectMapper.writeValue(out, fitParams);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read json file into network training FitParams, including weights and biases for all layers
     *
     * @param src json input stream
     * @return FitParams
     */
    public static FitParams jsonToFitParams(InputStream src) {
        FitParams fitParams = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            fitParams = objectMapper.readValue(src, FitParams.class);

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return fitParams;
    }

    /**
     * Write list of String array to json file
     *
     * @param dataList list of String array
     * @param out      json output stream
     */
    public static void strListToJson(List<String[]> dataList, OutputStream out) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //   TypeReference<List<double[]>> mapType = new TypeReference<List<double[]>>() {};
            objectMapper.writeValue(out, dataList);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read json file into list of String array
     *
     * @param src json input stream
     * @return list of String array
     */
    public static List<String[]> jsonToStrList(InputStream src) {
        List<String[]> dataList = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<String[]>> mapType = new TypeReference<List<String[]>>() {
            };
            dataList = objectMapper.readValue(src, mapType);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dataList;
    }

    /**
     * Write list of double array to json file
     *
     * @param dataList list of double array
     * @param out      json output stream
     */
    public static void dataListToJson(List<double[]> dataList, OutputStream out) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            //   TypeReference<List<double[]>> mapType = new TypeReference<List<double[]>>() {};
            objectMapper.writeValue(out, dataList);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read json file into list of double array
     *
     * @param src json input stream
     * @return list of double array
     */
    public static List<double[]> jsonToDataList(InputStream src) {
        List<double[]> dataList = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<double[]>> mapType = new TypeReference<List<double[]>>() {
            };
            dataList = objectMapper.readValue(src, mapType);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return dataList;
    }

    /**
     * Write matrix list json file
     *
     * @param matrixList
     * @param out        json output stream
     */
    public static void matrixListToJson(List<Matrix> matrixList, OutputStream out) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<Matrix>> mapType = new TypeReference<List<Matrix>>() {
            };
            objectMapper.writeValue(out, matrixList);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read json file into matrix list
     *
     * @param src json input stream
     * @return matrix list
     */
    public static List<Matrix> jsonToMatrixList(InputStream src) {
        List<Matrix> matrixList = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<List<Matrix>> mapType = new TypeReference<List<Matrix>>() {
            };
            matrixList = objectMapper.readValue(src, mapType);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return matrixList;
    }

    /**
     * Write matrix to json file
     *
     * @param m   matrix to convert
     * @param out json output stream
     */
    public static void matrixToJson(Matrix m, OutputStream out) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(out, m);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read json file into matrix
     *
     * @param src json input stream
     * @return matrix
     */
    public static Matrix jsonToMatrix(InputStream src) {
        Matrix m = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            m = objectMapper.readValue(src, Matrix.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return m;
    }

    /**
     * Convert matrix into json string
     *
     * @param m matrix to convert
     * @return json string
     */
    public static String matrixToJson(Matrix m) {
        String jsonStr = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonStr = objectMapper.writeValueAsString(m);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return jsonStr;
    }

    /**
     * Convert json string into matrix
     *
     * @param src json string to convert
     * @return matrix
     */
    public static Matrix jsonToMatrix(String src) {
        Matrix m = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            m = objectMapper.readValue(src, Matrix.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return m;
    }

} // end class