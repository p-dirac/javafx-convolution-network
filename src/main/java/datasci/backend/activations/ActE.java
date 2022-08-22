package datasci.backend.activations;

import java.util.Set;
import java.util.TreeMap;

/**
 * The enum ActE.
 */
public enum ActE {
    NONE("None"),
    IDENT("Identity"),
    LEAKY_RELU("Leaky RELU"),
    SIGMOID("Sigmoid"),
    SOFTMAX("Softmax"),
    TANH("Hyperbolic Tangent");

    // label may have spaces and may be more user-friendly than the enum value
    public final String label;

    /**
     * Create a new Act e.
     *
     * @param label string label
     */
    private ActE(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    // use TreeMap to sort by keys: key = string label, value = enum value
    private static final TreeMap<String, ActE> LABEL_MAP = new TreeMap<>();

    static {
        for (ActE e : values()) {
            LABEL_MAP.put(e.label, e);
        }
    }

    // ... fields, constructor, methods

    /**
     * Get enum value for given string value
     *
     * @param label the label
     * @return the enum value
     */
    public static ActE valueOfLabel(String label) {
        return LABEL_MAP.get(label);
    }

    /**
     * Gets sorted string labels.
     *
     * @return enum string labels
     */
    public static Set<String> getLabels() {
        return LABEL_MAP.keySet();
    }
}
