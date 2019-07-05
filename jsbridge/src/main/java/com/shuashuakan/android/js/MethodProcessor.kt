package com.shuashuakan.android.js

import android.support.annotation.MainThread


/**
 * 处理 js 方法的 processor。
 * 使用时需要通过 {@link RainbowBridge.Builder#addMethodProcessor(MethodProcessor)} 来注册该 processor
 */
interface MethodProcessor {

  /**
   * 根据给定的 `request` 来判断是否支持该 request，一般情况下就是判断 [Request.methodName], [ ][Request.methodScope]
   * 如果返回 true，表示可以处理该 request，接下来的调用顺序为：
   * 1. [.beforeResponseProceed]
   * 2. [.process]
   * 3. [.afterResponseProceed]
   *
   * 如果返回 false，表示处理不了该 request，[RainbowBridge] 会继续查找下一个 processor
   *
   * @param request js 传递过来的参数，包含方法名，方法域，及具体参数
   * @return 返回 true 话来接收该 request
   */
  @MainThread
  fun canHandleRequest(request: Request): Boolean

  /**
   * 根据 js 的`request` 返回具体的 response，不可为 null
   */
  @MainThread
  fun process(request: Request): Response

  /**
   * [RainbowBridge] 在调用 [.process] 之前会调用该方法
   *
   * @see {@link .canHandleRequest
   */
  @MainThread
  fun beforeResponseProceed(request: Request) {
  }

  /**
   * [RainbowBridge] 在调用 [.process] 之后会调用该方法
   *
   * @see {@link .canHandleRequest
   */
  @MainThread
  fun afterResponseProceed(request: Request, response: Response) {
  }
}