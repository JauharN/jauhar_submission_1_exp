package com.afin.jauharnafissubmission1expert.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toUri
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.data.remote.api.ApiConfig
import com.afin.jauharnafissubmission1expert.core.di.Injection
import com.afin.jauharnafissubmission1expert.features.story.presentation.detail.DetailActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// fitur widget untuk menampilkan gambar story, sesuai kriteria
class StoryWidget : AppWidgetProvider() {

    companion object {
        private const val TOAST_ACTION = "com.afin.jauharnafissubmission1expert.TOAST_ACTION"
        const val EXTRA_ITEM = "com.afin.jauharnafissubmission1expert.EXTRA_ITEM"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, StackWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = intent.toUri(Intent.URI_INTENT_SCHEME).toUri()

            val views = RemoteViews(context.packageName, R.layout.story_widget)
            views.setRemoteAdapter(R.id.stack_view, intent)
            views.setEmptyView(R.id.stack_view, R.id.empty_view)

            val toastIntent = Intent(context, StoryWidget::class.java)
            toastIntent.action = TOAST_ACTION
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            val toastPendingIntent = PendingIntent.getBroadcast(
                context, 0, toastIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != null && intent.action == TOAST_ACTION) {
            val storyId = intent.getStringExtra(EXTRA_ITEM)
            val openIntent = Intent(context, DetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(DetailActivity.EXTRA_STORY_ID, storyId)
            }
            context.startActivity(openIntent)
        }
    }
}

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}

internal class StackRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<WidgetItem>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val userPreference = Injection.provideUserPreference(context)
        val token = runBlocking { userPreference.getUser().first().token }

        if (token.isNotEmpty()) {
            try {
                val apiService = ApiConfig.getApiService(token)
                val response = runBlocking { apiService.getStories(size = 5) }

                mWidgetItems.clear()
                response.listStory.forEach { story ->
                    mWidgetItems.add(
                        WidgetItem(
                            id = story.id,
                            name = story.name,
                            photoUrl = story.photoUrl
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item)

        if (position < mWidgetItems.size) {
            val item = mWidgetItems[position]

            try {
                val bitmap = Glide.with(context)
                    .asBitmap()
                    .load(item.photoUrl)
                    .submit(512, 512)
                    .get()

                rv.setImageViewBitmap(R.id.imageView, bitmap)
                rv.setTextViewText(R.id.tvName, item.name)
            } catch (e: Exception) {
                rv.setImageViewResource(R.id.imageView, R.drawable.ic_empty)
            }

            val fillInIntent = Intent().apply {
                putExtra(StoryWidget.EXTRA_ITEM, item.id)
            }
            rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        }

        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}

data class WidgetItem(
    val id: String,
    val name: String,
    val photoUrl: String
)