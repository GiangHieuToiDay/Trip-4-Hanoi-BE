package com.trip4hanoi.app.exception;

public enum ErrorCode {
    USER_NOT_FOUND(1001, "User not found"),
    USERNAME_ALREADY_EXISTS(1002, "Username already exists"),
    INVALID_PASSWORD(1003, "Invalid password"),
    UNAUTHORIZED(1004, "Unauthorized"),
    FORBIDDEN(1005, "Forbidden"),
    USER_LIST_EMPTY(1006,"List is Empty"),
    NOT_FOUND_ROLE(1007,"Role not found"),
    EMAIL_ALREADY_EXISTS(1008,"Email already exists"),
    INVALID_CREDENTIALS(1009, "Invalid credentials"),
    CANOT_CREATE_TOKEN(1010, "Can not create token jwt"),
    CANOT_SEND_EMAIL(1011, "Can not send email"),
    TOKEN_NOT_FOUND(1012, "Token not found"),
    TOKEN_EXPIRED(1013, "Token expired"),
    POST_IS_EMPTY(1014, "Post is empty"),
    POST_NOT_FOUND(1015, "Post not found"),
    CATEGORY_NOT_FOUND(1016, "Category not found"),
    CATEGORY_NAME_IS_EXIST(1017, "Category name already exists"),
    COMMENT_NOT_FOUND(1018, "Comment not found"),
    COMMENT_NOT_BY_USER(1019, "Comment not by user"),
    BOX_CHAT_NOT_FOUND(1020, "Box chat not found"),
    TYPE_NOT_FOUND(1021, "Type not found"),
    YOU_NOT_HAVE_AUTHOR_TO_DO_ACTION(1022, "You canot do action"),
    NOT_FOUND_BOX_PARTICIPANT (1023, "Box participant not found"),
    USER_ALREADY_IN_BOX (1024, "User is already in box"),
    USER_NOT_FOUND_IN_BOX (1025, "User not found in box");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
