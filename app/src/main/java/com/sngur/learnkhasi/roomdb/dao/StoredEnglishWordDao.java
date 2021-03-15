package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.Word;
import com.sngur.learnkhasi.model.room.StoredEnglishWord;
import com.sngur.learnkhasi.model.room.StoredKhasiWord;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
@Dao
public interface StoredEnglishWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoredEnglishWord word);

    @Query("SELECT * from english_word WHERE word LIKE :query ORDER BY word ASC")
    List<StoredEnglishWord> findWord(String query);

    //load All Sentences fromLang
    @Query("SELECT * from english_word ORDER BY word ASC limit 300")
    List<StoredEnglishWord> findAllWords();

    @Query("SELECT Count(wordId) from english_word")
    int getCount();
}
