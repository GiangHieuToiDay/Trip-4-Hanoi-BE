package com.trip4hanoi.app.dto.res;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.trip4hanoi.app.common.ChatRoomsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomResponse {
    private Long id;
    private  Long userId;
    private String userName;
    private Long staffId;
    private String staffName;
    private ChatRoomsStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
    private ChatMessageResponse lastMessage;

}
