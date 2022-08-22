package datasci.backend.activations;

import datasci.backend.model.Matrix;

public interface ActivationI {
    public Matrix trainingFn(Matrix z);

    public Matrix testingFn(Matrix z);

    public Matrix derivative();
}
