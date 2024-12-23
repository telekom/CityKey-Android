package com.telekom.citykey.utils.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.telekom.citykey.R
import com.telekom.citykey.domain.notifications.OscaPushService
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.RegExUtils
import timber.log.Timber
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified T : Activity> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}

inline fun <reified T : Activity> Fragment.startActivity() {
    startActivity(Intent(context!!, T::class.java))
}

inline fun <reified T : Activity> Activity.startActivity() {
    startActivity(Intent(applicationContext, T::class.java))
}

fun Fragment.attemptOpeningWebViewUri(uri: Uri?) {
    uri?.let {
        try {
            openLink(it.toString())
        } catch (e: Exception) {
            Timber.e(e, "Error in opening Custom Tab link  => $it")
            tryDelegatingUriToSystem(it)
        }
    }
}

fun Fragment.openLink(url: String) {
    if (url.lowercase().startsWith(OscaPushService.CITYKEY_DEEP_LINK_URI_IDENTIFIER)) {
        try {
            val uri = getDeeplinkUri(url)
            requireActivity().intent.data = uri
            findNavController().navigate(uri)
        } catch (e: Exception) {
            DialogUtil.showTechnicalError(requireContext())
        }
    } else if (url.startsWith(RegExUtils.PHONE_URI_PREFIX)) {
        val phoneIntent = Intent().apply {
            data = Uri.parse(url)
            action = Intent.ACTION_DIAL
        }
        startActivity(phoneIntent)
    } else if (url.startsWith(RegExUtils.EMAIL_URI_PREFIX)) {
        val emailIntent = Intent().apply {
            data = Uri.parse(url)
            action = Intent.ACTION_SEND
        }
        startActivity(emailIntent)
    } else if (RegExUtils.webUrl.matcher(url).matches()) {
        requireActivity().openLink(url)
    } else {
        DialogUtil.showTechnicalError(requireContext())
    }
}

fun Fragment.openApp(packageName: String) {
    val intent =
        requireActivity().packageManager.getLaunchIntentForPackage(packageName) ?: Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$packageName")
        }
    startActivity(intent)
}

fun Fragment.openMapApp(latitude: Double, longitude: Double) {

    try {
        val uriWaze = "waze://?ll=$latitude, $longitude&navigate=yes"
        val intentWazeNav = Intent(Intent.ACTION_VIEW, Uri.parse(uriWaze)).setPackage("com.waze")

        val uriHereWeGo = "here.directions://v1.0/mylocation/$latitude,$longitude?m=w"
        val intentHereWeGoNav = Intent(Intent.ACTION_VIEW, Uri.parse(uriHereWeGo)).setPackage("com.here.app.maps")

        val uriGoogle = "google.navigation:q=$latitude,$longitude"
        val intentGoogleNav =
            Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle)).setPackage("com.google.android.apps.maps")

        val chooserIntent = Intent.createChooser(intentGoogleNav, getString(R.string.application_name))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentWazeNav, intentHereWeGoNav))

        startActivity(chooserIntent)

    } catch (e: ActivityNotFoundException) {
        DialogUtil.showInfoDialog(
            requireContext(), R.string.dialog_technical_error_title, R.string.e_006_event_no_map_found_text
        )
    }
}

fun Activity.openLink(url: String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    customTabsIntent.launchUrl(applicationContext, Uri.parse(url))
}

fun Fragment.getColor(@ColorRes resId: Int): Int {
    context?.let { return ContextCompat.getColor(it, resId) }
    return 0
}

fun Fragment.getDrawable(@DrawableRes resId: Int): Drawable? {
    context?.let { return ContextCompat.getDrawable(it, resId) }
    return null
}

fun Fragment.safeRun(runnable: () -> Unit) {
    if (view != null) runnable()
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}

class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment, val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            Timber.i("onDestroy")
                            binding = null
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        binding?.let { return it }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

private const val SNACKBAR_TEXT_SIZE = 14f

fun Fragment.showActionSnackbar(
    @StringRes messageId: Int, @StringRes actionTextId: Int, actionClick: () -> Unit
) {
    Snackbar.make(
        requireView(), messageId, Snackbar.LENGTH_LONG
    ).apply {
        (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
            setMargins(6.dpToPixel(requireContext()))
            view.layoutParams = this
        }
        view.background =
            PaintDrawable(getColor(R.color.black85a)).apply { setCornerRadius(4.dpToPixel(requireContext()).toFloat()) }

        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, SNACKBAR_TEXT_SIZE)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action).apply {
            setTextColor(ColorUtils.darken(getColor(R.color.oscaColor), 1.10f))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, SNACKBAR_TEXT_SIZE)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        setAction(actionTextId) {
            actionClick()
        }
        show()
    }
}

fun Fragment.showInfoSnackBar(
    @StringRes messageId: Int,
) {
    val behavior =
        BaseTransientBottomBar.Behavior().apply { setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END) }

    Snackbar.make(requireView(), "", Snackbar.LENGTH_LONG).setBehavior(behavior).apply {
        val root = view as ViewGroup
        val custom: View = LayoutInflater.from(context).inflate(R.layout.appointment_snackbar_view, null)
        root.removeAllViews()
        root.addView(custom)
        custom.findViewById<TextView>(R.id.snackbarText).setText(messageId)
        custom.findViewById<View>(R.id.snackbarAction).setOnClickListener { dismiss() }
        (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
            setMargins(6.dpToPixel(requireContext()))
            view.layoutParams = this
        }

        view.background = PaintDrawable(getColor(R.color.black85a)).apply {
            setCornerRadius(4.dpToPixel(requireContext()).toFloat())
        }

        view.setPadding(0, 0, 0, 0)
        show()
    }
}

fun Fragment.showInfoSnackBar(
    message: String
) {
    Snackbar.make(
        requireView(), message, Snackbar.LENGTH_LONG
    ).apply {
        (view.layoutParams as? FrameLayout.LayoutParams)?.apply {
            setMargins(6.dpToPixel(requireContext()))
            view.layoutParams = this
        }
        view.background =
            PaintDrawable(getColor(R.color.black85a)).apply { setCornerRadius(4.dpToPixel(requireContext()).toFloat()) }

        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, SNACKBAR_TEXT_SIZE)
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        show()
    }
}

private fun Fragment.tryDelegatingUriToSystem(uri: Uri) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Timber.e(e, "No app found to handle the URI => $uri")
        DialogUtil.showTechnicalError(requireContext())
    }
}

private fun getDeeplinkUri(deepLinkUrl: String): Uri = if (deepLinkUrl.contains("eventId=")) {
    val cityId = Uri.parse(deepLinkUrl).getQueryParameter("cityId")
    var deeplink = deepLinkUrl.replace("?eventId=", "/")
    deeplink = deeplink.replace("&cityId=$cityId", "/0/")
    Uri.parse(deeplink)
} else {
    Uri.parse(deepLinkUrl)
}
