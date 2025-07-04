package com.project.moyora.app.service;

import com.project.moyora.app.Dto.NoticeDto;
import com.project.moyora.app.Dto.NoticeRequest;
import com.project.moyora.app.domain.User;

import java.util.List;

public interface NoticeService {
    NoticeDto createNotice(Long boardId, NoticeRequest request, User user);
    NoticeDto getNoticeByBoard(Long boardId);
    void addComment(Long noticeId, String content, User user);
    NoticeDto updateNotice(Long noticeId, NoticeRequest noticeRequest, User user);
    void deleteNotice(Long noticeId, User user);
}

