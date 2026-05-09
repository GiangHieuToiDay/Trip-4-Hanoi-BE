package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.common.ChatRoomsStatus;
import com.trip4hanoi.app.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // tìm danh sách phòng theo trạng thái
    List<ChatRoom> findByStatusOrderByCreatedAtDesc(ChatRoomsStatus status);

    // phòng mà nhân viên phụ trách
    List<ChatRoom> findByStaffAndStatus(Long staffId, ChatRoomsStatus status);

    // KIỂM tra xem user đã có phòng chat nào đang mở ( active hoặc pending) chưa
    Optional<ChatRoom> findByUserIdAndStatusNot(Long userId, ChatRoomsStatus status);
}
