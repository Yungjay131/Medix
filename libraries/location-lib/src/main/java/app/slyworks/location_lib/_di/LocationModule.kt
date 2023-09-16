package app.slyworks.location_lib._di

import android.content.Context
import app.slyworks.location_lib.LocationApiHelper
import app.slyworks.location_lib.LocationTracker
import app.slyworks.location_lib.PlacesApiHelper
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 5:02 PM, 21-May-2023.
 */
@Module
object LocationModule {

    @Provides
    fun provideLocationApiHelper(ctx: Context):LocationApiHelper = LocationApiHelper(ctx)

    @Provides
    fun providePlacesApiHelper(ctx: Context):PlacesApiHelper = PlacesApiHelper(ctx)

    @Provides
    fun provideLocationTracker(ctx: Context,
                               lah: LocationApiHelper,
                               pah: PlacesApiHelper ):LocationTracker =
        LocationTracker(ctx, lah, pah)
}