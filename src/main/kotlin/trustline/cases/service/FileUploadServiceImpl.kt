package trustline.cases.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import trustline.cases.model.FileUploadsModel
import trustline.cases.repository.FileUploadRepository
import trustline.config.exception.BadRequestException
import java.util.*

@Service
class FileUploadServiceImpl(
    private val cloudinary: Cloudinary,
    private val fileUploadRepository: FileUploadRepository
) : FileUploadService {

    private val log = KotlinLogging.logger {}

    override fun uploadImages(files: List<MultipartFile>): List<UUID> {
        return files.map { file ->
            validateImage(file)
            val uploadResult = cloudinary.uploader().upload(
                file.bytes,
                ObjectUtils.asMap(
                    "folder", "trustline/evidence",
                    "resource_type", "image"
                )
            )
            val url = uploadResult["secure_url"] as String
            log.info { "Uploaded image to Cloudinary: $url" }

            val fileUpload = fileUploadRepository.save(FileUploadsModel(uploadUrl = url))
            fileUpload.id!!
        }
    }

    private fun validateImage(file: MultipartFile) {
        val allowedTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp")
        if (file.contentType !in allowedTypes) {
            throw BadRequestException("Invalid file type '${file.contentType}'. Allowed: JPEG, PNG, GIF, WEBP")
        }
        val maxSize = 5 * 1024 * 1024 // 5MB
        if (file.size > maxSize) {
            throw BadRequestException("File size exceeds 5MB limit")
        }
    }
}
