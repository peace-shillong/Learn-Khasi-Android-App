package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.room.StoredSentence;
import com.sngur.learnkhasi.model.room.UserSentenceReported;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
// A Single place for storing all sentences
@Dao
public interface StoredSentenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoredSentence sentence);

    @Query("SELECT COUNT(sentenceId) from sentences Where fromLang=:fromLang")
    Integer getNumberOfSentences(String fromLang);

    //How to call this query
    //search = "%Thank you%";
    //loadHamsters(search);
    @Query("SELECT * from sentences WHERE fromLang= :fromLang AND sentence LIKE :query")
    List<StoredSentence> findSentence(String query,String fromLang);

    //load All Sentences fromLang
    @Query("SELECT * from sentences WHERE fromLang= :fromLang")
    List<StoredSentence> findSentence(String fromLang);

    @Query("SELECT * from sentences WHERE fromLang= :fromLang and category=:category order by timestamp Limit 40")
    List<StoredSentence> find50Sentence(String fromLang,String category);
}
