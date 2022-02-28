package com.github.andreyasadchy.xtra.ui.common

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.NotLoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.ui.login.LoginActivity
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.settings.SettingsActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.fragment_media.*


abstract class MediaFragment : Fragment(), Scrollable {

    private var previousItem = -1
    private var currentFragment: Fragment? = null
    open var hideSpinner = false

    open val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerMedia)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousItem = savedInstanceState?.getInt("previousItem", -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val isLoggedIn = User.get(activity) !is NotLoggedIn
        spinner.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, spinnerItems)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFragment = if (position != previousItem && isResumed) {
                    val newFragment = onSpinnerItemSelected(position)
                    childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment).commit()
                    previousItem = position
                    newFragment
                } else {
                    childFragmentManager.findFragmentById(R.id.fragmentContainer)
                }
                if (hideSpinner) {
                    spinner.gone()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        search.setOnClickListener { activity.openSearch() }
        menubutton.setOnClickListener { it ->
            PopupMenu(activity, it).apply {
                inflate(R.menu.top_menu)
                menu.findItem(R.id.login).title = if (isLoggedIn) getString(R.string.log_out) else getString(R.string.log_in)
                setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.settings -> { activity.startActivityFromFragment(this@MediaFragment, Intent(activity, SettingsActivity::class.java), 3) }
                        R.id.login -> {
                            if (!isLoggedIn) {
                                activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 1)
                            } else {
                                AlertDialog.Builder(activity)
                                    .setTitle(getString(R.string.logout_title))
                                    .setMessage(getString(R.string.logout_msg, context?.prefs()?.getString(C.USERNAME, "")))
                                    .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
                                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                        activity.startActivityForResult(Intent(activity, LoginActivity::class.java), 2) }
                                    .show()
                            }
                        }
                    }
                    true
                }
                show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("previousItem", previousItem)
        super.onSaveInstanceState(outState)
    }

    abstract fun onSpinnerItemSelected(position: Int): Fragment

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        (currentFragment as? Scrollable)?.scrollToTop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}