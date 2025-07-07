package com.afin.jauharnafissubmission1expert.features.story.presentation.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afin.jauharnafissubmission1expert.BuildConfig
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.utils.EventObserver
import com.afin.jauharnafissubmission1expert.core.utils.ImageUtils
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.core.utils.ViewModelFactory
import com.afin.jauharnafissubmission1expert.core.utils.WidgetUpdateHelper
import com.afin.jauharnafissubmission1expert.core.utils.showToast
import com.afin.jauharnafissubmission1expert.databinding.ActivityAddStoryBinding
import com.afin.jauharnafissubmission1expert.features.story.presentation.list.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: AddStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var currentImageFile: File? = null
    private var currentLocation: Location? = null
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupToolbar()
        setupAction()
        setupObservers()

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_CLOSE,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupObservers() {
        viewModel.uploadResult.observe(this, EventObserver { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    showToast(result.data)
                    WidgetUpdateHelper.updateWidget(this)
                    navigateToMain()
                }

                is Result.Error -> {
                    showLoading(false)
                    showToast(result.message)
                }
            }
        })
    }

    private fun setupAction() {
        binding.btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        binding.btnGallery.setOnClickListener {
            checkGalleryPermission()
        }

        binding.buttonAdd.setOnClickListener {
            uploadStory()
        }

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermission()
            } else {
                currentLocation = null
            }
        }
    }

    private fun checkCameraPermission() {
        Dexter.withContext(this)
            .withPermissions(Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        startCamera()
                    } else {
                        showPermissionDeniedDialog("Kamera")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_MEDIA_IMAGES)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        startGallery()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        showPermissionDeniedDialog("Galeri")
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        } else {
            Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        startGallery()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        showPermissionDeniedDialog("Galeri")
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun checkLocationPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).withListener(object : MultiplePermissionsListener {
                @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        getLastLocation()
                    } else {
                        binding.switchLocation.isChecked = false
                        showPermissionDeniedDialog("Lokasi")
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)?.also {
            ImageUtils.createTempFile(application).also { file ->
                currentPhotoPath = file.absolutePath
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    file
                )
                currentImageFile = file
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(intent)
            }
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Handle successful camera capture
            currentPhotoPath?.let { path ->
                val myFile = File(path)
                myFile.let { file ->
                    currentImageFile = file
                    binding.ivPreview.setImageBitmap(BitmapFactory.decodeFile(file.path))
                    showImage()
                }
            }
        } else {
            // Handle cancelled camera capture
            currentPhotoPath = null
            currentImageFile = null
            showToast("Pengambilan gambar dibatalkan")
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih Gambar")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data
            selectedImg?.let { uri ->
                val myFile = ImageUtils.uriToFile(uri, this@AddStoryActivity)
                currentImageFile = myFile
                binding.ivPreview.setImageURI(uri)
                showImage()
            }
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    showToast("Lokasi berhasil didapatkan")
                } else {
                    showToast("Lokasi tidak tersedia")
                    binding.switchLocation.isChecked = false
                }
            }
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString().trim()

        when {
            currentImageFile == null -> {
                showToast("Pilih gambar terlebih dahulu")
            }

            description.isEmpty() -> {
                binding.tilDescription.error = getString(R.string.error_field_empty)
            }

            else -> {
                binding.tilDescription.error = null
                val file = ImageUtils.reduceFileImage(currentImageFile as File)

                if (binding.switchLocation.isChecked && currentLocation != null) {
                    viewModel.uploadStoryWithLocation(
                        file,
                        description,
                        currentLocation!!.latitude,
                        currentLocation!!.longitude
                    )
                } else {
                    viewModel.uploadStory(file, description)
                }
            }
        }
    }

    private fun showImage() {
        binding.ivPreview.visibility = View.VISIBLE
        binding.llPlaceholder.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            buttonAdd.isEnabled = !isLoading
            btnCamera.isEnabled = !isLoading
            btnGallery.isEnabled = !isLoading
            edAddDescription.isEnabled = !isLoading
            switchLocation.isEnabled = !isLoading
        }
    }

    private fun showPermissionDeniedDialog(feature: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Permission Diperlukan")
            setMessage("Aplikasi memerlukan permission untuk mengakses $feature")
            setPositiveButton("OK", null)
            create()
            show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}