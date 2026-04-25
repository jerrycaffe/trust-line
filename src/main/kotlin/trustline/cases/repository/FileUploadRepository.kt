package trustline.cases.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import trustline.cases.model.FileUploadsModel
import java.util.*

@Repository
interface FileUploadRepository : JpaRepository<FileUploadsModel, UUID>
