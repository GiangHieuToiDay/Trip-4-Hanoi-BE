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
    PLAN_NOT_FOUND(1020, "User not have plan"),
    TYPE_NOT_FOUND(1021, "Type not found"),
    YOU_NOT_HAVE_AUTHOR_TO_DO_ACTION(1022, "You canot do action"),
    TITLE_EXIST (1023, "Title is exist"),
    BUDGET_NOT_ENOUGHT (1024, "Budget is not enought"),
    PLACE_IS_EXIST (1025, "Place is exist"),
    PLACE_NOT_FOUND(1026,"Place not found"),
    DAYS_INVALID(1027, "Days invalid"),
    INVALID_ORDER_INDEX(1028, "Invalid order index"),
    PLAN_PLACE_NOT_FOUND(1029, "Plan place not found"),
    BUDGET_EXCEEDED(1024, "Budget exceeded"),
    PLACE_ALREADY_EXISTS(1024, "Place already exists"),
    UPLOAD_FAIL(1025, "Upload failed");

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
