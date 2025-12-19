package com.partsystem.partvisitapp.feature.product.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductImageEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.network.modelDto.ProductWithPacking
import com.partsystem.partvisitapp.core.utils.extensions.gone
import com.partsystem.partvisitapp.core.utils.extensions.show
import com.partsystem.partvisitapp.databinding.ItemProductBinding
import com.partsystem.partvisitapp.feature.create_order.adapter.SpinnerAdapter
import com.partsystem.partvisitapp.feature.create_order.ui.FactorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
/*

class ProductListAdapter(
    private val factorViewModel: FactorViewModel,
    private val fromFactor: Boolean = false,
    private val factorId: Int,
    private val onProductChanged: (FactorDetailEntity) -> Unit,
    private val currentQuantities: Map<Int, Int> = emptyMap(),
    private val onClick: (ProductEntity) -> Unit = {}
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {
    private val formatter = DecimalFormat("#,###")
    private var productEntities: List<ProductEntity> = emptyList()
    private var productWithAct: List<ProductWithPacking> = emptyList()
    private var imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    private var useModel = false
    private lateinit var productPackingAdapter: SpinnerAdapter

    fun setProductData(
        list: List<ProductEntity>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        this.productEntities = list
        this.imagesMap = imagesMap
        this.useModel = false
        notifyDataSetChanged()
    }

    fun setProductWithActData(
        list: List<ProductWithPacking>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        this.productWithAct = list
        this.imagesMap = imagesMap
        this.useModel = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = if (useModel) productWithAct.size else productEntities.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (useModel) {
            val product = productWithAct[position]
            val images = imagesMap[product.product.id] ?: emptyList()
            holder.bind(product, images)
        } else {
            val product = productEntities[position]
            val images = imagesMap[product.id] ?: emptyList()
            holder.bind(product, images)
        }
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var watcherUnit1Value: TextWatcher? = null
        private var watcherPackingValue: TextWatcher? = null
        private var currentProduct: ProductWithPacking? = null
        private var isUpdating = false // برای جلوگیری از حلقه‌ی بی‌نهایت

        // برای ProductEntity
        fun bind(product: ProductEntity, images: List<ProductImageEntity>) = with(binding) {
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.name ?: ""}"
            tvUnitName.text = product.unitName ?: ""

            // نمایش تصویر
            if (!images.isNullOrEmpty()) {
                val localPath = images.first().localPath
                Glide.with(ivProduct.context)
                    .load(File(localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }
            llPrice.gone()
            cvProductPacking.gone()
            clAmount.gone()
            root.setOnClickListener { onClick(product) }
        }

        // برای ProductModel
        fun bind(product: ProductWithPacking, images: List<ProductImageEntity>) = with(binding) {
            currentProduct = product




            llPrice.show()
            clAmount.show()
            cvProductPacking.show()
            clUnitName.gone()

            // حذف Ripple در این حالت
            root.apply {
                isClickable = false
                isFocusable = false
                rippleColor = null
                foreground = null
            }
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.product.name ?: ""}"
            tvUnitName.text = "" // در ProductModel unitName نداریم، می‌توان اضافه کرد اگر لازم باشد
            tvPrice.text = "قیمت: ${formatter.format(product.finalRate)}" + " ریال"

            // نمایش تصویر
            if (!images.isNullOrEmpty()) {
                val localPath = images.first().localPath
                Glide.with(ivProduct.context)
                    .load(File(localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }


            setupEditTexts(product)
            setupButtons(product)
            setupSpinner(product)

            */
