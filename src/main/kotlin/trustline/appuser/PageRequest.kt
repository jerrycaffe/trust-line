package trustline.appuser

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

data class PageRequest(
    private val offset: Int,
    private val limit: Int,
    private val sort: Sort
) : Pageable {
    override fun getPageNumber() = offset / limit

    override fun getPageSize() = limit

    override fun getOffset() = offset.toLong()

    override fun getSort() = sort

    override fun next() = PageRequest(getOffset().toInt() + pageSize, pageSize, getSort())

    private fun previous(): PageRequest {
        return if (hasPrevious()) PageRequest(getOffset().toInt() - pageSize, pageSize, getSort()) else this
    }

    override fun previousOrFirst(): Pageable = if (hasPrevious()) previous() else first()

    override fun first() = PageRequest(0, pageSize, getSort())

    override fun withPage(pageNumber: Int) = PageRequest(pageNumber, pageSize, getSort())

    override fun hasPrevious() = offset > limit
}