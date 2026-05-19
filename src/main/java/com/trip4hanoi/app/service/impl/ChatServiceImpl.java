package com.trip4hanoi.app.service.impl;

import com.trip4hanoi.app.common.ChatMessageType;
import com.trip4hanoi.app.common.ChatRoomsStatus;
import com.trip4hanoi.app.dto.req.ChatMessageRequest;
import com.trip4hanoi.app.dto.req.InternalNoteRequest;
import com.trip4hanoi.app.dto.res.ChatMessageResponse;
import com.trip4hanoi.app.dto.res.ChatRoomResponse;
import com.trip4hanoi.app.dto.res.InternalNoteResponse;
import com.trip4hanoi.app.entity.ChatMessage;
import com.trip4hanoi.app.entity.ChatRoom;
import com.trip4hanoi.app.entity.InternalNote;
import com.trip4hanoi.app.entity.User;
import com.trip4hanoi.app.exception.AppException;
import com.trip4hanoi.app.exception.ErrorCode;
import com.trip4hanoi.app.mapper.ChatMessageMapper;
import com.trip4hanoi.app.mapper.ChatRoomMapper;
import com.trip4hanoi.app.mapper.InternalNoteMapper;
import com.trip4hanoi.app.repository.ChatMessageRepository;
import com.trip4hanoi.app.repository.ChatRoomRepository;
import com.trip4hanoi.app.repository.InternalNoteRepository;
import com.trip4hanoi.app.repository.UserRepository;
import com.trip4hanoi.app.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "CHAT-SERVICE")
public class ChatServiceImpl implements ChatService {


    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final InternalNoteRepository  internalNoteRepository;
    private final UserRepository userRepository;

    private final ChatRoomMapper chatRoomMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final InternalNoteMapper internalNoteMapper;

    private  final SimpMessagingTemplate messagingTemplate;



    @Override
    @Transactional
    public ChatMessageResponse sendMessageByEmail(String email, ChatMessageRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return sendMessage(user.getId(), request);
    }

    @Override
    @Transactional
    public ChatRoomResponse claimRoomByEmail(String email, Long roomId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return claimRoom(user.getId(), roomId);
    }

