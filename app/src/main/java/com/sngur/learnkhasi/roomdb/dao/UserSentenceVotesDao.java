package com.sngur.learnkhasi.roomdb.dao;

import com.sngur.learnkhasi.model.room.UserSentenceVotes;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserSentenceVotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSentenceVotes userSentenceVotes);

    @Query("SELECT * from user_sentence_votes")
    List<UserSentenceVotes> getUserVotes();

    @Query("SELECT vote from user_sentence_votes where sentenceId=:sentence")
    boolean getUserVoteBySentenceId(String sentence);
    //we will use this method to check if user has liked/unlike upvote/downvote a sentence -

    @Query("SELECT sentenceId from user_sentence_votes")
    List<String> getUserVotedSentenceId();

}
