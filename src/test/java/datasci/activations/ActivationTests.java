package datasci.activations;

import datasci.backend.activations.ActE;
import datasci.backend.activations.LeakyReluActivation;
import datasci.backend.activations.SigmoidActivation;
import datasci.backend.activations.SoftmaxActivation;
import datasci.backend.activations.TanhActivation;
import datasci.backend.model.Matrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActivationTests {

    @Test
    void softmax() {

        SoftmaxActivation softmax = new SoftmaxActivation();
        double[] a = {4.0, 2.0, -7.0, 3.0};
        Matrix mZ = new Matrix(4, 1, a);
        //
        Matrix mY = softmax.trainingFn(mZ);
        // softmax derivative not used
        // Matrix dYdZ = softmax.derivative();
        //
        double max = 4.0;
        double[] yex = new double[4];
        yex[0] = Math.exp(4.0 - max);
        yex[1] = Math.exp(2.0 - max);
        yex[2] = Math.exp(-7.0 - max);
        yex[3] = Math.exp(3.0 - max);
        double total = yex[0] + yex[1] + yex[2] + yex[3];

        double[] exY = {yex[0] / total, yex[1] / total, yex[2] / total, yex[3] / total};

        Assertions.assertArrayEquals(exY, mY.a, "Activation softmax failed");
    }

    @Test
    void sigmoid() {

        SigmoidActivation sigmoid = new SigmoidActivation();
        double[] a = {4.0, 2.0, -7.0, 3.0};
        Matrix mZ = new Matrix(4, 1, a);
        //
        Matrix mY = sigmoid.trainingFn(mZ);
        Matrix dYdZ = sigmoid.derivative();
        //
        double[] yex = new double[4];
        yex[0] = 1 / (1 + Math.exp(-4.0));
        yex[1] = 1 / (1 + Math.exp(-2.0));
        yex[2] = 1 / (1 + Math.exp(7.0));
        yex[3] = 1 / (1 + Math.exp(-3.0));

        double[] exY = {yex[0], yex[1], yex[2], yex[3]};

        Assertions.assertArrayEquals(exY, mY.a, "Activation sigmoid failed");

        double[] exdYdZ = {yex[0] * (1 - yex[0]), yex[1] * (1 - yex[1]), yex[2] * (1 - yex[2]), yex[3] * (1 - yex[3])};

        Assertions.assertArrayEquals(exdYdZ, dYdZ.a, "Activation tanhAct dYdZ failed");

    }

    @Test
    void tanhAct() {

        TanhActivation tanhAct = new TanhActivation();
        double[] a = {4.0, 2.0, -7.0, 3.0};
        Matrix mZ = new Matrix(4, 1, a);
        //
        Matrix mY = tanhAct.trainingFn(mZ);
        Matrix dYdZ = tanhAct.derivative();
        //
        double[] yex = new double[4];
        yex[0] = Math.tanh(4.0);
        yex[1] = Math.tanh(2.0);
        yex[2] = Math.tanh(-7.0);
        yex[3] = Math.tanh(3.0);

        double[] exY = {yex[0], yex[1], yex[2], yex[3]};

        Assertions.assertArrayEquals(exY, mY.a, "Activation tanhAct failed");

        double[] exdYdZ = {1 - yex[0] * yex[0], 1 - yex[1] * yex[1], 1 - yex[2] * yex[2], 1 - yex[3] * yex[3]};

        Assertions.assertArrayEquals(exdYdZ, dYdZ.a, "Activation tanhAct dYdZ failed");

    }

    @Test
    void leakyRelu() {

        LeakyReluActivation leakyRelu = new LeakyReluActivation();
        double[] a = {4.0, 2.0, -7.0, 3.0};
        Matrix mZ = new Matrix(4, 1, a);
        //
        Matrix mY = leakyRelu.trainingFn(mZ);
        Matrix dYdZ = leakyRelu.derivative();
        //
        double[] yex = new double[4];
        yex[0] = 4.0;
        yex[1] = 2.0;
        yex[2] = 0.1 * (-7.0);
        yex[3] = 3.0;

        double[] exY = {yex[0], yex[1], yex[2], yex[3]};

        Assertions.assertArrayEquals(exY, mY.a, "Activation leakyRelu failed");

        double[] exdYdZ = {1.0, 1.0, 0.1, 1.0};

        Assertions.assertArrayEquals(exdYdZ, dYdZ.a, "Activation leakyRelu failed");

    }

}
