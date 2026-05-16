package trustline.appuser

import org.springframework.data.domain.Page

data class PagedResponse<T>(
    val content: List<T>,
    val count: Int,
    val total: Int,
    val last: Boolean
) {
    constructor(page: Page<T>) : this(
        page.content,
        page.numberOfElements,
        page.totalElements.toInt(),
        page.isLast
    )
}