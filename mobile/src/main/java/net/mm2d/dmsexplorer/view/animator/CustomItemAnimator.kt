/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mm2d.dmsexplorer.view.animator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.SimpleItemAnimator

// copy from android-27/android/support/v7/widget/DefaultItemAnimator.java

/**
 * This implementation of [RecyclerView.ItemAnimator] provides basic
 * animations on remove, add, and move events that happen to the items in
 * a RecyclerView. RecyclerView uses a DefaultItemAnimator by default.
 *
 * @see RecyclerView.setItemAnimator
 */
class CustomItemAnimator(context: Context) : SimpleItemAnimator() {
    private val addingTranslationY: Int

    private val pendingRemovals = ArrayList<ViewHolder>()
    private val pendingAdditions = ArrayList<ViewHolder>()
    private val pendingMoves = ArrayList<MoveInfo>()
    private val pendingChanges = ArrayList<ChangeInfo>()

    private val additionsList = ArrayList<ArrayList<ViewHolder>>()
    private val movesList = ArrayList<ArrayList<MoveInfo>>()
    private val changesList = ArrayList<ArrayList<ChangeInfo>>()

    private val addAnimations = ArrayList<ViewHolder>()
    private val moveAnimations = ArrayList<ViewHolder>()
    private val removeAnimations = ArrayList<ViewHolder>()
    private val changeAnimations = ArrayList<ViewHolder>()

    private class MoveInfo(
        val holder: ViewHolder,
        val fromX: Int,
        val fromY: Int,
        val toX: Int,
        val toY: Int,
    )

    private class ChangeInfo(
        var oldHolder: ViewHolder?,
        var newHolder: ViewHolder?,
        val fromX: Int,
        val fromY: Int,
        val toX: Int,
        val toY: Int,
    ) {
        override fun toString(): String {
            return "ChangeInfo{oldHolder=$oldHolder, newHolder=$newHolder, fromX=$fromX, fromY=$fromY, toX=$toX, toY=$toY}"
        }
    }

    init {
        val density = context.resources.displayMetrics.density
        val translationY = ADDING_TRANSLATION_Y
        addingTranslationY = (translationY * density + 0.5f).toInt()
    }

