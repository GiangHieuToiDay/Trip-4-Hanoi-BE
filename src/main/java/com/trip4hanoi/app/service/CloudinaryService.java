package com.trip4hanoi.app.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map uploadFile(MultipartFile file) throws Exception {
        return cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.emptyMap());
    }

    public void deleteFile(String publicId) {

        System.out.println("→ Calling Cloudinary delete with publicId: " + publicId);

        try {
            Map result = cloudinary.uploader()
                    .destroy(publicId, ObjectUtils.emptyMap());

            String status = result.get("result").toString();

            System.out.println("Cloudinary response: " + status);

            if ("ok".equals(status)) {
                System.out.println("Delete success: " + publicId);
            } else if ("not found".equals(status)) {
                System.out.println("Image not found (already deleted?): " + publicId);
            } else {
                System.out.println("Delete failed with status: " + status);
            }

        } catch (Exception e) {
            System.out.println("Exception when deleting: " + publicId);
            e.printStackTrace();
        }
    }
}