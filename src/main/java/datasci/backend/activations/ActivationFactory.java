package datasci.backend.activations;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivationFactory {
    private static final Logger LOG = Logger.getLogger(ActivationFactory.class.getName());

    public ActivationFactory() {
    }

    public static ActivationI getActivation(String actName) {
        ActivationI actFn = null;
        try {
            ActE actType = ActE.valueOfLabel(actName);
            switch (actType) {
                case IDENT:
                    actFn = new IdentityActivation();
                    break;
                case LEAKY_LOG:
                    actFn = new LeakyLogActivation();
                    break;
                case LEAKY_RELU:
                    actFn = new LeakyReluActivation();
                    break;

                case SIGMOID:
                    actFn = new SigmoidActivation();
                    break;
                case SOFTMAX:
                    actFn = new SoftmaxActivation();
                    break;
                case TANH:
                    actFn = new TanhActivation();
                    break;
                case TANH_SCALED:
                    actFn = new TanhScaledActivation();
                    break;
                case TINY_RELU:
                    actFn = new TinyReluActivation();
                    break;

                default:
                    actFn = new IdentityActivation();
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return actFn;
    }

}  // end class
