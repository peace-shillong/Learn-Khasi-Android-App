package com.sngur.learnkhasi.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.sngur.learnkhasi.model.room.Editors;
import java.util.List;

@Dao
public interface EditorsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Editors editors);

    @Query("SELECT * from editors")
    List<Editors> getAll();
}
