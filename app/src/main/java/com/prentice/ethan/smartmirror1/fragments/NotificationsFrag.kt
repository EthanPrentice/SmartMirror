package com.prentice.ethan.smartmirror1.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Switch
import com.prentice.ethan.smartmirror1.R
import kotlinx.android.synthetic.main.fragment_notifications.*


class NotificationsFrag : Fragment() {

    private val prefsName = "config"
    private lateinit var config: SharedPreferences

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = activity.getSharedPreferences(prefsName, 0)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_notifications, container, false)
        return view
    }

    // Main method
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set switches / seekbars to their preference values
        notifsOnSwitch.isChecked = config.getBoolean("NotifsEnabled", false)
        notifsUseDataSwitch.isChecked = config.getBoolean("NotifsUseData", false)
        notifsShownSlider.progress = config.getInt("NotifsShown", 4)

        // Set listeners
        setSwitchListeners(arrayOf(notifsOnSwitch, notifsUseDataSwitch))
        notifsShownSlider.setOnSeekBarChangeListener(SeekBarChangeListener())
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }


    private fun setSwitchListeners (switches: Array<Switch>) {
        val switchListener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
            when (view.id) {
                R.id.notifsOnSwitch -> {
                    val prefsEditor = config.edit()
                    prefsEditor.putBoolean("NotifsEnabled", isChecked)
                    prefsEditor.commit()
                    println("NotifsOn switched to $isChecked")
                }
                R.id.notifsUseDataSwitch -> {
                    val prefsEditor = config.edit()
                    prefsEditor.putBoolean("NotifsUseData", isChecked)
                    prefsEditor.commit()
                }
            }
        }
        switches.forEach {
            println("View: ${view.toString()}")
            it.setOnCheckedChangeListener(switchListener)
        }
    }

    inner class SeekBarChangeListener: SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekbar: SeekBar?){}
        override fun onStopTrackingTouch(seekbar: SeekBar?){}

        @SuppressLint("CommitPrefEdits", "ApplySharedPref")
        override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
            when (seekbar!!.id) {
                R.id.notifsShownSlider -> {
                    val prefsEditor = config.edit()
                    prefsEditor.putInt("NotifsShown", progress)
                    prefsEditor.commit()
                }
            }
        }
    }

    companion object {
        fun newInstance(): NotificationsFrag {
            return NotificationsFrag()
        }
    }
}
