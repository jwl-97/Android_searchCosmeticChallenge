package com.jiwoolee.searchcosmeticchallenge

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jiwoolee.searchcosmeticchallenge.data.ProductData
import com.jiwoolee.searchcosmeticchallenge.data.SearchedItem
import com.jiwoolee.searchcosmeticchallenge.data.SearchedJson
import com.jiwoolee.searchcosmeticchallenge.databinding.ActivityMainBinding
import com.jiwoolee.searchcosmeticchallenge.retrofit.IMyService
import com.jiwoolee.searchcosmeticchallenge.retrofit.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import retrofit2.Retrofit
import java.io.Serializable
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnItemClickListener, View.OnClickListener {
    private var disposable: CompositeDisposable? = CompositeDisposable() //retrofit 통신
    private var retrofitClient = RetrofitClient.getInstance()
    private var iMyService: IMyService? = (retrofitClient as Retrofit).create(IMyService::class.java)
    private lateinit var binding: ActivityMainBinding
    private var adapter: RecyclerviewAdapter? = null

    private var start: Int = 0
    private var count: Int = 10
    private var keyword: String = ""
    private var subCategory: Array<Int>? = null

    private var isOn: Boolean = false
    private var preCategoryId : Int? = null
    private lateinit var preTextview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        initToolbar()
        initRecyclerview()
        setSpinner()

        start = 0
        getProductItem(makeSearchContentToJson()) //아이템 불러오기

        tv_categrory_1.setOnClickListener(this)
        tv_categrory_2.setOnClickListener(this)
        tv_categrory_3.setOnClickListener(this)
        tv_categrory_4.setOnClickListener(this)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun initRecyclerview() {
        adapter = RecyclerviewAdapter(this)
        binding.rvList.layoutManager = GridLayoutManager(this, 2) // 한 줄에 2개씩
        binding.rvList.adapter = adapter
        setRecyclerviewListener(binding.rvList)
    }

    //검색 툴바
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        binding.svToolbar.setIconifiedByDefault(false)
        binding.svToolbar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean { //검색 submit 시
                adapter!!.removeItem() //리사이클러뷰 아이템 비우고
                start = 0
                keyword = query
                getProductItem(makeSearchContentToJson()) //아이템 불러오기
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })

        val closeButton = binding.svToolbar.findViewById<ImageView>(R.id.search_close_btn) //searchview의 x 버튼
        closeButton.setOnClickListener {
            adapter!!.removeItem() 
            start = 0
            keyword = "" //초기화
            getProductItem(makeSearchContentToJson()) //아이템 불러오기

            (binding.rvList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 20) //맨 위로 스크롤

            binding.svToolbar.setQuery("", false) //searchview의 text칸 비우기
            binding.svToolbar.clearFocus()
        }
        return super.onCreateOptionsMenu(menu)
    }

    //카테고리 선택 스피너
    private fun setSpinner() {
        val myAdapter = ArrayAdapter(this, R.layout.spinner_item, resources.getStringArray(R.array.spinner_array))
        binding.spProductCount.adapter = myAdapter
        binding.spProductCount.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                adapter!!.removeItem()
                start = 0 //처음부터
                count = (position + 1) * 10 //선택한 개수만큼
                getProductItem(makeSearchContentToJson()) //아이템 불러오기
                binding.spProductCount.setSelection(position)
                (binding.rvList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 20) //맨 위로 스크롤
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setRecyclerviewListener(recyclerView: RecyclerView) {
        var isLastItem: Boolean = false

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager: LinearLayoutManager? = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLastItem) {
                    val totalItemCount: Int = linearLayoutManager!!.itemCount
                    val lastVisible: Int = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    isLastItem = (totalItemCount > 0) && (lastVisible >= totalItemCount - 1) // 마지막 아이템인지 판단
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, scrollState: Int) {
                super.onScrollStateChanged(recyclerView, scrollState)
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isLastItem) { //스크롤이 멈춰있고 마지막 아이템일 때
                    start += count
                    getProductItem(makeSearchContentToJson())//count만큼 다음 아이템 로드해 오기

                    isLastItem = false
                }
            }
        })
    }

    private fun makeSearchContentToJson(): ProductData {
        val queryContent = ProductData(start, count, keyword, subCategory)
        val gson = Gson()
        val jsonString: String = gson.toJson(queryContent, ProductData::class.java)
        return gson.fromJson(jsonString, ProductData::class.java)
    }

    //아이템 로드하기
    private fun getProductItem(data: ProductData) {
        disposable!!.add(iMyService!!.searchProducts(data)
            .subscribeOn(Schedulers.single()) //Api 호출은 한 번이므로 single
            .observeOn(AndroidSchedulers.mainThread())
            .retry(3)
            .subscribe ({ response ->
                val gson = Gson()
                val searchResult = gson.fromJson(response, SearchedJson::class.java) //JsonObject -> SearchedJson으로 파싱

                if (searchResult.total == 0) { //검색 결과가 없을 때
                    Toast.makeText(this, "검색된 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                }

                binding.apply { mainActivityItem = searchResult } //총 개수 넘겨주기
                parsingProductList(searchResult)
            }, {
                e -> Toast.makeText(this, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            })
        )
    }

    private fun parsingProductList(list: SearchedJson) = try {
        for (i in 0 until list.items.size) { //SearchedJson -> SearchedItem으로 파싱
            val items = list.items[i]
            val data = SearchedItem(items.id, items.brand, items.name, items.image, items.sub_category_ids, items.sub_categories)
            adapter!!.addItem(data) //recyclerview에 add
        }
        adapter!!.notifyDataSetChanged()
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    override fun onClick(v: View?) { //상단 카테고리 선택(스킨/토너~마스크)
        var categoryId : Int? = null
        lateinit var textView: TextView

        when (v?.id) {
            R.id.tv_categrory_1 -> {
                categoryId = 0
                textView = binding.tvCategrory1
            }
            R.id.tv_categrory_2 -> {
                categoryId = 10
                textView = binding.tvCategrory2
            }
            R.id.tv_categrory_3 -> {
                categoryId = 20
                textView = binding.tvCategrory3
            }
            R.id.tv_categrory_4 -> {
                categoryId = 25
                textView = binding.tvCategrory4
            }
        }

        if (!isOn) { //선택되어있는 항목이 없을 때
            setForCategoryItemAndRecyclerview(categoryId, textView, R.drawable.categorytext_circle)
            isOn = true
        } else {  //선택되어있는 항목이 있고,
            if (preCategoryId != categoryId || preTextview != textView) { //원래 선택된 항목과는 다른 항목을 클릭했을 때
                preTextview.setBackgroundResource(0);  //이전에 선택되어 있던 것은 해제

                setForCategoryItemAndRecyclerview(categoryId, textView, R.drawable.categorytext_circle)
                isOn = true
            } else { //원래 선택된 항목을 다시 클릭했을 때
                setForCategoryItemAndRecyclerview(null, textView, 0)
                isOn = false
            }
        }

        (binding.rvList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 20) //맨 위로 스크롤
    }

    private fun setForCategoryItemAndRecyclerview(id: Int?, textView : TextView, background: Int) {
        subCategory = when (id) { //카테고리 변경
            null -> null
            else -> arrayOf(id)
        }
        textView.setBackgroundResource(background);  //클릭효과(배경에 동그라미 추가)

        adapter!!.removeItem()
        start = 0 //처음부터
        getProductItem(makeSearchContentToJson()) //아이템 불러오기

        preCategoryId = id
        preTextview = textView
    }

    //recyclerview item 클릭시 ProductDetailActivity로 productData 옮겨주는 listener ==상세보기
    override fun onItemClicked(item: SearchedItem) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("SearchedItem", item as Serializable)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}