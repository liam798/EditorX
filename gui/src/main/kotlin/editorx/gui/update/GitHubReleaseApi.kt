package editorx.gui.update

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object GitHubReleaseApi {
    private val logger = LoggerFactory.getLogger(GitHubReleaseApi::class.java)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val http = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    data class Release(
        val tagName: String,
        val name: String?,
        val body: String?,
        val htmlUrl: String?,
        val publishedAt: String?,
        val assets: List<Asset>,
    )

    data class Asset(
        val name: String,
        val browserDownloadUrl: String,
        val size: Long?,
        val contentType: String?,
    )

    fun fetchLatestRelease(repo: String): Release? {
        val url = "https://api.github.com/repos/$repo/releases/latest"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "EditorX")
            .GET()
            .build()

        val resp = runCatching { http.send(request, HttpResponse.BodyHandlers.ofString()) }
            .onFailure { e -> logger.warn("检查更新失败：网络请求异常", e) }
            .getOrNull()
            ?: return null

        if (resp.statusCode() !in 200..299) {
            logger.info("检查更新返回非 2xx：status={}, body={}", resp.statusCode(), resp.body().take(2000))
            return null
        }

        val root = runCatching { json.parseToJsonElement(resp.body()) }
            .onFailure { e -> logger.warn("检查更新失败：解析 JSON 失败", e) }
            .getOrNull()
            ?: return null

        return parseRelease(root)
    }

    private fun parseRelease(root: JsonElement): Release? {
        val obj = root.safeObj() ?: return null
        val tagName = obj.str("tag_name")?.trim().orEmpty()
        if (tagName.isEmpty()) return null

        val assets = obj.arr("assets")
            ?.mapNotNull { parseAsset(it) }
            ?: emptyList()

        return Release(
            tagName = tagName,
            name = obj.str("name"),
            body = obj.str("body"),
            htmlUrl = obj.str("html_url"),
            publishedAt = obj.str("published_at"),
            assets = assets,
        )
    }

    private fun parseAsset(el: JsonElement): Asset? {
        val obj = el.safeObj() ?: return null
        val name = obj.str("name")?.trim().orEmpty()
        val url = obj.str("browser_download_url")?.trim().orEmpty()
        if (name.isEmpty() || url.isEmpty()) return null
        return Asset(
            name = name,
            browserDownloadUrl = url,
            size = obj.long("size"),
            contentType = obj.str("content_type"),
        )
    }

    private fun JsonElement.safeObj(): JsonObject? = (this as? JsonObject) ?: runCatching { this.jsonObject }.getOrNull()

    private fun JsonObject.str(key: String): String? {
        val p = this[key] as? JsonPrimitive ?: return null
        if (p is JsonNull) return null
        val s = p.content
        if (s == "null") return null
        return s
    }

    private fun JsonObject.long(key: String): Long? {
        val p = this[key] as? JsonPrimitive ?: return null
        if (p is JsonNull) return null
        return p.content.toLongOrNull()
    }

    private fun JsonObject.arr(key: String): List<JsonElement>? = (this[key] as? JsonArray)?.toList()
}
