package co.ravn.ecommerce.Services.Inventory;

import co.ravn.ecommerce.DTO.Response.Inventory.ImageUploadResponse;
import co.ravn.ecommerce.Exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public ImageUploadResponse upload(MultipartFile file) {
        // Validate allowed file types (JPEG, PNG only)
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("image/jpeg")
                        || contentType.equalsIgnoreCase("image/jpg")
                        || contentType.equalsIgnoreCase("image/png"))) {
            throw new BadRequestException("Only JPEG and PNG images are allowed.");
        }

        try {
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "use_filename", true,
                            "quality", "auto",
                            "fetch_format", "auto",
                            "transformation", new Transformation()
                                    .crop("limit")
                                    .width(1600)
                                    .height(1600)
                    )
            );
            String secureUrl =  uploadResult.getOrDefault("secure_url", uploadResult.get("url")).toString();
            String publicId = uploadResult.get("public_id").toString();

            if (secureUrl == null || publicId == null) {
                throw new BadRequestException("Cloudinary upload did not return required fields.");
            }

            return new ImageUploadResponse(secureUrl, publicId);
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new BadRequestException("Failed to upload image to Cloudinary.");
        }
    }

    public List<ImageUploadResponse> uploadMany(List<MultipartFile> files) {
        List<ImageUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(upload(file));
        }
        return responses;
    }

    public void delete(String publicId) {
        try {
            @SuppressWarnings("rawtypes")
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            Object status = result.get("result");
            if (!"ok".equals(status)) {
                log.warn("Cloudinary did not delete asset with public_id {}. Result: {}", publicId, status);
            }
        } catch (IOException e) {
            log.error("Error deleting asset from Cloudinary with public_id {}", publicId, e);
        }
    }
}

