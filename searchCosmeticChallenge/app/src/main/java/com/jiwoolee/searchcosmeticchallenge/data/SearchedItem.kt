package com.jiwoolee.searchcosmeticchallenge.data

import java.io.Serializable

data class SearchedItem(
    var id: Int,
    var brand: String,
    var name: String,
    var image: String,
    var sub_category_ids: Array<Int>,
    var sub_categories : Array<String>
) : Serializable