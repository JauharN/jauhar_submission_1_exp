package com.afin.jauharnafissubmission1expert.core.utils

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.afin.jauharnafissubmission1expert.features.widget.StoryWidget

object WidgetUpdateHelper {

    fun updateWidget(context: Context) {
        val intent = Intent(context, StoryWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val widgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = ComponentName(context, StoryWidget::class.java)
        val widgetIds = widgetManager.getAppWidgetIds(widgetComponent)

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        context.sendBroadcast(intent)
    }
}