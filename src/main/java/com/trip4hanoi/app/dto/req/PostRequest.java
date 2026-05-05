package com.trip4hanoi.app.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//public class PostRequest {
//    @NotBlank(message = "Title is required")
//    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
//    private String title;
//
//    @NotBlank(message = "Content is required")
//    @Size(min = 10, message = "Content must be at least 10 characters")
//    private String content;
//
//    @Size(max = 10, message = "Maximum 10 images allowed")
//    private List<
////            @NotBlank(message = "Image URL must not be blank")
////            @Pattern(
////                    regexp = "^(http|https)://.*$",
////                    message = "Image URL must be valid"
////            )
//                    String> imageUrls;
//
//    @Size(max = 20, message = "Maximum 20 places can be tagged")
//    private List<
//            @NotNull(message = "Place ID must not be null")
//                    Long> taggedPlaceIds;
//}
public class PostRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<Long> keepImageIds;

    @Size(max = 20)
    private List<Long> taggedPlaceIds;
}
