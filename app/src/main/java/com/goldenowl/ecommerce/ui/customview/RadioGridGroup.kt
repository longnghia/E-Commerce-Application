package com.goldenowl.ecommerce.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.CompoundButton
import android.widget.GridLayout
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatRadioButton
import java.util.concurrent.atomic.AtomicInteger


class RadioGridGroup : GridLayout {
    var checkedCheckableImageButtonId = -1
        private set
    private var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var mProtectFromCheckedChange = false
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null
    private var mPassThroughListener: PassThroughHierarchyChangeListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        mPassThroughListener = PassThroughHierarchyChangeListener()
        super.setOnHierarchyChangeListener(mPassThroughListener)
    }

    override fun setOnHierarchyChangeListener(listener: OnHierarchyChangeListener?) {
        mPassThroughListener!!.mOnHierarchyChangeListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (checkedCheckableImageButtonId != -1) {
            mProtectFromCheckedChange = true
            setCheckedStateForView(checkedCheckableImageButtonId, true)
            mProtectFromCheckedChange = false
            setCheckedId(checkedCheckableImageButtonId)
        }
    }

    override fun addView(@NonNull child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (child is AppCompatRadioButton) {
            val button = child as AppCompatRadioButton
            if (button.isChecked) {
                mProtectFromCheckedChange = true
                if (checkedCheckableImageButtonId != -1) {
                    setCheckedStateForView(checkedCheckableImageButtonId, false)
                }
                mProtectFromCheckedChange = false
                setCheckedId(button.id)
            }
        }
        super.addView(child, index, params)
    }

    fun check(id: Int) {
        if (id != -1 && id == checkedCheckableImageButtonId) {
            return
        }
        if (checkedCheckableImageButtonId != -1) {
            setCheckedStateForView(checkedCheckableImageButtonId, false)
        }
        if (id != -1) {
            setCheckedStateForView(id, true)
        }
        setCheckedId(id)
    }

    private fun setCheckedId(id: Int) {
        checkedCheckableImageButtonId = id
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener!!.onCheckedChanged(this, checkedCheckableImageButtonId)
        }
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView: View = findViewById(viewId)
        if (checkedView != null && checkedView is AppCompatRadioButton) {
            (checkedView as AppCompatRadioButton).isChecked = checked
        }
    }

    fun clearCheck() {
        check(-1)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        mOnCheckedChangeListener = listener
    }

    override fun onInitializeAccessibilityEvent(@NonNull event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = RadioGridGroup::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(@NonNull info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = RadioGridGroup::class.java.name
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(group: RadioGridGroup?, checkedId: Int)
    }

    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (mProtectFromCheckedChange) {
                return
            }
            mProtectFromCheckedChange = true
            if (checkedCheckableImageButtonId != -1) {
                setCheckedStateForView(checkedCheckableImageButtonId, false)
            }
            mProtectFromCheckedChange = false
            val id = buttonView.id
            setCheckedId(id)
        }
    }

    private inner class PassThroughHierarchyChangeListener : OnHierarchyChangeListener {
        var mOnHierarchyChangeListener: OnHierarchyChangeListener? = null
        override fun onChildViewAdded(parent: View, child: View) {
            if (parent === this@RadioGridGroup && child is AppCompatRadioButton) {
                var id: Int = child.getId()
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = generateViewId()
                    child.setId(id)
                }
                (child as AppCompatRadioButton).setOnCheckedChangeListener(
                    mChildOnCheckedChangeListener
                )
            }
            mOnHierarchyChangeListener?.onChildViewAdded(parent, child)
        }

        override fun onChildViewRemoved(parent: View, child: View?) {
            if (parent === this@RadioGridGroup && child is AppCompatRadioButton) {
                (child as AppCompatRadioButton).setOnCheckedChangeListener(null)
            }
            mOnHierarchyChangeListener?.onChildViewRemoved(parent, child)
        }
    }

    companion object {
        private val sNextGeneratedId: AtomicInteger = AtomicInteger(1)
        fun generateViewId(): Int {
            while (true) {
                val result: Int = sNextGeneratedId.get()

                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result
                }
            }
        }
    }
}