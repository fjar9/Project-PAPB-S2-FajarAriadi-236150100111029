package com.erdatamedia.estimator.networking;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ResponseDashboard {
    public Boolean status = false;
    public String msg = "";
    public Integer param = 10;
    @SerializedName("data")
    @Expose
    public Dashboard datas = new Dashboard();

    public class Dashboard {
        public List<Race> races = new ArrayList<>();
    }

    public class Race {
        public String id_race;
        public String race_name;
        public String created_date;

        public Race() {
        }

        public Race(String id_race, String race_name, String created_date) {
            this.id_race = id_race;
            this.race_name = race_name;
            this.created_date = created_date;
        }
    }
}
