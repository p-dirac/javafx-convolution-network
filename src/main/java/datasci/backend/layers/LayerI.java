package datasci.backend.layers;

public interface LayerI {

    // layerID fir debug purpose
    public void setLayerID(String layerID);
    public String getLayerID();

    public LayerE getLayerType();

    public Object trainForward(Object in);

    public Object testForward(Object in);

    public Object backProp(Object inLoss);
}
