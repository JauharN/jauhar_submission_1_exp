package com.afin.jauharnafissubmission1expert.core.utils

import androidx.lifecycle.Observer

/**
 * Kelas pembungkus (wrapper) untuk data yang dikirim melalui LiveData sebagai sebuah *event*.
 *
 * Tujuannya adalah agar event hanya diproses satu kali saja, misalnya untuk menampilkan pesan error,
 * navigasi halaman, atau aksi lainnya yang tidak boleh ter-trigger ulang ketika konfigurasi berubah (seperti rotasi layar).
 */
open class Event<out T>(private val content: T) {

    // Menandakan apakah event ini sudah pernah diambil/ditangani
    var hasBeenHandled = false
        private set // hanya bisa dibaca dari luar, tapi tidak bisa diubah langsung

    /**
     * Mengembalikan isi dari event *jika belum pernah diambil sebelumnya*.
     * Jika sudah diambil, akan mengembalikan null agar tidak terjadi duplikasi aksi.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null // Sudah ditangani sebelumnya, tidak dikembalikan lagi
        } else {
            hasBeenHandled = true
            content // Pertama kali diambil, baru dikembalikan
        }
    }

}

/**
 * Observer khusus untuk event yang dibungkus dengan `Event<T>`.
 *
 * Fungsinya menyederhanakan proses pengamatan LiveData agar hanya menanggapi event
 * yang *belum pernah* ditangani.
 *
 * [onEventUnhandledContent] hanya akan dipanggil jika event belum pernah diproses sebelumnya.
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
        value.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it) // Jalankan aksi hanya jika event baru
        }
    }
}
