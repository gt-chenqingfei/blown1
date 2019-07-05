package com.shuashuakan.android.js

import com.shuashuakan.android.js.Response.Companion.BUSINESS_ERROR_CODE
import com.shuashuakan.android.js.Response.Companion.METHOD_NOT_SUPPORTED_CODE
import com.shuashuakan.android.js.Response.Companion.PARAMS_ERROR_CODE
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResponseTest {

  private val request = Request(methodScope = "scope", methodId = "12id", methodName = "method",
      params = mapOf("a" to 1, "b" to "2"))

  @Test
  fun createResponse() {
    val re = Response.create(request, 100) {
      put("a", "a")
      put("b", 2)
    }
    assertResponse(re)
    assertThat(re.code).isEqualTo(100)
    assertThat(re.responseParams).hasSize(2)
    assertThat(re.responseParams).containsEntry("a", "a")
    assertThat(re.responseParams).containsEntry("b", 2)
  }

  @Test
  fun createResponseWithoutParams() {
    val re = Response.create(request, 100) {}
    assertResponse(re)
    assertThat(re.code).isEqualTo(100)
    assertThat(re.responseParams).isEmpty()
  }

  @Test
  fun successResponse() {
    val re = Response.success(request) {
      put("user", "abc")
      put("id", 12345L)
    }
    assertResponse(re)
    assertThat(re.code).isEqualTo(200)
    assertThat(re.responseParams).hasSize(2)
    assertThat(re.responseParams).containsEntry("user", "abc")
    assertThat(re.responseParams).containsEntry("id", 12345L)
  }

  @Test
  fun failedResponse() {
    val re = Response.failed(request, 403) {
      errorCode(40301)
      errorMessage("bad request")
    }
    assertResponse(re)
    assertThat(re.code).isEqualTo(403)
    assertThat(re.responseParams).hasSize(2)
    assertThat(re.responseParams).containsEntry("error_code", 40301)
    assertThat(re.responseParams).containsEntry("error_message", "bad request")
  }

  @Test
  fun methodNotSupportedResponse() {
    val re = Response.methodNotSupported(request, "233")
    assertResponse(re)
    assertThat(re.code).isEqualTo(METHOD_NOT_SUPPORTED_CODE)
    assertThat(re.responseParams).hasSize(1)
    assertThat(re.responseParams).containsEntry("error_message", "233")
  }

  @Test
  fun paramsErrorResponse() {
    val re = Response.paramsError(request, "error") {
      put("a", "b")
    }
    assertResponse(re)
    assertThat(re.code).isEqualTo(BUSINESS_ERROR_CODE)
    assertThat(re.responseParams).hasSize(3)
    assertThat(re.responseParams).containsEntry("error_code", PARAMS_ERROR_CODE)
    assertThat(re.responseParams).containsEntry("error_message", "error")
    assertThat(re.responseParams).containsEntry("a", "b")
  }

  private fun assertResponse(response: Response) {
    assertThat(response.methodScope).isEqualTo("scope")
    assertThat(response.methodId).isEqualTo("12id")
    assertThat(response.methodName).isEqualTo("method")
  }
}