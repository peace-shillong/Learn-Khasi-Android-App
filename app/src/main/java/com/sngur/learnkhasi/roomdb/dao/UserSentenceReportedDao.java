package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.room.UserSentenceReported;
import com.sngur.learnkhasi.model.room.UserSentenceVotes;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserSentenceReportedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSentenceReported userSentenceReported);

    @Query("SELECT * from user_sentence_reported")
    List<UserSentenceReported> getUserReport();

    @Query("SELECT sentenceId from user_sentence_reported")
    List<String> getUserReportedSentenceId();
}
