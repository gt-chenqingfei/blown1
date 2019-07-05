package com.shuashuakan.android.commons.cache


//@RunWith(RobolectricTestRunner::class)
class DiskCacheTest {
//  @Mock lateinit var context: Context
//
//  private lateinit var diskCache: DiskCache
//
//  @Before
//  fun setup() {
//    initMocks(this)
//    val cache = File("build/cache")
//    if (!cache.exists()) {
//      cache.mkdirs()
//    }
//    Mockito.`when`(context.cacheDir).thenReturn(cache)
//    val factory = MoshiValueConverter.Factory(Moshi.Builder().build())
//    diskCache = DiskCache(context, 1, "test.cache", factory)
//  }
//
//  @Test
//  fun testCache() {
//    val cache: Cache<User> = diskCache.cacheOf()
//    val key = "user"
//    val value = User(name = "android", channelId = "abc")
//    assertThat(cache.contains(key)).isFalse()
//    cache.put(key, value)
//    assertThat(cache.contains(key)).isTrue()
//    assertThat(cache.get(key).orNull()).isEqualTo(value)
//    cache.remove(key)
//    assertThat(cache.contains(key)).isFalse()
//  }
//
//  @Test fun collectionCache() {
//    val cache: Cache<List<User>> = diskCache.cacheOf(userListType)
//    val key = "users"
//    val user1 = User(name = "os", channelId = "123")
//    val user2= User(name = "android", channelId = "abc")
//    val value = listOf(user1, user2)
//    assertThat(cache.contains(key)).isFalse()
//    cache.put(key, value)
//    assertThat(cache.contains(key)).isTrue()
//    assertThat(cache.get(key).orNull()).hasSize(2)
//    assertThat(cache.get(key).orNull()).contains(user1)
//    assertThat(cache.get(key).orNull()).contains(user2)
//    cache.remove(key)
//    assertThat(cache.contains(key)).isFalse()
//  }

}