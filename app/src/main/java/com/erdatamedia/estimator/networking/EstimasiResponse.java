package com.erdatamedia.estimator.networking;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EstimasiResponse {
    public Boolean status = false;
    public String msg = "";
    @SerializedName("data")
    @Expose
    public Float datas = null;
    public Integer param = 10;
}
