package com.github.c0rr4do.expansionlayout

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class ExpansionIndicator(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private var _collapsedRotation: Float

    /**
     * Rotation of indicator when linked [ExpansionLayout] is collapsed in angles
     */
    var collapsedRotation: Float
        get() = _collapsedRotation
        set(value) {
            _collapsedRotation = value
        }

    /**
     * Angle of indicator when linked [ExpansionLayout] is expanded
     */
    private var _expandedRotation: Float
    var expandedRotation: Float
        get() = _expandedRotation
        set(value) {
            _expandedRotation = value
        }

    private var _expansionLayout: ExpansionLayout? = null

    /**
     * [ExpansionLayout] that is linked to this indicator
     */
    val expansionLayout: ExpansionLayout?
        get() = _expansionLayout

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ExpansionIndicator, 0, 0).apply {
            try {
                _collapsedRotation = getFloat(R.styleable.ExpansionIndicator_collapsedRotation, 0f)
                _expandedRotation = getFloat(R.styleable.ExpansionIndicator_expandedRotation, 180f)
                if (drawable == null) {
                    setImageResource(R.drawable.ic_baseline_expand_more_24)
                }
            } finally {
                recycle()
            }
        }
    }

    internal fun linkExpansionLayout(expansionLayout: ExpansionLayout) {
        _expansionLayout = expansionLayout
        rotation = if (expansionLayout.expanded) expandedRotation else collapsedRotation
        setOnClickListener { this.expansionLayout?.toggle() }
    }

    internal fun unlinkExpansionLayout() {
        setOnClickListener(null)
        _expansionLayout = null
    }
}