package com.trip4hanoi.app.service;

import com.trip4hanoi.app.dto.req.ChatMessageRequest;
import com.trip4hanoi.app.dto.req.InternalNoteRequest;
import com.trip4hanoi.app.dto.res.ChatMessageResponse;
import com.trip4hanoi.app.dto.res.ChatRoomResponse;
import com.trip4hanoi.app.dto.res.InternalNoteResponse;

import java.util.List;

public interface ChatService {

    //Cho user: gui tin nhan dau tien or tiep theo
    ChatMessageResponse sendMessage(Long userId, ChatMessageRequest request);
    ChatMessageResponse sendMessageByEmail(String email, ChatMessageRequest request);

    // Cho staff: tiếp nhận phòng chat
    ChatRoomResponse claimRoom(Long staffId, Long roomId);
    ChatRoomResponse claimRoomByEmail(String email, Long roomId);

    // Cho admin: chuyển giao phòng chat cho staff
    ChatRoomResponse transferRoom(Long staffId, Long roomId);

    // Lấy danh sách phòng chat theo trạng thái
    List<ChatRoomResponse> getRoomByStatus(String status);

    // lấy lịch sử tin nhán của 1 phòng
    List<ChatMessageResponse> getChatHistory(Long roomId);

    // Lấy phòng chat hiện tại của user
    ChatRoomResponse getActiveRoomForUser(Long userId);
    ChatRoomResponse getActiveRoomForUserByEmail(String email);

    // lưu ghi chú nội bộ
    InternalNoteResponse addInternalNote(Long authorId,Long roomId, InternalNoteRequest request);
    InternalNoteResponse addInternalNoteByEmail(String email, Long roomId, InternalNoteRequest request);
}
