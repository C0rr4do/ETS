package com.github.c0rr4do.expansionlayout

import android.animation.*
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import kotlin.math.abs

class ExpansionLayout(context: Context, attrs: AttributeSet? = null) : LinearLayoutCompat(context, attrs) {
    private var _expanded: Boolean

    /**
     * Indicates whether this [ExpansionLayout] is expanded.
     */
    var expanded: Boolean
        get() = _expanded
        private set(value) {
            _expanded = value
            _parentGroup?.onExpansionToggle(this, value)
            _expansionListener?.onExpansionToggle(this, value)
        }

    private var _expansionDuration: Long

    /**
     * Duration of expansion animation in milliseconds.
     *
     * If [collapsionDuration] of this [ExpansionLayout] is <i>null</i> this duration will also be used for collapsion animation.
     * */
    var expansionDuration: Long
        get() = _expansionDuration
        set(value) {
            _expansionDuration = value
        }

    private var _collapsionDuration: Long?

    /**
     * Duration of collapsion animation in milliseconds.
     *
     * If this property's value is null, [expansionDuration] will be used for expansion animation.
     */
    var collapsionDuration: Long?
        get() = _collapsionDuration
        set(value) {
            _collapsionDuration = value
        }

    private var _initialExpansionIndicatorId: Int
    private var _expansionIndicator: ExpansionIndicator? = null

    /**
     * Indicator view (This view will be animated on expansion/collapsion).
     *
     * It can also be used to toggle the expansion of this [ExpansionLayout].
     */
    var expansionIndicator: ExpansionIndicator?
        get() = _expansionIndicator
        set(value) {
            expansionIndicator?.unlinkExpansionLayout()
            _expansionIndicator = value
            expansionIndicator?.linkExpansionLayout(this)
        }

    private var _expansionInterpolator: TimeInterpolator

    /**
     * Interpolator for expansion animation.
     *
     * If [collapsionInterpolator] of this [ExpansionLayout] is <i>null</i> this interpolator will also be used for collapsion animation.
     */
    var expansionInterpolator: TimeInterpolator
        get() = _expansionInterpolator
        set(value) {
            _expansionInterpolator = value
        }

    private var _collapsionInterpolator: TimeInterpolator?

    /**
     * Interpolator for collapsion animation.
     *
     * If this property's value is null, [expansionInterpolator] will be used for collapsion animation.
     */
    var collapsionInterpolator: TimeInterpolator?
        get() = _collapsionInterpolator
        set(value) {
            _collapsionInterpolator = value
        }

    private var _expansionListener: ExpansionListener? = null

    /**
     * Callback for expansion/collapsion of this [ExpansionLayout].
     *
     * It will be called when [expanded] changes.
     */
    var expansionListener: ExpansionListener?
        get() = _expansionListener
        set(value) {
            _expansionListener = value
        }

    private var _toggleOnHeaderClick: Boolean

    /**
     * Specifies whether expansion state should be toggled on click events of header view.
     */
    var toggleOnHeaderClick: Boolean
        get() = _toggleOnHeaderClick
        set(value) {
            _toggleOnHeaderClick = value
            headerView?.setOnClickListener(if (value) headerViewClickListener else null)
        }

    private var _negativeSizeMode: NegativeSizeMode

    /**
     * Specifies how negative size values produced by animation are handled.
     */
    var negativeSizeMode: NegativeSizeMode
        get() = _negativeSizeMode
        set(value) {
            _negativeSizeMode = value
        }

    private var _parentGroup: ExpansionLayoutGroup? = null
    var parentGroup: ExpansionLayoutGroup?
        get() = _parentGroup
        private set(value) {
            _parentGroup = value
        }

    private val headerView: View?
        get() = if (childCount > 0) children.elementAt(0) else null

    private val contentView: View?
        get() = if (childCount > 1) children.elementAt(1) else null

    private val headerViewClickListener = OnClickListener { toggle() }

    private val contentViewFullWidth: Int
        get() {
            contentView?.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            return contentView?.measuredWidth ?: 0
        }

