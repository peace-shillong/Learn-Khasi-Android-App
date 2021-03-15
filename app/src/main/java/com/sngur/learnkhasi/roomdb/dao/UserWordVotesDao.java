package com.sngur.learnkhasi.roomdb.dao;


import com.sngur.learnkhasi.model.room.UserWordVoted;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserWordVotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserWordVoted userWordVoted);

    @Query("SELECT * from user_word_voted")
    List<UserWordVoted> getUserVotes();

    @Query("SELECT vote from user_word_voted where wordId=:wordId")
    boolean getUserVoteById(String wordId);
    //we will use this method to check if user has liked/unlike upvote/downvote a sentence -

    @Query("SELECT wordId from user_word_voted")
    List<String> getUserVotedWordId();
}