/*  watcherUnit1Value?.let { etUnit1Value.removeTextChangedListener(it) }
              watcherPackingValue?.let { etPackingValue.removeTextChangedListener(it) }

              val quantity = currentQuantities[product.product.id] ?: 0
              if (etUnit1Value.text.toString() != quantity.toString()) etUnit1Value.setText(quantity.toString())

              watcherUnit1Value = object : TextWatcher {
                  override fun beforeTextChanged(
                      s: CharSequence?,
                      start: Int,
                      count: Int,
                      after: Int
                  ) {
                  }

                  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                  override fun afterTextChanged(s: Editable?) {
                      notifyChange(product)
                  }
              }

              watcherPackingValue = object : TextWatcher {
                  override fun beforeTextChanged(
                      s: CharSequence?,
                      start: Int,
                      count: Int,
                      after: Int
                  ) {
                  }

                  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                  override fun afterTextChanged(s: Editable?) {
                      notifyChange(product)
                  }
              }

              etUnit1Value.addTextChangedListener(watcherUnit1Value)
              etPackingValue.addTextChangedListener(watcherPackingValue)

              ivMax.setOnClickListener {
                  val currentQty = etUnit1Value.text.toString().toIntOrNull() ?: 0
                  etUnit1Value.setText((currentQty + 1).toString())
              }
              ivMax.setOnClickListener {
                  val current = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                  val newValue = current + 1.0
                  binding.etUnit1Value.setText(newValue.toString())
              }

              ivMin.setOnClickListener {
                  val currentQty = etUnit1Value.text.toString().toIntOrNull() ?: 0
                  etUnit1Value.setText((currentQty - 1).coerceAtLeast(0).toString())
              }

              val packingNames: MutableList<String> = product.packings
                  .map { it.packingName ?: "" }
                  .toMutableList()

              productPackingAdapter = SpinnerAdapter(root.context, packingNames)
              spProductPacking.adapter = productPackingAdapter

              // تعیین انتخاب پیش‌فرض (اگر وجود داشته باشد)
              val defaultIndex = product.packings.indexOfFirst { it.isDefault == true }
              if (defaultIndex >= 0 && defaultIndex < packingNames.size) {
                  spProductPacking.setSelection(defaultIndex)
              } else if (packingNames.isNotEmpty()) {
                  spProductPacking.setSelection(0)
              }

              var isSpinnerInitialized = false

              spProductPacking.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                  override fun onItemSelected(
                      parent: AdapterView<*>?,
                      view: View?,
                      position: Int,
                      id: Long
                  ) {
                      val selectedPacking = product.packings.getOrNull(position)

                      *//*
*/
/*    if (position > 0) {
                            detail.applyPacking(selectedPacking)
                        } else {
                            detail.applyPacking(null)

                            binding.etPackingValue.setText("")
                        }
                        productPackingAdapter.notifyDataSetChanged()*//*
*/
/*

                    if (!isSpinnerInitialized) {
                        isSpinnerInitialized = true
                        return
                    }
                    notifyChange(product)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }*//*

        }

        private fun setupEditTexts(product: ProductWithPacking) {
            binding.etUnit1Value.removeTextChangedListener(unit1Watcher)
            binding.etPackingValue.removeTextChangedListener(packingWatcher)

            val quantity = currentQuantities[product.product.id] ?: 0
            binding.etUnit1Value.setText(quantity.toString())

            binding.etUnit1Value.addTextChangedListener(unit1Watcher)
            binding.etPackingValue.addTextChangedListener(packingWatcher)
        }

        private val unit1Watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val value = s.toString().toDoubleOrNull() ?: 0.0
                CoroutineScope(Dispatchers.Main).launch {
                    notifyChangeFromUnit1(value)
                }
            }
        }

        private val packingWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val intValue = s.toString().toIntOrNull() ?: 0
                CoroutineScope(Dispatchers.Main).launch {
                    notifyChangeFromPacking(intValue.toDouble())
                }
            }
        }


        private fun setupButtons(product: ProductWithPacking) {
            binding.apply {
                */
