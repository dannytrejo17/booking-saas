package com.gestorReservas.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gestorReservas.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file, Long businessId, String type) {
        validateImage(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "gestor-reservas/businesses/" + businessId + "/" + type,
                            "resource_type", "image"
                    )
            );
            Object url = result.get("secure_url");
            if (url == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "no se pudo subir la imagen");
            }
            return url.toString();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "error al subir la imagen");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "la imagen es obligatoria");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean allowedType = contentType != null && ALLOWED_TYPES.contains(contentType);
        boolean allowedExt = name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".png") || name.endsWith(".webp");
        if (!allowedType && !allowedExt) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "formato no permitido (jpg, png o webp)");
        }
    }
}
