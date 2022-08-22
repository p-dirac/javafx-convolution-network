package datasci.model;

import datasci.backend.model.MathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MathUtilTests {

    private static final double DELTA = 1e-4;

    @Test
    void indexOfMax() {
        double[] x = {1.0, 3.0, 2.0, 1.0,
                6.0, -9.0, 1.0, 1.0,
                1.0, 3.0, 2.0, -3.0,
                5.0, 4.0, 1.0, 2.0};
        //
        int k = MathUtil.indexOfMax(x);

        int exK = 4;

        Assertions.assertEquals(exK, k, "MathUtil indexOfMax failed");
    }

    @Test
    void triangleFn() {
        int step = 100;
        int count = 0;
        //
        double y = MathUtil.triangleFn(step, count);
        double exY = 0;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil triangleFn failed");
        //
        count = 50;
        y = MathUtil.triangleFn(step, count);
        exY = 0.5;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil triangleFn failed");
        //
        count = 100;
        y = MathUtil.triangleFn(step, count);
        exY = 1;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil triangleFn failed");
        //
        count = 150;
        y = MathUtil.triangleFn(step, count);
        exY = 0.5;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil triangleFn failed");
        //
        count = 200;
        y = MathUtil.triangleFn(step, count);
        exY = 0;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil triangleFn failed");

    }
    @Test
    void decayTriangleFn() {
        int step = 200; //one cycle step size
        double decay = 0.1;  //decay per step
        double minY = 0.10;
        double maxY = 0.80;
        double minPeak = 2*minY;
        double diffY = maxY - minY;

        //
        int count = 0;
        double y = MathUtil.decayTriangleFn(minY, maxY, minPeak, decay, step, count);
        double exY = minY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayTriangleFn failed");
        //
        count = 50;
        y = MathUtil.decayTriangleFn(minY, maxY, minPeak, decay, step, count);
        exY = minY + 0.5*diffY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayTriangleFn failed");
        //
        count = 100;
        y = MathUtil.decayTriangleFn(minY, maxY, minPeak,  decay, step, count);
        exY = 1*maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayTriangleFn failed");
        //
        count = 150;
        y = MathUtil.decayTriangleFn(minY, maxY, minPeak, decay, step, count);
        exY = minY + 0.5*diffY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayTriangleFn failed");
        //
        count = 200;
        y = MathUtil.decayTriangleFn(minY, maxY, minPeak,  decay, step, count);
        exY = minY + 0*maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayTriangleFn failed");

        //
        count = 300;
        y = MathUtil.decayTriangleFn(minY, maxY, minPeak,  decay, step, count);
        exY = minY + (maxY - decay*1 - minY);
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayTriangleFn failed");

    }
    @Test
    void decayStepFn(){
        int step = 200; //one cycle step size
        double decay = 0.1;  //decay per step
        double minY = 0.10;
        double maxY = 0.80;

        //
        int count = 0;
        double y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        double exY = maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 50;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 100;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 199;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 200;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY - decay*1;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 201;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY - decay*1;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 301;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY - decay*1;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");
        //
        count = 601;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY - decay*3;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");

        //
        count = 1001;
        y = MathUtil.decayStepFn(minY, maxY, decay, step, count);
        exY = maxY - decay*5;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil decayStepFn failed");

    }
    @Test
    void dampedSineFn() {
        int step = 100;
        int count = 0;
        double decay = 0.02/1000.0;

        //
        double maxY = 1.0;
        double y = MathUtil.dampedSineFn(decay, step, count);
        double exY = 0*maxY;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil dampedSineFn failed");
        //
        count = 50;
        maxY = 0.99900;
        y = MathUtil.dampedSineFn(decay, step, count);
        exY = maxY*0.70711;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil dampedSineFn failed");
        //
        count = 100;
        maxY = 0.99800;
        y = MathUtil.dampedSineFn(decay, step, count);
        exY = maxY*1;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil dampedSineFn failed");
        //
        count = 150;
        maxY = 0.99700;
        y = MathUtil.dampedSineFn(decay, step, count);
        exY = maxY*0.70711;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil dampedSineFn failed");
        //
        count = 200;
        maxY = 0.99601;
        y = MathUtil.dampedSineFn(decay, step, count);
        exY = maxY*0;
        Assertions.assertEquals(exY, y, DELTA,"MathUtil dampedSineFn failed");

    }

}