    @Override
    @Transactional
    public InternalNoteResponse addInternalNoteByEmail(String email, Long roomId, InternalNoteRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return addInternalNote(user.getId(), roomId, request);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, ChatMessageRequest request) {
        User sender = userRepository.findById(userId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        // tim hoac tao chatRoom
        ChatRoom  room;

        if(request.getRoomId() != null){
            room = chatRoomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

            // KIỂM TRA QUYỀN SỞ HỮU/TRUY CẬP PHÒNG CHAT
            boolean isOwner = room.getUser().getId().equals(userId);
            boolean isAssignedStaff = room.getStaff() != null && room.getStaff().getId().equals(userId);
            boolean isAdmin = sender.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));

            if (!isOwner && !isAssignedStaff && !isAdmin) {
                log.error("[CHAT-SECURITY] User {} attempted to send message to room {} owned by {}", 
                    userId, room.getId(), room.getUser().getId());
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        else {
            // kiem tra xem user da co room active/ pending chua
            room = chatRoomRepository.findByUserIdAndStatusNot(userId, ChatRoomsStatus.CLOSED)
                    .orElseGet(() -> {
                       ChatRoom newRoom = ChatRoom.builder()
                               .user(sender)
                               .status(ChatRoomsStatus.PENDING)
                               .unreadCount(0)
                               .build();
                       return chatRoomRepository.save(newRoom);
                    });
        }

        // Luu tin nhan cua user
        ChatMessageType messageType = ChatMessageType.USER;
        if (sender.getRoles().stream().anyMatch(r -> r.getName().equals("STAFF") || r.getName().equals("ADMIN"))) {
            messageType = ChatMessageType.STAFF;
        }

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(request.getContent())
                .type(messageType)
                .build();

        message = chatMessageRepository.save(message);

        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(message);

        // Cập nhật room info (unreadCount, updatedAt)
        if (messageType == ChatMessageType.USER) {
            int currentUnread = (room.getUnreadCount() != null) ? room.getUnreadCount() : 0;
            room.setUnreadCount(currentUnread + 1);
        }
        room.setUpdatedAt(LocalDateTime.now());
        room = chatRoomRepository.save(room); // Lưu và gán lại room đã update

        // Ban tin nhan qua ws vào room topic
        messagingTemplate.convertAndSend("/topic/chat/"+ room.getId(),response);

        // Bắn update room cho staff dashboard (để hiện badge unread và reorder)
        ChatRoomResponse roomUpdate = chatRoomMapper.tcChatRoomResponse(room);
        roomUpdate.setLastMessage(response);
        roomUpdate.setUnreadCount(room.getUnreadCount() != null ? room.getUnreadCount() : 0);
        messagingTemplate.convertAndSend("/topic/chat/rooms", roomUpdate);

        // neu la phong moi (chua co tin nhan nao truoc do hoac moi tao), gui Auto-reply va thong bao cho staff
        // Kiem tra tin nhan dau tien cua room la USER gui
        if(room.getStatus() == ChatRoomsStatus.PENDING && messageType == ChatMessageType.USER && chatMessageRepository.countByRoomId(room.getId()) <= 1){
            // gui auto reply
            sendSystemMessage(room,"Chào bạn đến với Trip4 Hà Nội. Vui lòng đợi trong giây lát để kết nối với nhân viên.");
        }

        return response;
    }

    @Override
    @Transactional
    public ChatRoomResponse claimRoom(Long staffId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(()-> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if(room.getStaff() != null){
            throw new AppException(ErrorCode.ROOM_ALREADY_ASSIGNED); // trang 2 staff cung nhan
        }

        User staff = userRepository.findById(staffId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        room.setStaff(staff);
        room.setStatus(ChatRoomsStatus.ACTIVE);
        chatRoomRepository.save(room);

        // THÔNG báo hệ thống trong room
        sendSystemMessage(room,"Nhân viên " +staff.getUsername() + " đã tham gia hỗ trợ.");

        ChatRoomResponse response = chatRoomMapper.tcChatRoomResponse(room);

        // ban tin cap nhat cho moi nguoi
        messagingTemplate.convertAndSend("/topic/chat/" + roomId,response);
        messagingTemplate.convertAndSend("/topic/staff/rooms/",response); // để các staff khác ẩn  room này khỏi danh sách chờ


        return response;
    }

    @Override
    @Transactional
    public ChatRoomResponse transferRoom(Long staffId, Long roomId) {
        // tim phong chat
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        //Tìm nhân viên mới
        User newStaff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //Lưu vết nhân viên cũ (để đưa vào tin nhắn hệ thống nếu cần)
        String oldStaffName = (room.getStaff() != null) ? room.getStaff().getUsername() : "không có ai";


        //Cập nhật nhân viên mới
        room.setStaff(newStaff);
        room.setStatus(ChatRoomsStatus.ACTIVE);
        chatRoomRepository.save(room);


        //Tạo tin nhắn hệ thống thông báo chuyển giao
        sendSystemMessage(room,"Cuộc hội thoại đã được chuyển từ" +oldStaffName+  " sang" + newStaff.getUsername());

        ChatRoomResponse response = chatRoomMapper.tcChatRoomResponse(room);

        //  Bắn tin cập nhật realtime cho tất cả các bên
        messagingTemplate.convertAndSend("/topic/chat/" + roomId,response);

        // Bắn cho Staff Dashboard để cập nhật danh sách đang xử lý
        messagingTemplate.convertAndSend("/topic/chat/rooms"  ,response);

        return response;
    }

    @Override
    @Transactional
    public List<ChatRoomResponse> getRoomByStatus(String status) {
        try {
            //Chuyển đổi String sang Enum
            ChatRoomsStatus  roomsStatus = ChatRoomsStatus.valueOf(status.toUpperCase());

            // Lấy danh sách từ DB và map sang DTO
            return  chatRoomRepository.findByStatusOrderByUpdatedAtDesc(roomsStatus)
                    .stream()
                    .map(room -> {
                        ChatRoomResponse res = chatRoomMapper.tcChatRoomResponse(room);

                        // lấy tin nhắn cuối cùng để hiển thị preview ở sidebar
                        chatMessageRepository.findFirstByRoomIdOrderByTimestampDesc(room.getId())
                                .ifPresent(msg -> res.setLastMessage(chatMessageMapper.toChatMessageResponse(msg)));
                        return res;

                    })
                    .collect(Collectors.toList());

        }
        catch (IllegalArgumentException e) {
            throw new  AppException(ErrorCode.INVALID_STATUS);
        }
    }

    @Override
    @Transactional
    public List<ChatMessageResponse> getChatHistory(Long roomId) {
        // RESET unreadCount khi xem lịch sử (dành cho staff)
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            int currentUnread = (room.getUnreadCount() != null) ? room.getUnreadCount() : 0;
            if (currentUnread > 0) {
                room.setUnreadCount(0);
                chatRoomRepository.save(room);
                
                // Bắn tin cập nhật cho staff list
                ChatRoomResponse update = chatRoomMapper.tcChatRoomResponse(room);
                chatMessageRepository.findFirstByRoomIdOrderByTimestampDesc(room.getId())
                        .ifPresent(msg -> update.setLastMessage(chatMessageMapper.toChatMessageResponse(msg)));
                messagingTemplate.convertAndSend("/topic/chat/rooms", update);
            }
        });

        // Lấy tin nhắn chat bình thường
        List<ChatMessageResponse> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(chatMessageMapper::toChatMessageResponse)
                .collect(Collectors.toList());

        // Lấy ghi chú nội bộ và gộp vào (chỉ staff mới gọi api này nên an toàn)
        List<InternalNote> notes = internalNoteRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        for (InternalNote note : notes) {
            ChatMessageResponse noteRes = ChatMessageResponse.builder()
                    .id(note.getId()) // Lưu ý: ID này có thể trùng với ID message nếu không cẩn thận, nhưng ở FE dùng id string phối hợp type sẽ ổn
                    .content("[GHI CHÚ] " + note.getContent())
                    .type(ChatMessageType.valueOf("STAFF")) // Tạm thời dùng STAFF type
                    .senderId(note.getAuthor().getId())
                    .senderName(note.getAuthor().getUsername())
                    .senderAvatar(note.getAuthor().getAvatar())
                    .timestamp(note.getCreatedAt())
                    .build();
            messages.add(noteRes);
        }

        // Sắp xếp lại theo thời gian
        messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

        return messages;
    }

    @Override
    @Transactional
    public ChatRoomResponse getActiveRoomForUser(Long userId) {
        return chatRoomRepository.findByUserIdAndStatusNot(userId, ChatRoomsStatus.CLOSED)
                .map(chatRoomMapper::tcChatRoomResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public ChatRoomResponse getActiveRoomForUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return getActiveRoomForUser(user.getId());
    }

    @Override
    @Transactional
    public InternalNoteResponse addInternalNote(Long authorId, Long roomId, InternalNoteRequest request) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(()-> new AppException(ErrorCode.ROOM_NOT_FOUND));

        User author = userRepository.findById(authorId).orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        InternalNote note = InternalNote.builder()
                .room(room)
                .author(author)
                .content(request.getContent())
                .build();

        note = internalNoteRepository.save(note);


        InternalNoteResponse response = internalNoteMapper.toInternalNoteResponse(note);

        // Bắn realtime cho Staff/Admin khác cùng xem
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/internal", response);
        return response;
    }



    private void sendSystemMessage(ChatRoom room, String content){
        ChatMessage systemMsg = ChatMessage.builder()
                .room(room)
                .content(content)
                .type(ChatMessageType.SYSTEM)
                .build();

        chatMessageRepository.save(systemMsg);

        ChatMessageResponse msgRes = chatMessageMapper.toChatMessageResponse(systemMsg);
        messagingTemplate.convertAndSend("/topic/chat/"+ room.getId(),msgRes);

    }
}
