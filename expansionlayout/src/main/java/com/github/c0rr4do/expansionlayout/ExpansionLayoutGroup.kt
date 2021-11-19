package com.github.c0rr4do.expansionlayout

class ExpansionLayoutGroup {
    private var _singleSelect: Boolean = false

    /**
     * Specifies whether more than one [ExpansionLayout] attached to this group may be expanded at the same time
     */
    var singleSelect: Boolean
        get() = _singleSelect
        set(value) {
            _singleSelect = value
        }

    private var _expansionLayouts = mutableListOf<ExpansionLayout>()

    private val expandedLayouts: List<ExpansionLayout>
        get() = _expansionLayouts.filter { it.expanded }

    /**
     * Adds the specified [ExpansionLayout] to this group
     */
    fun add(expansionLayout: ExpansionLayout) {
        if (singleSelect && expandedLayouts.isNotEmpty()) {
            expansionLayout.collapse(false)
        }
        _expansionLayouts.add(expansionLayout)
        expansionLayout.attachToGroup(this)
    }

    /**
     * Removes the specified [ExpansionLayout] from this group
     */
    fun remove(expansionLayout: ExpansionLayout) {
        expansionLayout.detachFromGroup()
        _expansionLayouts.remove(expansionLayout)
    }

    /**
     * @return A shallow copy of all [ExpansionLayout]s attached to this group
     */
    fun getExpansionLayouts() = _expansionLayouts.toSet()

    internal fun onExpansionToggle(expansionLayout: ExpansionLayout, expanded: Boolean) {
        if (singleSelect && expanded) {
            expandedLayouts.forEach {
                if (it != expansionLayout) it.collapse()
            }
        }
    }
}