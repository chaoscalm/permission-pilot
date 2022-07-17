package eu.darken.myperm.common.coil

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import eu.darken.myperm.apps.core.Pkg
import eu.darken.myperm.apps.core.getIcon2
import eu.darken.myperm.apps.core.tryIcon
import javax.inject.Inject

class AppIconFetcher @Inject constructor(
    private val packageManager: PackageManager,
    private val data: Pkg,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        var drawable: Drawable? = packageManager.getIcon2(data.id)

        if (drawable == null) {
            drawable = data.tryIcon(options.context)
        }

        return DrawableResult(
            drawable = drawable ?: ColorDrawable(Color.TRANSPARENT),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory @Inject constructor(
        private val packageManager: PackageManager,
    ) : Fetcher.Factory<Pkg> {

        override fun create(
            data: Pkg,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher = AppIconFetcher(packageManager, data, options)
    }
}

