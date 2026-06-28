package com.bvue.domain.model

/** A crowd-sourced skippable segment of a video (times in milliseconds). */
data class SponsorSegment(
    val category: String,
    val startMs: Long,
    val endMs: Long,
)

/** SponsorBlock categories we support, with their API names and friendly labels. */
enum class SponsorCategory(val apiName: String, val label: String) {
    SPONSOR("sponsor", "Sponsor"),
    SELF_PROMO("selfpromo", "Self-promotion"),
    INTRO("intro", "Intro / intermission"),
    OUTRO("outro", "Endcards / outro"),
    INTERACTION("interaction", "Interaction reminder"),
    ;

    companion object {
        /** Skipped by default: sponsor + self-promotion (least surprising). */
        val DEFAULT: Set<String> = setOf(SPONSOR.apiName, SELF_PROMO.apiName)
    }
}
