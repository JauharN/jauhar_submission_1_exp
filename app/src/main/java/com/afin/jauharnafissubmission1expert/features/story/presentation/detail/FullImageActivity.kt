package com.afin.jauharnafissubmission1expert.features.story.presentation.detail

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullImageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Buat ImageView fullscreen
        val imageView = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
        }

        setContentView(imageView)

        // Ambil URL gambar dari intent
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        Glide.with(this)
            .load(imageUrl)
            .into(imageView)

        imageView.setOnClickListener {
            finish()
        }
    }
}
