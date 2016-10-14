package com.di7ak.spaces.forum.api;

import java.util.List;

public class Topic {
	public String topicUser, lastUser, date, lastCommentDate, subject, text, id, avatarUrl, editDate, editUser;
	public boolean newTopic, locked, bookmarkAdded;
	public int commentsCount, attachCount, likes, dislikes, currentPage, lastPage;
	public List<Attach> attachList;
	public Voting voting;
}
