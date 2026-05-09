package com.trip4hanoi.app.repository;

import com.trip4hanoi.app.dto.res.InternalNoteResponse;
import com.trip4hanoi.app.entity.InternalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalNoteRepository extends JpaRepository<InternalNote,Long> {
    // Lấy danh sách ghi chú nội bộ của một phòng chat, sắp xếp mới nhất lên đầu
    List<InternalNote> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}
