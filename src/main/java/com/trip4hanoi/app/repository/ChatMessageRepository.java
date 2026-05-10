package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // lấy lịch sử chat
    List<ChatMessage>  findByRoomIdOrderByTimestampAsc(Long roomId);

    //Lấy tin nhắn cuối cùng để hiển thị ở danh sách ben ngoài
    Optional<ChatMessage> findFirstByRoomIdOrderByTimestampDesc(Long roomId);


    //=============================================================================================
    //DASHBOARD

    // Thống kê lượng chat theo khung giờ trong 7 ngày qua
    @Query(value = "SELECT HOUR(timestamp) as hr, COUNT(*) FROM chat_message " +
            "WHERE timestamp > NOW() - INTERVAL 7 DAY GROUP BY hr ORDER BY hr", nativeQuery = true)
    List<Object[]> getChatVolumeByHour();

    // Lấy nội dung chat mới nhất cho AI
    @Query(value = "SELECT content FROM chat_message WHERE content IS NOT NULL ORDER BY timestamp DESC LIMIT 100", nativeQuery = true)
    List<String> getRecentChatContents();
}
