package com.partsystem.partvisitapp.core.utils

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.partsystem.partvisitapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException

/** دانلود عکس و ذخیره روی حافظه داخلی */
suspend fun saveBase64ImageToFile(base64Data: String, fileName: String, context: Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            // پاک‌سازی احتمالی header و newlineها
            val cleanedBase64 = base64Data.substringAfter(",").replace("\n", "").replace("\r", "")

            // decode Base64
            val bytes = android.util.Base64.decode(cleanedBase64, android.util.Base64.DEFAULT)

            // ایجاد فایل با پسوند jpg در حافظه داخلی
            val file = File(context.filesDir, "$fileName.jpg")
            file.outputStream().use { it.write(bytes) }

            // بازگرداندن مسیر فایل
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

object NetworkUtil {
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
}

fun getTypefaceRegular(context: Context): Typeface {
    return ResourcesCompat.getFont(context, R.font.iran_sans)!!
}

fun getColorAttr(ctx: Context, attrId: Int): Int {
    val typedValue = TypedValue()
    ctx.theme.resolveAttribute(attrId, typedValue, true)
    return ContextCompat.getColor(ctx, typedValue.resourceId)
}

fun Context.getColorFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}


class RtlGridLayoutManager : GridLayoutManager {
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, spanCount: Int) : super(context, spanCount)

    constructor(
        context: Context?,
        spanCount: Int,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(context, spanCount, orientation, reverseLayout)

    override fun isLayoutRTL(): Boolean {
        return true
    }
}

class ReceiptTopEdgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val redPaint = Paint().apply {
        color = getColorAttr(context, R.attr.colorPassive)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val grayPaint = Paint().apply {
        color = getColorAttr(context, R.attr.colorRipple)
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = 20f
        val gap = 10f
        var startX = 0f

        while (startX < width) {
            canvas.drawCircle(startX + radius, 0f, radius, redPaint)
            startX += 2 * radius + gap
        }

        //  canvas.drawRect(0f, radius, width.toFloat(), height.toFloat(), grayPaint)
    }
}

object ErrorHandler {
    fun getHttpErrorMessage(context: Context, code: Int, message: String?): String {
        return when (code) {
            400 -> context.getString(R.string.error_bad_request)
            401 -> context.getString(R.string.error_unauthorized)
            403 -> context.getString(R.string.error_forbidden)
            404 -> context.getString(R.string.error_not_found)
            500 -> context.getString(R.string.error_internal_server)
            else -> getErrorMessage(context, message)
        }
    }

    fun getErrorMessage(context: Context, message: String?): String {
        return when {
            message.isNullOrBlank() -> context.getString(R.string.error_unknown)
            message.contains("timeout", ignoreCase = true) ->
                context.getString(R.string.error_timeout)

            message.contains("Unable to resolve host", ignoreCase = true) ->
                context.getString(R.string.error_network_internet)

            else -> message
        }
    }

    fun getExceptionMessage(context: Context, throwable: Throwable): String {
        return when (throwable) {
            is IOException -> context.getString(R.string.error_network_internet)
            is HttpException -> getErrorMessage(context, throwable.message())
            else -> context.getString(R.string.error_unknown)
        }
    }
}

 fun fixPersianChars(input: String): String {
    return input
        .replace('ي', 'ی') // Arabic yeh to Persian yeh
        .replace('ك', 'ک') // Arabic kaf to Persian kaf
}

 fun convertNumbersToEnglish(input: String): String {
    val arabicNumbers = listOf('٠','١','٢','٣','٤','٥','٦','٧','٨','٩')
    val persianNumbers = listOf('۰','۱','۲','۳','۴','۵','۶','۷','۸','۹')
    val englishNumbers = listOf('0','1','2','3','4','5','6','7','8','9')

    var result = input
    for (i in 0..9) {
        result = result.replace(persianNumbers[i], englishNumbers[i])
        result = result.replace(arabicNumbers[i], englishNumbers[i])
    }
    return result
}


