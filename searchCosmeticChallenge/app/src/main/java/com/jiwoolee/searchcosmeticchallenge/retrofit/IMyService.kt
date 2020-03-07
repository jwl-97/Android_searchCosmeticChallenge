package com.jiwoolee.searchcosmeticchallenge.retrofit

import com.google.gson.JsonObject
import com.jiwoolee.searchcosmeticchallenge.data.ProductData
import io.reactivex.Observable
import retrofit2.http.*

interface IMyService {
    @POST("cosmetics/")
    fun searchProducts(@Body data: ProductData): Observable<JsonObject>
}
