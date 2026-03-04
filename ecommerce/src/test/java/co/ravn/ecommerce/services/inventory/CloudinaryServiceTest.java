package co.ravn.ecommerce.services.inventory;

import co.ravn.ecommerce.dto.response.inventory.ImageUploadResponse;
import co.ravn.ecommerce.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("returns ImageUploadResponse for a valid JPEG file")
        void returnsResponseForJpeg() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getBytes()).thenReturn(new byte[]{});
            when(cloudinary.uploader()).thenReturn(uploader);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("secure_url", "https://cloudinary.com/img.jpg");
            resultMap.put("public_id", "pub123");
            when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(resultMap);

            ImageUploadResponse response = cloudinaryService.upload(file);

            assertThat(response.getUrl()).isEqualTo("https://cloudinary.com/img.jpg");
            assertThat(response.getPublic_id()).isEqualTo("pub123");
        }

        @Test
        @DisplayName("returns ImageUploadResponse for a valid PNG file")
        void returnsResponseForPng() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("image/png");
            when(file.getBytes()).thenReturn(new byte[]{});
            when(cloudinary.uploader()).thenReturn(uploader);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("secure_url", "https://cloudinary.com/img.png");
            resultMap.put("public_id", "pub456");
            when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(resultMap);

            ImageUploadResponse response = cloudinaryService.upload(file);

            assertThat(response.getUrl()).isEqualTo("https://cloudinary.com/img.png");
            assertThat(response.getPublic_id()).isEqualTo("pub456");
        }

        @Test
        @DisplayName("returns ImageUploadResponse for a valid JPG file")
        void returnsResponseForJpgAlias() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("image/jpg");
            when(file.getBytes()).thenReturn(new byte[]{});
            when(cloudinary.uploader()).thenReturn(uploader);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("secure_url", "https://cloudinary.com/img.jpg");
            resultMap.put("public_id", "pub789");
            when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(resultMap);

            ImageUploadResponse response = cloudinaryService.upload(file);

            assertThat(response.getPublic_id()).isEqualTo("pub789");
        }

        @Test
        @DisplayName("throws BadRequestException when content type is null")
        void throwsForNullContentType() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn(null);

            assertThatThrownBy(() -> cloudinaryService.upload(file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Only JPEG and PNG");
        }

        @Test
        @DisplayName("throws BadRequestException for an unsupported content type")
        void throwsForUnsupportedContentType() {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("image/gif");

            assertThatThrownBy(() -> cloudinaryService.upload(file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Only JPEG and PNG");
        }

        @Test
        @DisplayName("throws BadRequestException when an IOException occurs during upload")
        void throwsOnIoException() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getBytes()).thenThrow(new IOException("disk error"));

            assertThatThrownBy(() -> cloudinaryService.upload(file))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Failed to upload");
        }
    }

    @Nested
    @DisplayName("uploadMany")
    class UploadMany {

        @Test
        @DisplayName("uploads each file and returns a response list")
        void uploadsEachFile() throws IOException {
            MultipartFile file1 = mock(MultipartFile.class);
            MultipartFile file2 = mock(MultipartFile.class);
            when(file1.getContentType()).thenReturn("image/jpeg");
            when(file2.getContentType()).thenReturn("image/png");
            when(file1.getBytes()).thenReturn(new byte[]{});
            when(file2.getBytes()).thenReturn(new byte[]{});
            when(cloudinary.uploader()).thenReturn(uploader);
            Map<String, Object> result1 = new HashMap<>();
            result1.put("secure_url", "https://cloudinary.com/1.jpg");
            result1.put("public_id", "p1");
            Map<String, Object> result2 = new HashMap<>();
            result2.put("secure_url", "https://cloudinary.com/2.png");
            result2.put("public_id", "p2");
            when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(result1, result2);

            List<ImageUploadResponse> responses = cloudinaryService.uploadMany(List.of(file1, file2));

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getPublic_id()).isEqualTo("p1");
            assertThat(responses.get(1).getPublic_id()).isEqualTo("p2");
        }

        @Test
        @DisplayName("returns an empty list when no files are provided")
        void returnsEmptyListForNoFiles() {
            List<ImageUploadResponse> responses = cloudinaryService.uploadMany(List.of());

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("calls Cloudinary destroy with the correct public ID")
        void callsDestroyWithPublicId() throws IOException {
            when(cloudinary.uploader()).thenReturn(uploader);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", "ok");
            when(uploader.destroy(eq("pub123"), any(Map.class))).thenReturn(resultMap);

            cloudinaryService.delete("pub123");

            verify(uploader).destroy(eq("pub123"), any());
        }

        @Test
        @DisplayName("does not throw when Cloudinary returns a non-ok result")
        void doesNotThrowOnNonOkResult() throws IOException {
            when(cloudinary.uploader()).thenReturn(uploader);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("result", "not found");
            when(uploader.destroy(eq("pub123"), any(Map.class))).thenReturn(resultMap);

            cloudinaryService.delete("pub123");

            verify(uploader).destroy(eq("pub123"), any());
        }

        @Test
        @DisplayName("handles IOException from Cloudinary gracefully without throwing")
        void handlesIoExceptionWithoutThrowing() throws IOException {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(any(String.class), any(Map.class))).thenThrow(new IOException("network error"));

            cloudinaryService.delete("pub123");
        }
    }
}
