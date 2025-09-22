package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionCompletionHandler
import platform.Foundation.NSError
import platform.Foundation.NSURL

internal class WebSessionFlow(
    private val ephemeralBrowserSession: Boolean,
) {
    private var pendingSession: ASWebAuthenticationSession? = null
    private var pendingSessionContinuation: CancellableContinuation<Url?>? = null

    /**
     * @return null if user cancelled the flow (closed the web view)
     */
    internal suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): Url? {
        return suspendCancellableCoroutine { continuation ->
            val nsurl = NSURL.URLWithString(requestUrl.toString())
            if (nsurl != null) {
                pendingSessionContinuation = continuation
                val session = ASWebAuthenticationSession(
                    uRL = nsurl,
                    callbackURLScheme = Url(redirectUrl).protocol.name,
                    completionHandler = object : ASWebAuthenticationSessionCompletionHandler {
                        override fun invoke(p1: NSURL?, p2: NSError?) {
                            if (p1 != null) {
                                val url = Url(p1.toString()) // use sane url instead of NS garbage
                                pendingSession = null
                                pendingSessionContinuation = null
                                continuation.resumeIfActive(url)
                            } else {
                                // browser closed, no redirect.
                                if (pendingSessionContinuation == null) {
                                    // already handled by another session
                                    return
                                }
                                // regular cancel
                                pendingSessionContinuation = null
                                pendingSession = null
                                continuation.resumeIfActive(null)
                            }
                        }
                    }
                )
                pendingSession = session
                session.prefersEphemeralWebBrowserSession = ephemeralBrowserSession
                session.presentationContextProvider = PresentationContext()

                MainScope().launch {
                    session.start()
                }
            } else {
                continuation.resumeWithExceptionIfActive(OpenIdConnectException.InvalidUrl(requestUrl.toString()))
            }
        }
    }

    fun handleUrl(url: String) {
        pendingSessionContinuation?.resumeIfActive(Url(url))
        pendingSessionContinuation = null
        pendingSession?.cancel()
        pendingSession = null

    }
}