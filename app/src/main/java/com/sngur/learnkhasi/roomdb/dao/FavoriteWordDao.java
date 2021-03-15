package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.room.FavoriteWord;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface FavoriteWordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteWord favoriteWord);

    @Query("SELECT COUNT(wordId) from favorite_word")
    Integer getNumberOfWords();

    @Query("SELECT * from favorite_word")
    List<FavoriteWord> getAllWords();
}
