package HotspotDatabase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import HotspotDatabase.Hotspot;
import HotspotDatabase.HotspotDao;

/**
 * Created by eddyl on 24/3/2018.
 */
@Database(entities = {Hotspot.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    private static final String DB_NAME = "hotspot.db";
    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context){
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static AppDatabase create(final Context context) {
        AppDatabase db = Room.databaseBuilder(context,
                AppDatabase.class, "hotspot.db").allowMainThreadQueries().build();
        return db;
    }

    public abstract HotspotDao hotspotDao();
}