    override fun runPendingAnimations() {
        val removalsPending = pendingRemovals.isNotEmpty()
        val movesPending = pendingMoves.isNotEmpty()
        val changesPending = pendingChanges.isNotEmpty()
        val additionsPending = pendingAdditions.isNotEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        // First, remove stuff
        for (holder in pendingRemovals) {
            animateRemoveImpl(holder)
        }
        pendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves = ArrayList(pendingMoves)
            movesList.add(moves)
            pendingMoves.clear()
            val mover = Runnable {
                for (moveInfo in moves) {
                    animateMoveImpl(
                        moveInfo.holder,
                        moveInfo.fromX,
                        moveInfo.fromY,
                        moveInfo.toX,
                        moveInfo.toY,
                    )
                }
                moves.clear()
                movesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList(pendingChanges)
            changesList.add(changes)
            pendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
                changesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(holder!!.itemView, changer, removeDuration)
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList(pendingAdditions)
            additionsList.add(additions)
            pendingAdditions.clear()
            val adder = Runnable {
                var delay = addAnimations.size * ADDING_ANIMATION_DELAY
                additions.sortWith { o1, o2 -> o1.absoluteAdapterPosition - o2.absoluteAdapterPosition }
                for (holder in additions) {
                    animateAddImpl(holder, delay)
                    delay = minOf(delay + ADDING_ANIMATION_DELAY, ADDING_ANIMATION_DELAY_MAX)
                }
                additions.clear()
                additionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay = removeDuration + maxOf(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    override fun animateRemove(holder: ViewHolder): Boolean {
        resetAnimation(holder)
        pendingRemovals.add(holder)
        return true
    }

    private fun animateRemoveImpl(holder: ViewHolder) {
        val view = holder.itemView
        val animation = view.animate()
        removeAnimations.add(holder)
        animation.setDuration(removeDuration).alpha(0f).setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchRemoveStarting(holder)
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    view.alpha = 1f
                    dispatchRemoveFinished(holder)
                    removeAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            },
        ).start()
    }

    override fun animateAdd(holder: ViewHolder): Boolean {
        resetAnimation(holder)
        holder.itemView.alpha = 0f
        holder.itemView.translationY = addingTranslationY.toFloat()
        pendingAdditions.add(holder)
        return true
    }

    private fun animateAddImpl(
        holder: ViewHolder,
        delay: Long,
    ) {
        val view = holder.itemView
        val animation = view.animate()
        addAnimations.add(holder)
        animation.alpha(1f).setDuration(addDuration)
            .translationY(0f)
            .setStartDelay(delay)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchAddStarting(holder)
                }

                override fun onAnimationCancel(animator: Animator) {
                    view.alpha = 1f
                    view.translationY = 0f
                }

                override fun onAnimationEnd(animator: Animator) {
                    animation.setListener(null)
                    dispatchAddFinished(holder)
                    addAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
    }

    override fun animateMove(
        holder: ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int,
    ): Boolean {
        val view = holder.itemView
        val newFromX = fromX + view.translationX.toInt()
        val newFromY = fromY + view.translationY.toInt()
        resetAnimation(holder)
        val deltaX = toX - newFromX
        val deltaY = toY - newFromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            view.translationX = (-deltaX).toFloat()
        }
        if (deltaY != 0) {
            view.translationY = (-deltaY).toFloat()
        }
        pendingMoves.add(MoveInfo(holder, newFromX, newFromY, toX, toY))
        return true
    }

    private fun animateMoveImpl(
        holder: ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int,
    ) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            view.animate().translationX(0f)
        }
        if (deltaY != 0) {
            view.animate().translationY(0f)
        }
        val animation = view.animate()
        moveAnimations.add(holder)
        animation.setDuration(moveDuration).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animator: Animator) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(animator: Animator) {
                if (deltaX != 0) {
                    view.translationX = 0f
                }
                if (deltaY != 0) {
                    view.translationY = 0f
                }
            }

            override fun onAnimationEnd(animator: Animator) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                moveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    override fun animateChange(
        oldHolder: ViewHolder,
        newHolder: ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int,
    ): Boolean {
        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = oldHolder.itemView.translationX
        val prevTranslationY = oldHolder.itemView.translationY
        val prevAlpha = oldHolder.itemView.alpha
        resetAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        oldHolder.itemView.translationX = prevTranslationX
        oldHolder.itemView.translationY = prevTranslationY
        oldHolder.itemView.alpha = prevAlpha
        // carry over translation values
        resetAnimation(newHolder)
        newHolder.itemView.translationX = (-deltaX).toFloat()
        newHolder.itemView.translationY = (-deltaY).toFloat()
        newHolder.itemView.alpha = 0f
        pendingChanges.add(ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY))
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            val oldViewAnim = view.animate().setDuration(
                changeDuration,
            )
            changeAnimations.add(holder)
            oldViewAnim.translationX((changeInfo.toX - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((changeInfo.toY - changeInfo.fromY).toFloat())
            oldViewAnim.alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animator: Animator) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(animator: Animator) {
                    oldViewAnim.setListener(null)
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    dispatchChangeFinished(holder, true)
                    changeAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            val newViewAnimation = newView.animate()
            changeAnimations.add(newHolder)
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration)
                .alpha(1f).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animator: Animator) {
                        dispatchChangeStarting(newHolder, false)
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        newViewAnimation.setListener(null)
                        newView.alpha = 1f
                        newView.translationX = 0f
                        newView.translationY = 0f
                        dispatchChangeFinished(newHolder, false)
                        changeAnimations.remove(newHolder)
                        dispatchFinishedWhenDone()
                    }
                }).start()
        }
    }

    private fun endChangeAnimation(
        infoList: MutableList<ChangeInfo>,
        item: ViewHolder,
    ) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder!!)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder!!)
        }
    }

    private fun endChangeAnimationIfNecessary(
        changeInfo: ChangeInfo,
        item: ViewHolder,
    ): Boolean {
        var oldItem = false
        when {
            changeInfo.newHolder === item -> changeInfo.newHolder = null
            changeInfo.oldHolder === item -> {
                changeInfo.oldHolder = null
                oldItem = true
            }

            else -> return false
        }
        item.itemView.alpha = 1f
        item.itemView.translationX = 0f
        item.itemView.translationY = 0f
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        view.animate().cancel()
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (i in pendingMoves.indices.reversed()) {
            val moveInfo = pendingMoves[i]
            if (moveInfo.holder === item) {
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(item)
                pendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(pendingChanges, item)
        if (pendingRemovals.remove(item)) {
            view.alpha = 1f
            dispatchRemoveFinished(item)
        }
        if (pendingAdditions.remove(item)) {
            view.alpha = 1f
            dispatchAddFinished(item)
        }

        for (i in changesList.indices.reversed()) {
            val changes = changesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                changesList.removeAt(i)
            }
        }
        for (i in movesList.indices.reversed()) {
            val moves = movesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo = moves[j]
                if (moveInfo.holder === item) {
                    view.translationY = 0f
                    view.translationX = 0f
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        movesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in additionsList.indices.reversed()) {
            val additions = additionsList[i]
            if (additions.remove(item)) {
                view.alpha = 1f
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    additionsList.removeAt(i)
                }
            }
        }
        // animations should be ended by the cancel above.
        //noinspection PointlessBooleanExpression,ConstantConditions
        check(!DEBUG || !removeAnimations.remove(item)) { "after animation is cancelled, item should not be in removeAnimations list" }
        //noinspection PointlessBooleanExpression,ConstantConditions
        check(!DEBUG || !addAnimations.remove(item)) { "after animation is cancelled, item should not be in addAnimations list" }
        //noinspection PointlessBooleanExpression,ConstantConditions
        check(!DEBUG || !changeAnimations.remove(item)) { "after animation is cancelled, item should not be in changeAnimations list" }
        //noinspection PointlessBooleanExpression,ConstantConditions
        check(!DEBUG || !moveAnimations.remove(item)) { "after animation is cancelled, item should not be in moveAnimations list" }
        dispatchFinishedWhenDone()
    }

    private fun resetAnimation(holder: ViewHolder) {
        if (sDefaultInterpolator == null) {
            sDefaultInterpolator = ValueAnimator().interpolator
        }
        holder.itemView.animate().interpolator = sDefaultInterpolator
        endAnimation(holder)
    }

    override fun isRunning(): Boolean {
        return (
            pendingAdditions.isNotEmpty() ||
                pendingChanges.isNotEmpty() ||
                pendingMoves.isNotEmpty() ||
                pendingRemovals.isNotEmpty() ||
                moveAnimations.isNotEmpty() ||
                removeAnimations.isNotEmpty() ||
                addAnimations.isNotEmpty() ||
                changeAnimations.isNotEmpty() ||
                movesList.isNotEmpty() ||
                additionsList.isNotEmpty() ||
                changesList.isNotEmpty()
            )
    }

    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = pendingMoves.size
        for (i in count - 1 downTo 0) {
            val item = pendingMoves[i]
            val view = item.holder.itemView
            view.translationY = 0f
            view.translationX = 0f
            dispatchMoveFinished(item.holder)
            pendingMoves.removeAt(i)
        }
        count = pendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = pendingRemovals[i]
            dispatchRemoveFinished(item)
            pendingRemovals.removeAt(i)
        }
        count = pendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = pendingAdditions[i]
            item.itemView.alpha = 1f
            dispatchAddFinished(item)
            pendingAdditions.removeAt(i)
        }
        count = pendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(pendingChanges[i])
        }
        pendingChanges.clear()
        if (!isRunning) {
            return
        }

        var listCount = movesList.size
        for (i in listCount - 1 downTo 0) {
            val moves = movesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo = moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    movesList.remove(moves)
                }
            }
        }
        listCount = additionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = additionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                view.alpha = 1f
                dispatchAddFinished(item)
                additions.removeAt(j)
                if (additions.isEmpty()) {
                    additionsList.remove(additions)
                }
            }
        }
        listCount = changesList.size
        for (i in listCount - 1 downTo 0) {
            val changes = changesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    changesList.remove(changes)
                }
            }
        }
        cancelAll(removeAnimations)
        cancelAll(moveAnimations)
        cancelAll(addAnimations)
        cancelAll(changeAnimations)
        dispatchAnimationsFinished()
    }

    private fun cancelAll(viewHolders: List<ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            viewHolders[i].itemView.animate().cancel()
        }
    }

    /**
     * If the payload list is not empty, DefaultItemAnimator returns `true`.
     * When this is the case:
     *
     * * If you override [.animateChange], both
     * ViewHolder arguments will be the same instance.
     * * If you are not overriding [.animateChange],
     * then DefaultItemAnimator will call [.animateMove] and
     * run a move animation instead.
     */
    override fun canReuseUpdatedViewHolder(
        viewHolder: ViewHolder,
        payloads: List<Any>,
    ): Boolean {
        return payloads.isNotEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    companion object {
        private const val DEBUG = false
        private const val ADDING_ANIMATION_DELAY = 16L
        private const val ADDING_ANIMATION_DELAY_MAX = 500L
        private const val ADDING_TRANSLATION_Y = 200

        private var sDefaultInterpolator: TimeInterpolator? = null
    }
}
