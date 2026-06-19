package org.koitharu.kotatsu.parsers.site.pt

import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import org.json.JSONArray
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.parsers.MangaSourceParser
import org.koitharu.kotatsu.parsers.config.ConfigKey
import org.koitharu.kotatsu.parsers.core.PagedMangaParser
import org.koitharu.kotatsu.parsers.model.*
import org.koitharu.kotatsu.parsers.network.CommonHeaders
import org.koitharu.kotatsu.parsers.util.*
import org.koitharu.kotatsu.parsers.util.json.mapJSON
import java.security.MessageDigest
import java.util.Base64
import kotlin.random.Random

@MangaSourceParser("NEXUSTOONS", "Nexus Toons", "pt")
internal class NexusToons(context: MangaLoaderContext) :
    PagedMangaParser(context, MangaParserSource.NEXUSTOONS, 30) {

    override val configKeyDomain = ConfigKey.Domain("nexustoons.com")
    
    override fun getRequestHeaders() = super.getRequestHeaders().newBuilder()
        .add(CommonHeaders.ACCEPT, "application/json")
        .add(CommonHeaders.REFERER, "https://$domain/")
        .build()

    override val availableSortOrders: Set<SortOrder> = setOf(SortOrder.RELEVANCE)

    override val filterCapabilities = MangaListFilterCapabilities(isSearchSupported = true)

    override suspend fun getFilterOptions() = MangaListFilterOptions()

    private fun getApiUrlBuilder(): okhttp3.HttpUrl.Builder {
        return okhttp3.HttpUrl.Builder()
            .scheme("https")
            .host("nx-toons.xyz")
            .addPathSegment("api")
    }

    private suspend fun requestAPI(urlBuilder: okhttp3.HttpUrl.Builder): JSONObject {
        val request = urlBuilder.build()
        val response = webClient.httpGet(request)
        val bodyStr = response.body?.string() ?: ""

        val contentType = response.header("Content-Type") ?: ""
        if (!contentType.contains("application/json")) {
            throw Exception("Expected JSON, got: $contentType")
        }

        var decryptedBody = bodyStr
        try {
            val enc = JSONObject(bodyStr)
            if (enc.has("v") && (enc.getInt("v") == 1 || enc.getInt("v") == 2)) {
                val keyIndex = if (enc.getInt("v") == 1) 0 else enc.optInt("k", 0)
                decryptedBody = OrionCrypto.decrypt(keyIndex, enc.getString("d"), context)
            }
        } catch (e: Exception) {
            // Not encrypted or failed
        }

        return try {
            JSONObject(decryptedBody)
        } catch (e: Exception) {
            JSONObject()
        }
    }

    override suspend fun getListPage(page: Int, order: SortOrder, filter: MangaListFilter): List<Manga> {
        val url = getApiUrlBuilder().addPathSegment("mangas")
            .addQueryParameter("page", page.toString())
            .addQueryParameter("limit", "30")
            .addQueryParameter("includeNsfw", "true")

        if (!filter.query.isNullOrEmpty()) {
            url.addQueryParameter("search", filter.query)
        } else {
            url.addQueryParameter("sortBy", "views")
            url.addQueryParameter("sortOrder", "desc")
        }

        val json = requestAPI(url)
        val data = json.optJSONArray("data") ?: return emptyList()

        return data.mapJSON { manga ->
            val id = manga.getString("slug")
            val title = manga.getString("title")
            val coverUrl = manga.optString("coverImage", "")

            Manga(
                id = generateUid(id),
                title = title,
                altTitles = emptySet(),
                url = id,
                publicUrl = "https://$domain/manga/$id",
                rating = RATING_UNKNOWN,
                contentRating = if (manga.optBoolean("isNsfw", false)) ContentRating.ADULT else ContentRating.SAFE,
                coverUrl = coverUrl,
                tags = emptySet(),
                state = null,
                authors = emptySet(),
                largeCoverUrl = coverUrl,
                source = source
            )
        }
    }

    override suspend fun getDetails(manga: Manga): Manga = coroutineScope {
        val id = manga.url.removePrefix("/manga/").removePrefix("manga/").removePrefix("/")
        val url = getApiUrlBuilder().addPathSegment("manga").addPathSegment(id)
        val json = requestAPI(url)
        
        val chaptersList = json.optJSONArray("chapters") ?: JSONArray()
        val chapters = chaptersList.mapJSON { ch ->
            val chId = ch.get("id").toString()
            val chNum = ch.optString("number", "0")
            val chTitle = ch.optString("title", "").ifEmpty { "Capítulo ${chNum.removeSuffix(".0")}" }
            
            MangaChapter(
                id = generateUid(chId),
                title = chTitle,
                number = chNum.toFloatOrNull() ?: 0f,
                volume = 0,
                url = chId,
                scanlator = null,
                uploadDate = 0L,
                branch = null,
                source = source
            )
        }.sortedBy { it.number }.reversed() // Sort descending like typical mangas

        val altTitlesSet = mutableSetOf<String>()
        val altTitlesObj = json.opt("alternativeTitles")
        if (altTitlesObj is JSONArray) {
            for (i in 0 until altTitlesObj.length()) {
                val item = altTitlesObj.optJSONObject(i)
                if (item != null) {
                    val title = item.optString("title")
                    if (title.isNotEmpty()) altTitlesSet.add(title)
                } else {
                    val str = altTitlesObj.optString(i)
                    if (str.isNotEmpty()) altTitlesSet.add(str)
                }
            }
        } else if (altTitlesObj is String) {
            try {
                val array = JSONArray(altTitlesObj)
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i)
                    if (item != null) {
                        val title = item.optString("title")
                        if (title.isNotEmpty()) altTitlesSet.add(title)
                    }
                }
            } catch (e: Exception) {
                if (altTitlesObj.isNotEmpty()) {
                    altTitlesSet.addAll(altTitlesObj.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                }
            }
        }

        val authorStr = json.optString("author", "")
        val authorSet = if (authorStr.isNotEmpty()) setOf(authorStr) else emptySet()

        val tagsArray = json.optJSONArray("categories")
        val tagsSet = if (tagsArray != null) {
            val set = mutableSetOf<MangaTag>()
            for (i in 0 until tagsArray.length()) {
                val tagObj = tagsArray.optJSONObject(i)
                if (tagObj != null) {
                    set.add(MangaTag(title = tagObj.optString("name", "Unknown"), key = tagObj.optString("id", i.toString()), source = source))
                } else {
                    val tagStr = tagsArray.optString(i)
                    if (tagStr.isNotEmpty()) set.add(MangaTag(title = tagStr, key = tagStr, source = source))
                }
            }
            set
        } else emptySet()

        manga.copy(
            title = json.optString("title").ifEmpty { manga.title },
            coverUrl = json.optString("coverImage").ifEmpty { manga.coverUrl },
            largeCoverUrl = json.optString("coverImage").ifEmpty { manga.largeCoverUrl },
            description = json.optString("description", ""),
            tags = tagsSet,
            authors = authorSet,
            altTitles = altTitlesSet,
            chapters = chapters
        )
    }

    override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
        val url = getApiUrlBuilder().addPathSegment("read").addPathSegment(chapter.url)
        val json = requestAPI(url)
        val pages = json.optJSONArray("pages") ?: return emptyList()
        val pageToken = json.optString("pageToken", "")

        return pages.mapJSON { page ->
            var imageUrl = page.optString("imageUrl", "")
            val index = page.optInt("index", 0)
            
            if (imageUrl.isEmpty()) {
                imageUrl = "https://nx-toons.xyz/api/p/$pageToken/$index"
            } else if (imageUrl.startsWith("/api")) {
                imageUrl = "https://nx-toons.xyz$imageUrl"
            }

            MangaPage(
                id = generateUid(imageUrl),
                url = imageUrl,
                preview = null,
                source = source
            )
        }
    }

    // ======================= CRYPTO UTILS =======================
    private object OrionCrypto {
        private const val NUM_KEYS = 5
        private const val CRYPTO_SECRET = "OrionNexus2025CryptoKey!Secure"

        private class KeyData(val key: IntArray, val rsbox: IntArray)

        private val keys by lazy {
            Array(NUM_KEYS) { i ->
                val pattern = "_orion_key_${i}_v2_$CRYPTO_SECRET"
                val md = MessageDigest.getInstance("SHA-256")
                val hashBytes = md.digest(pattern.toByteArray(Charsets.UTF_8))
                val key = hashBytes.map { it.toInt() and 0xFF }.toIntArray()

                val sbox = IntArray(256) { it }
                var j = 0
                for (k in 0 until 256) {
                    j = (j + sbox[k] + key[k % key.size]) % 256
                    val temp = sbox[k]
                    sbox[k] = sbox[j]
                    sbox[j] = temp
                }
                
                val rsbox = IntArray(256)
                for (k in 0 until 256) {
                    rsbox[sbox[k]] = k
                }
                KeyData(key, rsbox)
            }
        }

        private fun rotateRight(byte: Int, shift: Int): Int {
            val s = shift % 8
            return ((byte ushr s) or (byte shl (8 - s))) and 0xFF
        }

        fun decrypt(keyIndex: Int, base64Data: String, context: MangaLoaderContext): String {
            if (keyIndex < 0 || keyIndex >= NUM_KEYS) return ""

            val keyData = keys[keyIndex]
            val key = keyData.key
            val rsbox = keyData.rsbox

            val inputBytes = context.decodeBase64(base64Data)
            val input = inputBytes.map { it.toInt() and 0xFF }.toIntArray()
            val output = ByteArray(input.size)
            val keyLen = key.size

            for (i in input.indices.reversed()) {
                var byte = input[i]
                byte = if (i > 0) byte xor input[i - 1] else byte xor key[keyLen - 1]
                byte = rsbox[byte]

                val rotAmount = ((key[(i + 3) % keyLen] + (i and 0xFF)) and 0xFF) % 7 + 1
                byte = rotateRight(byte, rotAmount)
                byte = byte xor key[i % keyLen]
                output[i] = byte.toByte()
            }
            return String(output, Charsets.UTF_8)
        }
    }
}
