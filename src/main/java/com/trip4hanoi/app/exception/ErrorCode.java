package com.trip4hanoi.app.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // SYSTEM ERRORS
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(8888, "Invalid message key", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND(1006, "Permission not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(1007, "Role not found", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1024, "Role already exists", HttpStatus.CONFLICT),
    PERMISSION_EXISTED(1025, "Permission already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1020, "UNAUTHENTICATED", HttpStatus.UNAUTHORIZED),
    EMAIL_EXISTED(1003, "Email already exists", HttpStatus.CONFLICT),
    USER_EXISTED(1002, "User already exists", HttpStatus.CONFLICT),

    // USER ERRORS
    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS(1002, "Username already exists", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1003, "Invalid password", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1004, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(1005, "Forbidden", HttpStatus.FORBIDDEN),
    USER_LIST_EMPTY(1006, "User list is empty", HttpStatus.NOT_FOUND),
    NOT_FOUND_ROLE(1007, "Role not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS(1008, "Email already exists", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1009, "Invalid credentials", HttpStatus.UNAUTHORIZED),

    // TOKEN & AUTH ERRORS
    CANOT_CREATE_TOKEN(1010, "Cannot create JWT token", HttpStatus.INTERNAL_SERVER_ERROR),
    CANOT_SEND_EMAIL(1011, "Cannot send email", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_NOT_FOUND(1012, "Token not found", HttpStatus.NOT_FOUND),
    TOKEN_EXPIRED(1013, "Token expired", HttpStatus.UNAUTHORIZED),

    // POST ERRORS
    POST_IS_EMPTY(1014, "Post is empty", HttpStatus.NOT_FOUND),
    POST_NOT_FOUND(1015, "Post not found", HttpStatus.NOT_FOUND),

    // CATEGORY
    CATEGORY_NOT_FOUND(1016, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_IS_EXIST(1017, "Category name already exists", HttpStatus.BAD_REQUEST),

    // COMMENT
    COMMENT_NOT_FOUND(1018, "Comment not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_BY_USER(1019, "Comment not by user", HttpStatus.FORBIDDEN),

    // PLAN / ITINERARY (THÊM TỪ CODE CỦA M)
    PLAN_NOT_FOUND(1020, "User not have plan", HttpStatus.NOT_FOUND),
    TYPE_NOT_FOUND(1021, "Type not found", HttpStatus.NOT_FOUND),
    YOU_NOT_HAVE_AUTHOR_TO_DO_ACTION(1022, "You cannot do this action", HttpStatus.FORBIDDEN),
    TITLE_EXIST(1023, "Title already exists", HttpStatus.BAD_REQUEST),

    BUDGET_NOT_ENOUGH(1024, "Budget is not enough", HttpStatus.BAD_REQUEST),
    PLACE_IS_EXIST(1025, "Place already exists", HttpStatus.BAD_REQUEST),

    PLACE_NOT_FOUND(1026, "Place not found", HttpStatus.NOT_FOUND),
    DAYS_INVALID(1027, "Days invalid", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_INDEX(1028, "Invalid order index", HttpStatus.BAD_REQUEST),
    PLAN_PLACE_NOT_FOUND(1029, "Plan place not found", HttpStatus.NOT_FOUND),
    IMAGE_NOT_FOUND(1038, "Image  not found", HttpStatus.NOT_FOUND),

    BUDGET_EXCEEDED(1030, "Budget exceeded", HttpStatus.BAD_REQUEST),
    PLACE_ALREADY_EXISTS(1031, "Place already exists", HttpStatus.BAD_REQUEST),
    UPLOAD_FAIL(1032, "Upload failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // CHAT
    BOX_CHAT_NOT_FOUND(1033, "Box chat not found", HttpStatus.NOT_FOUND),
    NOT_FOUND_BOX_PARTICIPANT(1034, "Box participant not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_IN_BOX(1035, "User already in box", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND_IN_BOX(1036, "User not found in box", HttpStatus.NOT_FOUND),
    POST_ALREADY_SAVED(1037, "Post already saved", HttpStatus.BAD_REQUEST),
    POST_NOT_SAVED(1038, "Post not saved", HttpStatus.NOT_FOUND),
    EVENT_NOT_FOUND(1039, "Event not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(1040, "Notification not found", HttpStatus.NOT_FOUND);


    private final int code;
    private final String message;
    private final HttpStatus status;
}