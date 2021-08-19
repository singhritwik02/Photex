package com.ritwik.photex

import android.net.Uri
import android.os.Bundle
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase

class Referral() {
    fun createReferralLink(): String {
        var domainUriPrefix = ""
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
        dynamicLink.link = Uri.parse("https://photex.page.link/j6mu")
        dynamicLink.setDomainUriPrefix("https://photex.page.link")
        val androidOptionsBuilder = DynamicLink.AndroidParameters.Builder("com.ritwik.photex")
        androidOptionsBuilder.minimumVersion = 12
        val androidOptions = androidOptionsBuilder.build()
        dynamicLink.setAndroidParameters(androidOptions)

        val dynamicLinkFinal = dynamicLink.buildDynamicLink()
        val dynamicLinkUri = dynamicLinkFinal.uri
        return dynamicLinkUri.toString()
    }
    fun getCustomLink():String
    {
        val link = "https://photex.page.link/?link=https://photex.page.link/j6mu-id=1024&apn=com.ritwik.photex&amv=12&st=Photex&sd=Download Photex and create your own memes"
        return link



    }
}