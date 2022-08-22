package datasci.backend.layers;

public interface LayerI {
    public LayerE getLayerType();

    public Object trainForward(Object in);

    public Object testForward(Object in);

    public Object backProp(Object inLoss);
}
