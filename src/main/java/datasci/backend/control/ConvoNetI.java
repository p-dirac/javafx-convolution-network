package datasci.backend.control;

import datasci.backend.model.EvaluationR;
import datasci.backend.model.NetConfig;
import datasci.backend.model.NetResult;
import datasci.backend.model.FitParams;

public interface ConvoNetI {

    public void configureNet(NetConfig config);

    public boolean prepAll();

    public int getTotalSamples();

    public int getBatchSize();

    public void fitBatch();

    public void setDoNow(boolean doNow);

    public EvaluationR evaluate();

    public String getStatus();

    public FitParams createFitParams();

    public void setFitParams(FitParams fitParams);

    public NetResult getNetResult();
}