    private val contentViewFullHeight: Int
        get() {
            contentView?.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            return contentView?.measuredHeight ?: 0
        }

    private var runningAnimations = mutableSetOf<AnimatorSet>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ExpansionLayout, 0, 0).apply {
            try {
                // Load expanded value from xml attributes
                // Default: false
                _expanded = getBoolean(R.styleable.ExpansionLayout_expanded, false)

                // Load 'expansionDuration' from xml attributes
                // Default: 300 (-> androids default duration)
                _expansionDuration = getInt(R.styleable.ExpansionLayout_expansionDuration, 300).toLong()

                getInt(R.styleable.ExpansionLayout_collapsionDuration, -1).toLong().let {
                    _collapsionDuration = if (it > -1) it else null
                }

                // Load 'expansionIndicator' from xml attributes
                // Default: 0 (-> empty resource id)
                _initialExpansionIndicatorId = getResourceId(R.styleable.ExpansionLayout_expansionIndicator, 0)

                // Load interpolator from xml attributes
                // Default: androids default interpolator
                _expansionInterpolator = AnimationUtils.loadInterpolator(
                    context,
                    getResourceId(
                        R.styleable.ExpansionLayout_expansionInterpolator,
                        android.R.anim.accelerate_decelerate_interpolator
                    )
                )

                // Load interpolator from xml attributes
                // Default: androids default interpolator
                getResourceId(R.styleable.ExpansionLayout_collapsionInterpolator, 0).let {
                    _collapsionInterpolator = if (it > 0) AnimationUtils.loadInterpolator(context, it) else null
                }

                // Load 'toggleOnHeaderClick' from xml attributes
                // Default: false
                _toggleOnHeaderClick = getBoolean(R.styleable.ExpansionLayout_toggleOnHeaderClick, false)

                // Load 'negativeSizeMode from xml attributes
                // Default: keep negative values
                _negativeSizeMode =
                    NegativeSizeMode.values()[getInt(
                        R.styleable.ExpansionLayout_negativeSizeMode,
                        NegativeSizeMode.values().indexOf(NegativeSizeMode.ABS)
                    )]

                // Load 'android:orientation' from xml attributes
                // Default: LinearLayoutCompat.VERTICAL
                orientation = getInt(R.styleable.ExpansionLayout_android_orientation, VERTICAL)
            } finally {
                recycle()
            }
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (childCount < MAX_CHILD_COUNT) {
            super.addView(child, index, params)
        } else {
            // Child count
            throw IllegalStateException(CHILD_COUNT_EXCEPTION_MESSAGE)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (expanded) expand(false)
        else collapse(false)

        if (toggleOnHeaderClick) headerView?.setOnClickListener(headerViewClickListener)
        expansionIndicator = findViewById(_initialExpansionIndicatorId)
    }

    /**
     * Expands this [ExpansionLayout]
     */
    fun expand(animate: Boolean = true) {
        executeExpansion(true, animate)
    }

    /**
     * Collapses this [ExpansionLayout]
     */
    fun collapse(animate: Boolean = true) {
        executeExpansion(false, animate)
    }

    /**
     * Toggles the expansion of this [ExpansionLayout]
     */
    fun toggle(animate: Boolean = true) {
        if (expanded) {
            collapse(animate)
        } else {
            expand(animate)
        }
    }

    internal fun attachToGroup(expansionLayoutGroup: ExpansionLayoutGroup) {
        parentGroup = expansionLayoutGroup
    }

    internal fun detachFromGroup() {
        parentGroup = null
    }

    private fun executeExpansion(expand: Boolean, animate: Boolean) {
        // If 'animate' is false set 'expansionDuration' to 0
        val expansionDuration = if (animate) expansionDuration else 0L

        var animateWidth: Boolean? = null
        var targetSize: Int? = null
        var targetRotation: Float? = null

        // If there is a content view
        contentView?.apply {
            // Set 'animateWidth'
            animateWidth = orientation == HORIZONTAL
            // Set 'targetSize'
            targetSize = if (expand) {
                if (animateWidth == true) contentViewFullWidth else contentViewFullHeight
            } else 0
        }
        // If there is an indicator view
        expansionIndicator?.apply {
            // Set target rotation
            targetRotation = if (expand) expandedRotation else collapsedRotation
        }

        // If animation is disabled
        if (expansionDuration == 0L) {
            // If there is a content view
            contentView?.apply {
                layoutParams =
                    layoutParams.apply { if (animateWidth == true) width = targetSize!! else height = targetSize!! }
            }
            // If there is an indicator view
            expansionIndicator?.apply {
                rotation = targetRotation!!
            }
        } else {
            val animators = mutableSetOf<Animator>()

            // If there is a content view
            contentView?.apply {
                animators.add(sizeAnimator(this, targetSize!!, animateWidth!!))
            }
            expansionIndicator?.apply {
                animators.add(rotationAnimator(this, targetRotation!!))
            }

            // If there are animators to be executed
            if (animators.isNotEmpty()) {

                // Combine animators to simultaneous animation
                val mergedAnimation = AnimatorSet().apply {
                    playTogether(animators)

                    duration = if (expand) expansionDuration else collapsionDuration ?: expansionDuration

                    interpolator = if (expand) expansionInterpolator else collapsionInterpolator ?: expansionInterpolator


                    doOnStart {
                        // Cancel all running expansion/collapse animations
                        runningAnimations.forEach {
                            runningAnimations.remove(it)
                            it.cancel()
                        }
                        // Add 'mergedAnimation' to runningAnimations
                        runningAnimations.add(this)
                        contentView?.let { contentView ->
                            // If 'contentView' was collapsed and thus INVISIBLE set its visibility to VISIBLE
                            contentView.visibility = VISIBLE
                        }
                    }

                    doOnEnd {
                        // Remove this animation from running animations and ping 'expansionListener'
                        finishExpansionExecution(it, expand)
                    }
                }
                mergedAnimation.start()
            }
        }
        expanded = expand
    }

    private fun sizeAnimator(view: View, target: Int, animateWidth: Boolean = false): ValueAnimator {
        view.apply {
            val values =
                floatArrayOf((if (animateWidth) layoutParams.width else layoutParams.height).toFloat(), target.toFloat())

            val contentViewAnimator = ValueAnimator.ofFloat(*values)

            contentViewAnimator.addUpdateListener {
                // Updated width/height value for 'view'
                var newLength = (it.animatedValue as Float).toInt()

                if (negativeSizeMode == NegativeSizeMode.ABS) newLength = abs(newLength)
                else if (negativeSizeMode == NegativeSizeMode.CEIL_TO_ZERO && newLength < 0) newLength = 0

                // Apply updated width/height to 'view'
                layoutParams = layoutParams.apply { if (animateWidth) width = newLength else height = newLength }
            }
            return contentViewAnimator
        }
    }

    private fun rotationAnimator(view: View, targetRotation: Float): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, ROTATION, targetRotation)
    }

    private fun finishExpansionExecution(animation: Animator?, expand: Boolean) {
        if (animation == null || runningAnimations.contains(animation)) {
            runningAnimations.remove(animation)
            expansionListener?.onAnimationFinished(this, expand)
            contentView?.let { contentView ->
                if (!expand) {
                    contentView.visibility = GONE
                }
            }
        }
    }

    companion object {
        private const val MAX_CHILD_COUNT = 2
        private val CHILD_COUNT_EXCEPTION_MESSAGE =
            "View of type ${ExpansionLayout::class.java} may contain a maximum of $MAX_CHILD_COUNT direct children (1 header-view, 1 content-view)"
    }

    interface ExpansionListener {
        fun onExpansionToggle(expansionLayout: ExpansionLayout, expanded: Boolean) {}
        fun onAnimationFinished(expansionLayout: ExpansionLayout, expanded: Boolean) {}
    }
}