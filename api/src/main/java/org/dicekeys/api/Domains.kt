package org.dicekeys.api


import java.net.URL
import kotlin.math.min


val domainRegexp = Regex("(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]")

fun isValidDomainSyntax(candidate: String): Boolean {
    return domainRegexp.matches(candidate)
}

fun isValidDomainOrWildcardDomain(candidate: String): Boolean =
    // Is valid wildcard domain
    ( candidate.startsWith("*.") && isValidDomainSyntax(candidate.substring(2)) ) ||
    // Is valid non-wildcard domain
      isValidDomainSyntax(candidate)



fun removeWildcardPrefixIfPresent(s: String): String = if(s.startsWith("*.")) s.substring(2) else s

fun extractDomainIfWebUrl(candidateUrl: String?): String? {
    try {
        val url = URL(candidateUrl)
        if((url.protocol == "http" || url.protocol == "https") && isValidDomainOrWildcardDomain(url.host)){
            return url.host
        }
    } catch(e: Exception) {
        // Return null if not a valid URL
    }
    return null
}

fun isWebUrl(candidate: String): Boolean = !extractDomainIfWebUrl(candidate).isNullOrBlank()

/**
 * Determine the maximum number of labels at the suffix of a domain
 * that match a public suffix.
 *
 * @param domain A domain name
 */
fun getDepthOfPublicSuffix(domain: String): Int{
    val labelsFromTopToBottom = domain.split(".").reversed()
    var depth = 0;
    var depthOfPublicSuffix = 0;
    var domainNode: DomainNode? = rootNode

    while (depth < labelsFromTopToBottom.size && domainNode != null) {
        if (domainNode.isTerminalNode) {
            depthOfPublicSuffix = depth
        }
        domainNode = domainNode.nodes[labelsFromTopToBottom[depth++]]
    }

    return depthOfPublicSuffix
}

/**
 * Given a string that may be an HTTP(s) URL or a domain name
 * extract the domain name if it is a URL or just return it if
 * it is not a URL.
 *
 * @param domainOrUrl A domain name or an HTTP(s) URL.
 */
fun getDomainFromDomainOrUrlString(domainOrUrl: String): String?{
    var domainOrUrl = domainOrUrl
    if (domainOrUrl.startsWith("*.")) {
        domainOrUrl = domainOrUrl.substring(2);
    }
    try {
        val url = URL(if(domainOrUrl.indexOf(":") != -1) domainOrUrl else "https://${domainOrUrl}/")
        if (
            (url.protocol == "http:" || url.protocol == "https:" || url.protocol === "mailto:") &&
            getDepthOfPublicSuffix(url.host) > 0
        ) {
            return if(isValidDomainSyntax(url.host)) url.host else null

        }
    } catch(e: Exception) {}
    return null;
}

fun getRegisteredDomainFromValidNonwildcardDomain(domain: String): String{
    val labels = domain.split(".");
    val depthOfPublicSuffix = getDepthOfPublicSuffix(domain);
    // The registered domain will have one label in addition to the public suffix
    // of the registry.  Hence, for the public suffix of "com" with 1 label,
    // "dicekeys.com", the registered domain, will ha ve 2 labels.
    val depthOfRegisteredSuffix = min(depthOfPublicSuffix + 1, labels.size);
    // create the registered domain by joining the labels to the correct depth.
    return labels.subList(labels.size - depthOfRegisteredSuffix, labels.size).joinToString(".")
}

/**
 * Given a string that may be an HTTP(s) URL, return a wildcard
 * of the registered hostname.  If the string is not a valid
 * web URL, return null.
 *
 * For example, https://vault.bitwarden.com/stuff is a URL with a host
 * that is a subdomain of bitwarden.com, and Bitwarden is the domain
 * registered with the .com registry.  Therefore, this function
 * would return "*.bitwarden.com".
 *
 * @param domainOrUrl A domain name or an HTTP(s) URL.
 */
fun getWildcardOfRegisteredDomainFromCandidateWebUrl(candidateUrl: String?): String? {
    val domain = extractDomainIfWebUrl(candidateUrl);
    return domain?.let { domain ->
        // Return the domain with prefixed with '*.' to indicate it's a wildcard
        "*.${getRegisteredDomainFromValidNonwildcardDomain(domain)}"
    }
}

