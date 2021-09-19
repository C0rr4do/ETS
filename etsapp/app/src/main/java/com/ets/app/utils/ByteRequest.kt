package com.ets.app.utils

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser

class ByteRequest(
    url: String,
    private val listener: Response.Listener<ByteArray>,
    errorListener: Response.ErrorListener
) : Request<ByteArray>(Method.GET, url, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<ByteArray> {
        return Response.success(response?.data, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: ByteArray?) = listener.onResponse(response)
}