/*       binding.ivMax.setOnClickListener {
                           val currentQty = binding.etUnit1Value.text.toString().toDoubleOrNull()?.toInt() ?: 0
                           binding.etUnit1Value.setText((currentQty + 1).toString())
                           // TextWatcher خودش notifyChange را فراخوانی می‌کند
                       }
                       binding.ivMin.setOnClickListener {
                           val currentQty = binding.etUnit1Value.text.toString().toDoubleOrNull()?.toInt() ?: 0
                           binding.etUnit1Value.setText((currentQty - 1).coerceAtLeast(0).toString())
                       }*//*


                // دکمه Max
                ivMax.setOnClickListener {
                    val current = etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val incremented = current.toInt() + 1 // همیشه به عدد صحیح تبدیل کن
                    etUnit1Value.setText(incremented.toString())
                }

// دکمه Min
                ivMin.setOnClickListener {
                    val current = etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val decremented = (current.toInt() - 1).coerceAtLeast(0)
                    etUnit1Value.setText(decremented.toString())
                }
            }
        }

        private fun setupSpinner(product: ProductWithPacking) {
            val packingNames = product.packings.map { it.packingName ?: "" }
            val adapter = SpinnerAdapter(itemView.context, packingNames.toMutableList())
            binding.spProductPacking.adapter = adapter

            val defaultIndex = product.packings.indexOfFirst { it.isDefault == true }
            binding.spProductPacking.setSelection(if (defaultIndex >= 0) defaultIndex else 0)

            var initialized = false
            binding.spProductPacking.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        pos: Int,
                        id: Long
                    ) {
                        if (!initialized) {
                            initialized = true
                            return
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            notifyChangeFromUnit1(
                                binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                            )
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
        }

        private suspend fun notifyChangeFromUnit1(unit1: Double) {
            val product = currentProduct ?: return
            val packing =
                product.packings.getOrNull(binding.spProductPacking.selectedItemPosition) ?: return

            val detail = createBaseDetail(product, packing)
            detail.applyProduct(product)
            detail.applyPacking(packing)

            detail.setInputUnit1Value(unit1, factorViewModel.factorHeader.value?.defaultAnbarId!!)

            syncUI(detail)
            onProductChanged(detail)
        }

        private suspend fun notifyChangeFromPacking(packingVal: Double) {
            val product = currentProduct ?: return
            val packing =
                product.packings.getOrNull(binding.spProductPacking.selectedItemPosition) ?: return

            val detail = createBaseDetail(product, packing)
            detail.applyProduct(product)
            detail.applyPacking(packing)

            detail.setInputPackingValue(
                packingVal,
                factorViewModel.factorHeader.value?.defaultAnbarId!!
            )

            syncUI(detail)
            onProductChanged(detail)
        }

        private fun createBaseDetail(
            product: ProductWithPacking,
            packing: ProductPackingEntity
        ): FactorDetailEntity {
            return FactorDetailEntity(
                factorId = factorId,
                sortCode = bindingAdapterPosition,
                productId = product.product.id,
                anbarId = factorViewModel.factorHeader.value?.defaultAnbarId!!,
                actId = factorViewModel.factorHeader.value?.actId!!,
                unit1Value = 0.0,
                unit2Value = 0.0,
                price = product.finalRate,
                packingId = packing.packingId,
                packingValue = 0.0,
                vat = 0.0
            ).apply {
                repository = factorViewModel.productRepository
            }
        }

   */
/*     private fun syncUI(detail: FactorDetailEntity) {
            isUpdating = true
            binding.etUnit1Value.setText(detail.unit1Value.toString())
            binding.etPackingValue.setText(detail.packingValue.toString())
            isUpdating = false
        }*//*

   private fun syncUI(detail: FactorDetailEntity) {
       isUpdating = true

       // نمایش unit1Value بدون اعشار اضافه
       val unit1Text = if (detail.unit1Value % 1 == 0.0) {
           detail.unit1Value.toInt().toString()
       } else {
           detail.unit1Value.toString()
       }

       // ❌ اشتباه: packingValue را عدد خام نمایش بده، نه عدد صحیح
       // ✅ درست: فقط مقدار واقعی packingValue را نمایش بده
       val packingText = detail.packingValue.toString()

       binding.etUnit1Value.setText(unit1Text)
       binding.etPackingValue.setText(packingText)

       isUpdating = false
   }
        */
/*
                private fun notifyChange(product: ProductWithPacking) {

                    val unit1Value =
                        binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0

                    val packingValue =
                        binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0

                    val selectedPacking =
                        product.packings.getOrNull(binding.spProductPacking.selectedItemPosition)
                            ?: return

                    val detail = FactorDetailEntity(
                        factorId = factorId,
                        productId = product.product.id,
                        unit1Value = unit1Value,
                        price = product.finalRate,
                        packingId = selectedPacking.packingId,
                       // packingName = selectedPacking.packingName ?: ""
                    )

                    if (packingValue > 0) {
                        factorViewModel.updateByPacking(
                            detail,
                            packingValue,
                            product,
                            selectedPacking
                        )

                        Log.d("factorViewModeldetail", detail.toString())
                        Log.d("factorViewModelpackingValue", packingValue.toString())
                        Log.d("factorViewModelproduct", product.toString())
                        Log.d("factorViewModelselectedPacking", selectedPacking.toString())
                    } else {
                        factorViewModel.updateByUnit(
                            detail,
                            unit1Value,
                            product,
                            selectedPacking
                        )

                        Log.d("factorViewModeldetail2", detail.toString())
                        Log.d("factorViewModelpackingValue2", unit1Value.toString())
                        Log.d("factorViewModelproduct2", product.toString())
                        Log.d("factorViewModelselectedPacking2", selectedPacking.toString())
                    }
                }
        *//*


        */
