import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2


private var sensitivity = -1

fun ViewPager2.reduceDragSensitivity() {
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(recyclerView) as Int
    if (sensitivity < 0) {
        sensitivity = touchSlop
    }
    touchSlopField.set(recyclerView, sensitivity * 5) // "6" was obtained experimentally
//    Log.d("Cirno", "reduceDragSensitivity: $touchSlop")
}

fun ViewPager2.normalizeDragSensitivity() {
    if (sensitivity < 0) {
        return
    }
    val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
    recyclerViewField.isAccessible = true
    val recyclerView = recyclerViewField.get(this) as RecyclerView

    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    touchSlopField.set(recyclerView, sensitivity) // "6" was obtained experimentally
}
