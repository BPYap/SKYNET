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

    @Query("SELECT * FROM hotspot WHERE addressPostalCode LIKE :postcode")
    public Hotspot findByPostcode(int postcode);

    @Query("SELECT * FROM hotspot WHERE `index` LIKE :index")
    public Hotspot findByIndex(int index);

    @Insert
    public void insertAll(Hotspot[] hotspots);

    @Query("Delete FROM hotspot")
    public void dropTable();
}
