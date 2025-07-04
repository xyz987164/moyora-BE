package com.project.moyora.app.repository;

import com.project.moyora.app.domain.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);

}
