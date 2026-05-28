package wilddad.oppo.asspilot

import android.app.Application
import com.google.android.material.color.DynamicColors
import wilddad.oppo.asspilot.db.AppDatabase

class AssPilotApp : Application() {

    companion object {
        lateinit var instance: AssPilotApp
            private set
    }

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Enable Monet engine (Dynamic Colors)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