/*

                private fun notifyChange(product: ProductWithPacking) {

                    val unit1Value = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val packingValue = binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0
                    val selectedPacking =
                        product.packings.getOrNull(binding.spProductPacking.selectedItemPosition)
                            ?: return
                    val detail = FactorDetailEntity(
                        factorId = factorId,
                        sortCode = bindingAdapterPosition,
                        productId = product.product.id,
                        anbarId = factorViewModel.factorHeader.value?.defaultAnbarId!!,
                        actId = factorViewModel.factorHeader.value?.actId!!,
                        unit1Value = unit1Value,
                        unit2Value = 0.0,
                        price = product.finalRate,
                        packingId = selectedPacking.packingId,
                        packingValue = packingValue,
                        vat = 0.0
                    )

                    // اول Product و Packing را ست کن
                    detail.repository = factorViewModel.productRepository   // خیلی مهم
                   detail.applyProduct(product)
                   // detail.applyPacking(selectedPacking)

                    // بعد محاسبات
                    detail.setPackingValue1(
                        packingValue,
                        factorViewModel.factorHeader.value?.defaultAnbarId!!
                    )
                    if (unit1Value > 0) detail.setUnit1Value1(
                         unit1Value,
                        factorViewModel.factorHeader.value?.defaultAnbarId!!
                    )

                    Log.d("factordetail", detail.toString())
                    Log.d("factordetail2", product.toString())
                    Log.d("factordetail3", packingValue.toString())

                    onProductChanged(detail)
                }


                private fun notifyChange(product: ProductWithPacking) {
                    val unit1Value = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
                    val selectedPacking = product.packings.getOrNull(binding.spProductPacking.selectedItemPosition) ?: return

                    val detail = FactorDetailEntity(
                        factorId = factorId,
                        sortCode = bindingAdapterPosition,
                        productId = product.product.id,
                        anbarId = factorViewModel.factorHeader.value?.defaultAnbarId!!,
                        actId = factorViewModel.factorHeader.value?.actId!!,
                        unit1Value = unit1Value, // فقط این را استفاده کن
                        unit2Value = 0.0,
                        price = product.finalRate,
                        packingId = selectedPacking.packingId,
                        packingValue = 0.0, // این را خود سیستم محاسبه کند
                        vat = 0.0
                    )

                    detail.repository = factorViewModel.productRepository
                    detail.applyProduct(product)
                    detail.applyPacking(selectedPacking) // این هم فقط packingId را ست می‌کند

                    // فقط یک بار unit1Value را ست کن → بقیه خودکار به‌روز می‌شوند
                    detail.setUnit1Value1(unit1Value, factorViewModel.factorHeader.value?.defaultAnbarId!!)

                    onProductChanged(detail)
                }*//*

    }
}

*/

