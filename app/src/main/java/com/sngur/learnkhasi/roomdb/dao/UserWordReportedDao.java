package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.room.UserSentenceReported;
import com.sngur.learnkhasi.model.room.UserWordReported;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserWordReportedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserWordReported userWordReported);

    @Query("SELECT * from userwordreported")
    List<UserWordReported> getUserReport();

    @Query("SELECT wordId from userwordreported")
    List<String> getUserReportedWordId();
}
