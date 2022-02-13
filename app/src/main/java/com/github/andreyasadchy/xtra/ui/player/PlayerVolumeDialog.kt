package com.github.andreyasadchy.xtra.ui.player

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.edit
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.player_volume.*


class PlayerVolumeDialog : ExpandingBottomSheetDialogFragment() {

    interface PlayerVolumeListener {
        fun changeVolume(volume: Float)
    }

    companion object {
        fun newInstance(): PlayerVolumeDialog {
            return PlayerVolumeDialog()
        }
    }

    private lateinit var listener: PlayerVolumeListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as PlayerVolumeListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.player_volume, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vol = context?.prefs()?.getInt(C.PLAYER_VOLUME, 100)
        volumeText.text = vol.toString()
        if (vol == 0) {
            volumeMute.setImageResource(R.drawable.baseline_volume_off_black_24)
            volumeMute.setOnClickListener {
                setVolume(100)
                volumeBar.progress = 100
            }
        } else {
            volumeMute.setImageResource(R.drawable.baseline_volume_up_black_24)
            volumeMute.setOnClickListener {
                setVolume(0)
                volumeBar.progress = 0
            }
        }
        volumeBar.progress = vol!!
        volumeBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                setVolume(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun setVolume(volume: Int) {
        listener.changeVolume((volume / 100f))
        context?.prefs()?.edit { putInt(C.PLAYER_VOLUME, volume) }
        volumeText.text = volume.toString()
        if (volume == 0) {
            volumeMute.setImageResource(R.drawable.baseline_volume_off_black_24)
            volumeMute.setOnClickListener {
                setVolume(100)
                volumeBar.progress = 100
            }
        } else {
            volumeMute.setImageResource(R.drawable.baseline_volume_up_black_24)
            volumeMute.setOnClickListener {
                setVolume(0)
                volumeBar.progress = 0
            }
        }
    }
}
