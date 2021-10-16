package me.roybailey.research.kotlin.guava

import com.google.common.base.Optional
import com.google.common.cache.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit


/**
 * Test samples taken from https://www.baeldung.com/guava-cache
 * Guava cache is thread-safe
 * you can insert values manually into the cache using put(key,value)
 * you can measure your cache performance using CacheStats ( hitRate(), missRate(), ..)
 */
class GuavaCachingTest {

    @Test
    fun testGuavaCache() {

        val cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(CacheLoader.from { key: String -> key.length })

        assertThat(cache.get("1")).isEqualTo(1)
        assertThat(cache.get("12")).isEqualTo(2)
        assertThat(cache.get("123")).isEqualTo(3)
        assertThat(cache.get("1234")).isEqualTo(4)
    }

    @Test
    fun whenCacheMiss_thenValueIsComputed() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder().build(loader)
        assertThat(cache.size()).isEqualTo(0)
        assertThat(cache.getUnchecked("hello")).isEqualTo("HELLO")
        assertThat(cache.size()).isEqualTo(1)
    }

    @Test
    fun whenCacheReachMaxSize_thenEviction() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder().maximumSize(3).build(loader)
        cache.getUnchecked("first")
        cache.getUnchecked("second")
        cache.getUnchecked("third")
        cache.getUnchecked("forth")
        assertThat(cache.size()).isEqualTo(3)
        assertThat(cache.getIfPresent("first")).isNull()
        assertThat(cache.getIfPresent("forth")).isEqualTo("FORTH")
    }

    @Test
    fun whenCacheReachMaxWeight_thenEviction() {
        val loader = CacheLoader.from {
            key: String -> key.toUpperCase()
        }
        val weighByLength: Weigher<String, String> = Weigher<String, String> { key, value -> value.length }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .maximumWeight(16)
            .weigher(weighByLength)
            .build(loader)
        cache.getUnchecked("first")
        cache.getUnchecked("second")
        cache.getUnchecked("third")
        cache.getUnchecked("last")
        assertThat(cache.size()).isEqualTo(3)
        assertThat(cache.getIfPresent("first")).isNull()
        assertThat(cache.getIfPresent("last")).isEqualTo("LAST")
    }

    @Test
    @Throws(InterruptedException::class)
    fun whenEntryIdle_thenEviction() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MILLISECONDS)
            .build(loader)
        cache.getUnchecked("hello")
        assertThat(cache.size()).isEqualTo(1)
        cache.getUnchecked("hello")
        Thread.sleep(300)
        cache.getUnchecked("test")
        assertThat(cache.size()).isEqualTo(1)
        assertThat(cache.getIfPresent("hello")).isNull()
    }

    @Test
    @Throws(InterruptedException::class)
    fun whenEntryLiveTimeExpire_thenEviction() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MILLISECONDS)
            .build(loader)
        cache.getUnchecked("hello")
        assertThat(cache.size()).isEqualTo(1)
        Thread.sleep(300)
        cache.getUnchecked("test")
        assertThat(cache.size()).isEqualTo(1)
        assertThat(cache.getIfPresent("hello")).isNull()
    }

    @Test
    fun whenWeakKeyHasNoRef_thenRemoveFromCache() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .weakKeys()
            .build(loader)
    }

    @Test
    fun whenSoftValue_thenRemoveFromCache() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .softValues()
            .build(loader)
    }

    @Test
    fun whenNullValue_thenOptional() {
        val loader: CacheLoader<String, Optional<String>>
        loader = object : CacheLoader<String, Optional<String>>() {
            override fun load(key: String): Optional<String>? {
                return Optional.fromNullable(getSuffix(key))
            }
        }
        val cache: LoadingCache<String, Optional<String>> = CacheBuilder.newBuilder().build(loader)
        assertThat(cache.getUnchecked("text.txt").get()).isEqualTo("txt")
        assertThat(cache.getUnchecked("hello").isPresent).isFalse
    }

    private fun getSuffix(str: String): String? {
        val lastIndex = str.lastIndexOf('.')
        return if (lastIndex == -1) {
            null
        } else str.substring(lastIndex + 1)
    }

    @Test
    fun whenLiveTimeEnd_thenRefresh() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .refreshAfterWrite(1, TimeUnit.MINUTES)
            .build(loader)
    }

    @Test
    fun whenPreloadCache_thenUsePutAll() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder().build(loader)
        val map: MutableMap<String, String> = HashMap()
        map["first"] = "FIRST"
        map["second"] = "SECOND"
        cache.putAll(map)
        assertThat(cache.size()).isEqualTo(2)
    }

    @Test
    fun whenEntryRemovedFromCache_thenNotify() {
        val loader: CacheLoader<String, String>
        loader = object : CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return key.toUpperCase()
            }
        }
        val listener: RemovalListener<String, String>
        listener = object : RemovalListener<String, String> {
            override fun onRemoval(n: RemovalNotification<String, String>) {
                if (n.wasEvicted()) {
                    val cause = n.cause.name
                    assertThat(cause).isEqualTo(RemovalCause.SIZE.toString())
                }
            }
        }
        val cache: LoadingCache<String, String> = CacheBuilder.newBuilder()
            .maximumSize(3)
            .removalListener(listener)
            .build(loader)
        cache.getUnchecked("first")
        cache.getUnchecked("second")
        cache.getUnchecked("third")
        cache.getUnchecked("last")
        assertThat(cache.size()).isEqualTo(3)
    }
}
