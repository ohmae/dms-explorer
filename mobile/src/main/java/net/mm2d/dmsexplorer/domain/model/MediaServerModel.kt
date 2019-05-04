/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.model

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.IntDef
import io.reactivex.android.schedulers.AndroidSchedulers
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.android.upnp.cds.MediaServer
import net.mm2d.dmsexplorer.domain.entity.ContentDirectoryEntity
import net.mm2d.dmsexplorer.domain.entity.ContentEntity
import java.util.*

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class MediaServerModel(
    context: Context,
    val mediaServer: MediaServer,
    private val playbackTargetObserver: PlaybackTargetObserver
) : ExploreListener {
    private val handler = Handler(Looper.getMainLooper())
    private val historyStack = LinkedList<ContentDirectoryEntity>()
    @Volatile
    private var exploreListener: ExploreListener = EXPLORE_LISTENER

    var path: String? = null
        private set
    val udn: String
        get() = mediaServer.udn
    val title: String
        get() = mediaServer.friendlyName
    val selectedEntity: ContentEntity?
        get() {
            val entry = historyStack.peekFirst() ?: return null
            return entry.selectedEntity
        }

    fun initialize() {
        prepareEntry(ContentDirectoryEntity())
    }

    fun terminate() {
        setExploreListener(null)
        for (directory in historyStack) {
            directory.terminate()
        }
        historyStack.clear()
    }

    fun canDelete(entity: ContentEntity): Boolean {
        if (!entity.canDelete()) {
            return false
        }
        if (historyStack.size >= 2) {
            val p = historyStack[1].selectedEntity
            if (p != null && !p.canDelete()) {
                return false
            }
        }
        return mediaServer.hasDestroyObject()
    }

    @SuppressLint("CheckResult")
    fun delete(
        entity: ContentEntity,
        successCallback: Runnable?,
        errorCallback: Runnable?
    ) {
        val doNothing = Runnable { }
        val onSuccess = successCallback ?: doNothing
        val onError = errorCallback ?: doNothing
        val id = (entity.getObject() as CdsObject).objectId
        mediaServer.destroyObject(id)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ integer ->
                if (integer == MediaServer.NO_ERROR) {
                    onSuccess.run()
                    reload()
                    return@subscribe
                }
                onError.run()
            }, { onError.run() })
    }

    fun enterChild(entity: ContentEntity): Boolean {
        val directory = historyStack.peekFirst() ?: return false
        val child = directory.enterChild(entity) ?: return false
        directory.setExploreListener(null)
        prepareEntry(child)
        updatePlaybackTarget()
        return true
    }

    private fun prepareEntry(directory: ContentDirectoryEntity) {
        historyStack.offerFirst(directory)
        path = makePath()
        directory.setExploreListener(this)
        directory.clearState()
        directory.startBrowse(mediaServer.browse(directory.parentId))
    }

    fun exitToParent(): Boolean {
        if (historyStack.size < 2) {
            return false
        }
        exploreListener.onStart()

        handler.post {
            val directory = historyStack.pollFirst() ?: return@post
            directory.terminate()
            path = makePath()
            val parent = historyStack.peekFirst() ?: return@post
            parent.setExploreListener(this)
            updatePlaybackTarget()
            exploreListener.onUpdate(parent.entities)
            exploreListener.onComplete()
        }
        return true
    }

    fun reload() {
        val directory = historyStack.peekFirst() ?: return
        directory.clearState()
        directory.startBrowse(mediaServer.browse(directory.parentId))
    }

    fun setExploreListener(listener: ExploreListener?) {
        exploreListener = listener ?: EXPLORE_LISTENER
        val directory = historyStack.peekFirst() ?: return
        exploreListener.onStart()
        exploreListener.onUpdate(directory.entities)
        if (!directory.isInProgress) {
            exploreListener.onComplete()
        }
    }

    fun setSelectedEntity(entity: ContentEntity): Boolean {
        val directory = historyStack.peekFirst() ?: return false
        directory.selectedEntity = entity
        updatePlaybackTarget()
        return true
    }

    private fun updatePlaybackTarget() {
        playbackTargetObserver.update(selectedEntity)
    }

    fun selectPreviousEntity(@ScanMode scanMode: Int): Boolean {
        val nextEntity = findPrevious(selectedEntity, scanMode) ?: return false
        return setSelectedEntity(nextEntity)
    }

    private fun findPrevious(
        current: ContentEntity?,
        @ScanMode scanMode: Int
    ): ContentEntity? {
        val directory = historyStack.peekFirst()
        if (current == null || directory == null) {
            return null
        }
        val list = directory.entities
        when (scanMode) {
            SCAN_MODE_SEQUENTIAL -> return findPreviousSequential(current, list)
            SCAN_MODE_LOOP -> return findPreviousLoop(current, list)
        }
        return null
    }

    private fun findPreviousSequential(
        current: ContentEntity,
        list: List<ContentEntity>
    ): ContentEntity? {
        val index = list.indexOf(current)
        if (index - 1 < 0) {
            return null
        }
        for (i in index - 1 downTo 0) {
            val target = list[i]
            if (isValidEntity(current, target)) {
                return target
            }
        }
        return null
    }

    private fun findPreviousLoop(
        current: ContentEntity,
        list: List<ContentEntity>
    ): ContentEntity? {
        val size = list.size
        val index = list.indexOf(current)
        var i = (size + index - 1) % size
        while (i != index) {
            val target = list[i]
            if (isValidEntity(current, target)) {
                return target
            }
            i = (size + i - 1) % size
        }
        return null
    }

    fun selectNextEntity(@ScanMode scanMode: Int): Boolean {
        val nextEntity = findNext(selectedEntity, scanMode) ?: return false
        return setSelectedEntity(nextEntity)
    }

    private fun findNext(
        current: ContentEntity?,
        @ScanMode scanMode: Int
    ): ContentEntity? {
        if (historyStack.isEmpty()) {
            return null
        }
        val list = historyStack.peekFirst().entities
        if (current == null) {
            return null
        }
        when (scanMode) {
            SCAN_MODE_SEQUENTIAL -> return findNextSequential(current, list)
            SCAN_MODE_LOOP -> return findNextLoop(current, list)
        }
        return null
    }

    private fun findNextSequential(
        current: ContentEntity,
        list: List<ContentEntity>
    ): ContentEntity? {
        val size = list.size
        val index = list.indexOf(current)
        if (index + 1 == size) {
            return null
        }
        for (i in index + 1 until size) {
            val target = list[i]
            if (isValidEntity(current, target)) {
                return target
            }
        }
        return null
    }

    private fun findNextLoop(
        current: ContentEntity,
        list: List<ContentEntity>
    ): ContentEntity? {
        val size = list.size
        val index = list.indexOf(current)
        var i = (index + 1) % size
        while (i != index) {
            val target = list[i]
            if (isValidEntity(current, target)) {
                return target
            }
            i = (i + 1) % size
        }
        return null
    }

    private fun isValidEntity(current: ContentEntity, target: ContentEntity): Boolean {
        return (target.type == current.type && target.hasResource() && !target.isProtected)
    }

    private fun makePath(): String {
        val sb = StringBuilder()
        for (directory in historyStack) {
            if (sb.isNotEmpty() && directory.parentName.isNotEmpty()) {
                sb.append(DELIMITER)
            }
            sb.append(directory.parentName)
        }
        return sb.toString()
    }

    override fun onStart() {
        exploreListener.onStart()
    }

    override fun onUpdate(list: List<ContentEntity>) {
        exploreListener.onUpdate(list)
    }

    override fun onComplete() {
        exploreListener.onComplete()
    }

    interface PlaybackTargetObserver {
        fun update(entity: ContentEntity?)
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(SCAN_MODE_SEQUENTIAL, SCAN_MODE_LOOP)
    annotation class ScanMode

    companion object {
        const val SCAN_MODE_SEQUENTIAL = 0
        const val SCAN_MODE_LOOP = 1
        private const val DELIMITER = " < "
        private val EXPLORE_LISTENER = ExploreListenerAdapter()
    }
}
