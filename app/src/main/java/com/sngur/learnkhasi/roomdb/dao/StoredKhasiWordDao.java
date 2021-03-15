package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface StoredKhasiWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoredKhasiWord word);

    @Query("SELECT * from khasi_word WHERE word LIKE :query ORDER BY word ASC")
    List<StoredKhasiWord> findWord(String query);

    @Query("SELECT * from khasi_word ORDER BY word ASC  limit 300")
    List<StoredKhasiWord> findAllWords();

    @Query("SELECT Count(wordId) from khasi_word")
    int getCount();
}
