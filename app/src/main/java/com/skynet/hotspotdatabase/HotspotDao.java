package com.skynet.hotspotdatabase;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

/**
 * Created by eddyl on 24/3/2018.
 */

@Dao
public interface HotspotDao {
    @Query("SELECT * FROM hotspot")
    public Hotspot[] getAll();

    @Query("SELECT * FROM hotspot WHERE ADDRESSPOSTALCODE LIKE :postcode")
    public Hotspot findByPostcode(int postcode);

    /*
    @Query("SELECT * FROM hotspot WHERE (Long < :longtitude AND Lat < :lattitude)")
    public Hotspot findNearbyHotspots(double longtitude, double lattitude);

    @Query("SELECT ADDRESSSTREETNAME FROM hotspot WHERE NAME = :Names")
    public String findAllNames(String[] Names);

    @Query("SELECT * FROM hotspot WHERE (Long = :longtitude AND Lat = :lattitude)")
    Hotspot findDuplicate(float longtitude, float lattitude);
    */

    @Insert
    public void insertAll(Hotspot[] hotspots);

    /*
    @Update
    public int update(Hotspot... hotspots); //int returns number of rows updated

    @Delete
    public void delete(Hotspot hotspot);
    */

    @Query("Delete FROM hotspot")
    public void dropTable();
}
