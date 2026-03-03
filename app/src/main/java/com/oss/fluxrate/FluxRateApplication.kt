package com.oss.fluxrate

import android.app.Application
import com.oss.fluxrate.ui.util.FlagMapper

class FluxRateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FlagMapper.init(this)
    }
}
