/*
 * Copyright (c) 2017 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.domain.entity

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.mm2d.android.upnp.cds.CdsObject
import net.mm2d.dmsexplorer.domain.model.ExploreListener
import net.mm2d.dmsexplorer.domain.model.ExploreListenerAdapter
import net.mm2d.dmsexplorer.settings.Settings
import net.mm2d.dmsexplorer.settings.SortKey.*
import net.mm2d.log.Logger

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
class ContentDirectoryEntity private constructor(
    val parentId: String,
    override val parentName: String
) : DirectoryEntity {
    private val list = mutableListOf<ContentEntity>()
    private var sortedList = emptyList<ContentEntity>()
    private var entryListener: ExploreListener = ENTRY_LISTENER
    @Volatile
    override var isInProgress = true
        private set
    @Volatile
    private var disposable: Disposable? = null

    override val entities: List<ContentEntity>
        get() = sortedList

    private var _selectedEntity: ContentEntity? = null
    override var selectedEntity: ContentEntity?
        get() = _selectedEntity
        set(entity) {
            if (entity == null || !list.contains(entity)) {
                _selectedEntity = null
                return
            }
            _selectedEntity = entity
        }

    constructor() : this(ROOT_OBJECT_ID, ROOT_TITLE)

    fun terminate() {
        setExploreListener(null)
        disposable?.dispose()
        disposable = null
    }

    fun enterChild(entity: ContentEntity): ContentDirectoryEntity? {
        if (!list.contains(entity)) {
            return null
        }
        if (entity.type != ContentType.CONTAINER) {
            return null
        }
        _selectedEntity = entity
        val cdsObject = entity.cdsObject as CdsObject
        return ContentDirectoryEntity(cdsObject.objectId, cdsObject.title)
    }

    fun setExploreListener(listener: ExploreListener?) {
        entryListener = listener ?: ENTRY_LISTENER
    }

    fun clearState() {
        disposable?.dispose()
        disposable = null
        _selectedEntity = null
        isInProgress = true
        list.clear()
        sortedList = emptyList()
        entryListener.onStart()
    }

    fun startBrowse(observable: Observable<CdsObject>) {
        entryListener.onStart()
        disposable = observable
            .subscribeOn(Schedulers.io())
            .subscribe({ cdsObject ->
                list.add(CdsContentEntity(cdsObject))
                sort()
                entryListener.onUpdate(sortedList)
            }, {
                Logger.w(it)
            }, {
                isInProgress = false
                entryListener.onComplete()
            })
    }

    private fun sort() {
        val settings = Settings.get()
        val isAscending = settings.isAscendingSortOrder
        sortedList = when (settings.sortKey) {
            NONE -> list.toList()
            NAME -> {
                if (isAscending) list.sortedBy { it.name }
                else list.sortedByDescending { it.name }
            }
            DATE -> {
                if (isAscending) list.sortedBy { it.date }
                else list.sortedByDescending { it.date }
            }
        }
    }

    fun onUpdateSortSettings() {
        Completable.fromAction {
            sort()
            entryListener.onUpdate(sortedList)
        }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    companion object {
        private const val ROOT_OBJECT_ID = "0"
        private const val ROOT_TITLE = ""
        private val ENTRY_LISTENER: ExploreListener = ExploreListenerAdapter()
    }
}
