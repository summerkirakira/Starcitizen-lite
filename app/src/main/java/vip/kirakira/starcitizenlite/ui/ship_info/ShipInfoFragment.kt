package vip.kirakira.starcitizenlite.ui.ship_info

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import vip.kirakira.starcitizenlite.R

class ShipInfoFragment : Fragment() {

    companion object {
        fun newInstance() = ShipInfoFragment()
    }

    private lateinit var viewModel: ShipInfoViewModel
    private lateinit var webview: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_ship_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ShipInfoViewModel::class.java)
        // TODO: Use the ViewModel
        webview = requireView().findViewById(R.id.ship_info_webview)
        webview.webViewClient = MyWebViewClient()

        val webSettings: WebSettings = webview.settings
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

        val pref = requireActivity().getSharedPreferences(
            requireActivity().getString(R.string.preference_file_key),
            android.content.Context.MODE_PRIVATE)

        val themeKey = requireActivity().getString(R.string.theme_color_key)

        val theme = pref.getString(themeKey, "DEEP_BLUE")

        if(theme == "BLACK") {
            webview.setBackgroundColor(Color.parseColor("#000000"))
            webview.loadUrl("http://cache.biaoju.site/mobile?theme=dark")
        } else {
            webview.setBackgroundColor(Color.parseColor("#ffffff"))
            webview.loadUrl("http://cache.biaoju.site/mobile?theme=light")
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)



    }

    private class MyWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

}