class ProductListAdapter(
    private val factorViewModel: FactorViewModel,
    private val fromFactor: Boolean = false,
    private val factorId: Int,
    private val onProductChanged: (FactorDetailEntity) -> Unit,
    private val currentQuantities: Map<Int, Int> = emptyMap(),
    private val onClick: (ProductEntity) -> Unit = {}
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    private val formatter = DecimalFormat("#,###")
    private var productEntities: List<ProductEntity> = emptyList()
    private var productWithAct: List<ProductWithPacking> = emptyList()
    private var imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    private var useModel = false

    fun setProductData(
        list: List<ProductEntity>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        this.productEntities = list
        this.imagesMap = imagesMap
        this.useModel = false
        notifyDataSetChanged()
    }

    fun setProductWithActData(
        list: List<ProductWithPacking>,
        imagesMap: Map<Int, List<ProductImageEntity>> = emptyMap()
    ) {
        this.productWithAct = list
        this.imagesMap = imagesMap
        this.useModel = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int = if (useModel) productWithAct.size else productEntities.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (useModel) {
            val product = productWithAct[position]
            val images = imagesMap[product.product.id] ?: emptyList()
            holder.bind(product, images)
        } else {
            val product = productEntities[position]
            val images = imagesMap[product.id] ?: emptyList()
            holder.bind(product, images)
        }
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentProduct: ProductWithPacking? = null
        private var watcherUnit1: TextWatcher? = null
        private var watcherPacking: TextWatcher? = null

        fun bind(product: ProductEntity, images: List<ProductImageEntity>) = with(binding) {
            // حالت بدون packing
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.name ?: ""}"
            tvUnitName.text = product.unitName ?: ""
            // ... تصاویر و سایر تنظیمات
            llPrice.gone()
            cvProductPacking.gone()
            clAmount.gone()
            root.setOnClickListener { onClick(product) }
        }

        fun bind(product: ProductWithPacking, images: List<ProductImageEntity>) = with(binding) {
            currentProduct = product

            llPrice.show()
            clAmount.show()
            cvProductPacking.show()
            clUnitName.gone()

            root.isClickable = false
            tvNameProduct.text = "${bindingAdapterPosition + 1}_  ${product.product.name ?: ""}"
            tvPrice.text = "قیمت: ${formatter.format(product.finalRate)} ریال"

            // تصاویر
            if (images.isNotEmpty()) {
                Glide.with(ivProduct.context)
                    .load(File(images.first().localPath))
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivProduct)
            } else {
                ivProduct.setImageResource(R.drawable.ic_placeholder)
            }

            setupInputs(product)
            setupButtons()
            setupSpinner(product)
        }

        private fun setupInputs(product: ProductWithPacking) {
            watcherUnit1?.let { binding.etUnit1Value.removeTextChangedListener(it) }
            watcherPacking?.let { binding.etPackingValue.removeTextChangedListener(it) }

            val qty = currentQuantities[product.product.id] ?: 0
            binding.etUnit1Value.setText(qty.toString())

            watcherUnit1 = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    notifyChange()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            watcherPacking = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    notifyChange()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }

            binding.etUnit1Value.addTextChangedListener(watcherUnit1)
            binding.etPackingValue.addTextChangedListener(watcherPacking)
        }

        private fun setupButtons() {
            binding.ivMax.setOnClickListener {
                val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
                binding.etUnit1Value.setText((current + 1).toString())
            }
            binding.ivMin.setOnClickListener {
                val current = binding.etUnit1Value.text.toString().toIntOrNull() ?: 0
                binding.etUnit1Value.setText((current - 1).coerceAtLeast(0).toString())
            }
        }

        private fun setupSpinner(product: ProductWithPacking) {
            val names = product.packings.map { it.packingName ?: "" }
            val adapter = SpinnerAdapter(itemView.context, names.toMutableList())
            binding.spProductPacking.adapter = adapter

            val default = product.packings.indexOfFirst { it.isDefault == true }
            binding.spProductPacking.setSelection(if (default >= 0) default else 0)

            var init = false
            binding.spProductPacking.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    if (!init) { init = true; return }
                    notifyChange()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        private fun notifyChange() {
            val product = currentProduct ?: return
            val packing = product.packings.getOrNull(binding.spProductPacking.selectedItemPosition) ?: return

            val unit1Value = binding.etUnit1Value.text.toString().toDoubleOrNull() ?: 0.0
            val packingValue = binding.etPackingValue.text.toString().toDoubleOrNull() ?: 0.0

            // 🔥 فقط مقادیر خام را بفرست — هیچ محاسبه‌ای انجام نده!
            val detail = FactorDetailEntity(
                factorId = factorId,
                sortCode = bindingAdapterPosition,
                productId = product.product.id,
                anbarId = factorViewModel.factorHeader.value?.defaultAnbarId!!,
                actId = factorViewModel.factorHeader.value?.actId!!,
                unit1Value = unit1Value,       // خام
                unit2Value = 0.0,
                price = product.finalRate,
                packingId = packing.packingId,
                packingValue = packingValue,   // خام
                vat = 0.0
            )

            detail.applyProduct(product)
            detail.applyPacking(packing)
            detail.repository = factorViewModel.productRepository

            onProductChanged(detail)
        }
    }
}
