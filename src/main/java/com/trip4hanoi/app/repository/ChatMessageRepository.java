package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // lấy lịch sử chat
    List<ChatMessage>  findByRoomIdOrderByTimestampAsc(Long roomId);

    //Lấy tin nhắn cuối cùng để hiển thị ở danh sách ben ngoài
    Optional<ChatMessage> findFirstByRoomIdOrderByTimestampDesc(Long roomId);
}
