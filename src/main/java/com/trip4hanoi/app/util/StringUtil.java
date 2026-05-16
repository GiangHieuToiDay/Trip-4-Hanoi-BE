package com.trip4hanoi.app.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtil {
    
    /**
     * Loại bỏ dấu tiếng Việt và chuyển về chữ thường.
     * Ví dụ: "Hồ Hoàn Kiếm" -> "ho hoan kiem"
     */
    public static String removeAccents(String str) {
        if (str == null) return "";
        try {
            String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            String result = pattern.matcher(temp).replaceAll("")
                    .replace('đ', 'd')
                    .replace('Đ', 'D');
            return result.toLowerCase().trim();
        } catch (Exception e) {
            return str.toLowerCase().trim();
        }
    }

    /**
     * Tạo vector tìm kiếm từ các trường thông tin.
     */
    public static String generateSearchVector(String name, String address, String categoryName) {
        StringBuilder sb = new StringBuilder();
        if (name != null) sb.append(removeAccents(name)).append(" ");
        if (address != null) sb.append(removeAccents(address)).append(" ");
        if (categoryName != null) sb.append(removeAccents(categoryName));
        return sb.toString().trim();
    }
}
