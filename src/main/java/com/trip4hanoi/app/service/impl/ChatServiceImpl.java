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
        boolean isNewRoom = false;

        if(request.getRoomId() != null){
            room = chatRoomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        }
        else {
            // kiem tra xem user da co room active/ pending chua
            room = chatRoomRepository.findByUserIdAndStatusNot(userId, ChatRoomsStatus.CLOSED)
                    .orElseGet(() -> {
                       ChatRoom newRoom = ChatRoom.builder()
                               .user(sender)
                               .status(ChatRoomsStatus.PENDING)
                               .build();
                       return chatRoomRepository.save(newRoom);
                    });
            isNewRoom = (room.getCreatedAt() == null || room.getMessages() == null); // logic check new
        }

        // Luu tin nhan cua user
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(request.getContent())
                .type(ChatMessageType.USER)
                .build();

        message = chatMessageRepository.save(message);

        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(message);

        // Ban tin nhan qua ws

        messagingTemplate.convertAndSend("/topic/chat/"+ room.getId(),response);

        // neu la phong moi , gui Auto-reply va thong bao cho staff
        if(room.getStatus() == ChatRoomsStatus.PENDING){
            // gui auto reply
            sendSystemMessage(room,"Chào bạn đến với Trip4 Hà Nội. Vui lòng đợi trong giây lát để kết nối với nhân viên.");

            //thong bao cho tat ca staff co phong moi dag cho
            ChatRoomResponse roomResponse = chatRoomMapper.tcChatRoomResponse(room);
            messagingTemplate.convertAndSend("/topic/chat/rooms",roomResponse);
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
            return  chatRoomRepository.findByStatusOrderByCreatedAtDesc(roomsStatus)
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

        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(chatMessageMapper::toChatMessageResponse)
                .collect(Collectors.toList());
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
