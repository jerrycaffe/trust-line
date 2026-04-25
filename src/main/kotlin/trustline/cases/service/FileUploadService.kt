package trustline.cases.service

import org.springframework.web.multipart.MultipartFile
import java.util.*

interface FileUploadService {
    fun uploadImages(files: List<MultipartFile>): List<UUID>
}
