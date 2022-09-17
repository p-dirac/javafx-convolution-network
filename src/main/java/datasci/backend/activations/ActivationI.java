package datasci.backend.activations;

import datasci.backend.model.Matrix;

public interface ActivationI {

    public String getActName();

    public Matrix trainingFn(Matrix z);

    public Matrix testingFn(Matrix z);

    public Matrix derivative();
}
