package com.jiwoolee.searchcosmeticchallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jiwoolee.searchcosmeticchallenge.data.SearchedItem
import com.jiwoolee.searchcosmeticchallenge.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_detail)

        setData()
    }

    private fun setData(){
        val item: SearchedItem = intent.extras!!.get("SearchedItem") as SearchedItem
        binding.apply {
            detailItem = item
        }

        binding.ibCancel.setOnClickListener { finish() }
    }
}