package net.mm2d.dmsexplorer.settings

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
enum class SortKey {
    NONE,
    NAME,
    DATE,
    ;

    companion object {
        private val map = values().map { it.name to it }.toMap()
        fun of(name: String): SortKey =
            map.getOrElse(name) { NONE }
    }
}
