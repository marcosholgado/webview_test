package com.ddg.android.extension

import android.net.Uri

val IP_REGEX = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(:[0-9]+)?$")

/**
 * Returns the host without the www. subdomain
 */
val Uri.baseHost: String?
  get() = withScheme().host?.removePrefix("www.")

fun Uri.withScheme(): Uri {
  // Uri.parse function falsely parses IP:PORT string.
  // For example if input is "255.255.255.255:9999", it falsely flags 255.255.255.255 as the scheme.
  // Therefore in the withScheme method, we need to parse it after manually inserting "http".
  if (scheme == null || scheme!!.matches(IP_REGEX)) {
    return Uri.parse("http://${toString()}")
  }

  return this
}
