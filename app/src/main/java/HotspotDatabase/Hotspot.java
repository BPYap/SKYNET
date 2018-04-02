package hotspotdatabase;

import android.arch.persistence.room.Entity;

/**
 * Created by eddyl on 24/3/2018.
 */

@Entity(primaryKeys = {"index"})
public class Hotspot {
    private int index;

    private double Lat;

    private double Long;

    private int ADDRESSPOSTALCODE;

    private String DESCRIPTION;

    private String NAME;

    private String ADDRESSSTREETNAME;

    private String OPERATOR_NAME;

    public Hotspot(int index, double Lat, double Long, int ADDRESSPOSTALCODE, String DESCRIPTION,
                   String NAME, String ADDRESSSTREETNAME, String OPERATOR_NAME){
        this.index = index;
        this.Lat = Lat;
        this.Long = Long;
        this.ADDRESSPOSTALCODE = ADDRESSPOSTALCODE;
        this.DESCRIPTION = DESCRIPTION;
        this.NAME = NAME;
        this.ADDRESSSTREETNAME = ADDRESSSTREETNAME;
        this.OPERATOR_NAME = OPERATOR_NAME;
    }

    public int getIndex() {
        return index;
    }

    public double getLat(){
        return Lat;
    }

    public double getLong(){
        return Long;
    }

    public int getADDRESSPOSTALCODE(){
        return ADDRESSPOSTALCODE;
    }

    public String getDESCRIPTION(){
        return DESCRIPTION;
    }

    public String getNAME(){
        return NAME;
    }

    public String getADDRESSSTREETNAME(){
        return  ADDRESSSTREETNAME;
    }

    public String getOPERATOR_NAME(){
        return OPERATOR_NAME;
    }

    public void setI(int index) {
        this.index = index;
    }

    public void setLat(double lat){
        this.Lat = lat;
    }

    public void setLong(double Long){
        this.Long = Long;
    }

    public void setADDRESSPOSTALCODE(int addresspostalcode){
        this.ADDRESSPOSTALCODE = addresspostalcode;
    }

    public void setADDRESSSTREETNAME(String ADDRESSSTREETNAME) {
        this.ADDRESSSTREETNAME = ADDRESSSTREETNAME;
    }

    public void setDESCRIPTION(String DESCRIPTION) {
        this.DESCRIPTION = DESCRIPTION;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public void setOPERATOR_NAME(String OPERATOR_NAME) {
        this.OPERATOR_NAME = OPERATOR_NAME;
    }
    
}
