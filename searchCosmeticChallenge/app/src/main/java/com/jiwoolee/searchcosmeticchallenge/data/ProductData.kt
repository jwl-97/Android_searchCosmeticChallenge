package com.jiwoolee.searchcosmeticchallenge.data

data class ProductData(
    var start: Int,
    var count: Int,
    var keyword: String?,
    var sub_category_ids: Array<Int>?
)