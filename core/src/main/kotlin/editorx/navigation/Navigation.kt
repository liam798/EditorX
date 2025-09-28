package editorx.navigation

import editorx.vfs.VirtualFile

/**
 * Lightweight navigation target: a file and an offset to place the caret at.
 */
data class NavigationTarget(
    val file: VirtualFile,
    val offset: Int,
)

/**
 * Provider for navigation/jump logic (e.g., Go to Definition).
 */
interface NavigationProvider {
    /** Whether this provider can handle the given file. */
    fun supports(file: VirtualFile): Boolean

    /**
     * Compute navigation target for given file and caret offset.
     * Return null if no target is applicable.
     */
    fun gotoDefinition(file: VirtualFile, offset: Int, contents: String): NavigationTarget?
}

object NavigationRegistry {
    private val providers = mutableListOf<NavigationProvider>()

    @Synchronized
    fun register(provider: NavigationProvider) {
        providers.add(provider)
    }

    fun findFirstForFile(file: VirtualFile): NavigationProvider? =
        providers.firstOrNull { it.supports(file) }
}

