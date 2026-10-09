package com.cococue.fakecallfootballplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cococue.fakecallfootballplayer.databinding.ActivityFakeCallBinding
import com.cococue.fakecallfootballplayer.model.CallTemplate
import com.cococue.fakecallfootballplayer.utils.AdManager
import com.cococue.fakecallfootballplayer.utils.LocaleHelper
import com.cococue.fakecallfootballplayer.utils.SoundVibrationManager
import android.content.Context

class FakeCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFakeCallBinding
    private lateinit var soundVibrationManager: SoundVibrationManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase))
    }

    private var callTimerHandler = Handler(Looper.getMainLooper())
    private var secondsElapsed = 0
    private var isCallConnected = false
    private var isVideoCall = true
    private var playerVideoRes = R.raw.video_ronaldo
    private var customVideoUriStr: String? = null
    private var selectedTemplate = CallTemplate.WHATSAPP

    private var currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    // Draggable PIP coordinates
    private var dX = 0f
    private var dY = 0f

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startFrontCameraPreview()
        } else {
            Toast.makeText(this, "Camera permission needed for Video Call", Toast.LENGTH_SHORT).show()
        }
    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            secondsElapsed++
            val minutes = secondsElapsed / 60
            val seconds = secondsElapsed % 60
            binding.tvCallTimer.text = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
            callTimerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Keep screen on and show on lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        binding = ActivityFakeCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle Window Insets for Edge-to-Edge display
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerContainer.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        // Force CameraX PreviewView to use TextureView so it renders ON TOP of VideoView surface
        binding.cameraPreviewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

        soundVibrationManager = SoundVibrationManager(this)
        soundVibrationManager.startRingtoneAndVibration()

        setupUIFromIntent()
        setupClickListeners()
        setupDraggableCameraPip()

        if (isVideoCall) {
            checkCameraPermissionAndStart()
        }
    }

    private fun setupUIFromIntent() {
        val playerName = intent.getStringExtra("EXTRA_PLAYER_NAME") ?: "Cristiano Ronaldo"
        val avatarRes = intent.getIntExtra("EXTRA_PLAYER_AVATAR", R.drawable.img_ronaldo)
        playerVideoRes = intent.getIntExtra("EXTRA_PLAYER_VIDEO", R.raw.video_ronaldo)
        val customAvatarUriStr = intent.getStringExtra("EXTRA_CUSTOM_AVATAR_URI")
        customVideoUriStr = intent.getStringExtra("EXTRA_CUSTOM_VIDEO_URI")
        val templateStr = intent.getStringExtra("EXTRA_TEMPLATE") ?: "WHATSAPP"
        isVideoCall = intent.getBooleanExtra("EXTRA_IS_VIDEO", true)

        binding.tvCallerName.text = playerName

        if (customAvatarUriStr != null) {
            try {
                binding.imgCallerAvatar.setImageURI(Uri.parse(customAvatarUriStr))
            } catch (e: Exception) {
                binding.imgCallerAvatar.setImageResource(avatarRes)
            }
        } else {
            binding.imgCallerAvatar.setImageResource(avatarRes)
        }

        selectedTemplate = try {
            CallTemplate.valueOf(templateStr)
        } catch (e: Exception) {
            CallTemplate.WHATSAPP
        }

        applyTemplateTheme()
    }

    private fun applyTemplateTheme() {
        when (selectedTemplate) {
            CallTemplate.WHATSAPP -> {
                binding.rootCallView.setBackgroundColor(ContextCompat.getColor(this, R.color.wa_bg_dark))
                binding.btnMinimize.setImageResource(R.drawable.ic_minimize)
                binding.btnMinimize.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnAddPerson.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnHeaderMore.visibility = View.GONE
                binding.layoutCenterHeader.visibility = View.VISIBLE
                binding.layoutIgEffects.visibility = View.GONE
                binding.layoutPipActions.visibility = View.VISIBLE

                // Dock buttons for WA
                binding.btnDock1.setImageResource(R.drawable.ic_more_horiz)
                binding.btnDock1.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnDock1.setColorFilter(Color.WHITE)

                binding.btnDock2.setImageResource(R.drawable.ic_videocam)
                binding.btnDock2.setBackgroundResource(R.drawable.bg_circle_white_button)
                binding.btnDock2.setColorFilter(Color.BLACK)

                binding.btnDock3.setImageResource(R.drawable.ic_speaker)
                binding.btnDock3.setBackgroundResource(R.drawable.bg_circle_white_button)
                binding.btnDock3.setColorFilter(Color.BLACK)

                binding.btnDock4.setImageResource(R.drawable.ic_mic_mute)
                binding.btnDock4.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnDock4.setColorFilter(Color.WHITE)
            }
            CallTemplate.INSTAGRAM -> {
                binding.rootCallView.setBackgroundColor(ContextCompat.getColor(this, R.color.ig_bg_dark))
                binding.btnMinimize.setImageResource(R.drawable.ic_chevron_down)
                binding.btnMinimize.setBackgroundResource(android.R.color.transparent)
                binding.btnAddPerson.setBackgroundResource(android.R.color.transparent)
                binding.btnHeaderMore.visibility = View.VISIBLE
                binding.btnHeaderMore.setBackgroundResource(android.R.color.transparent)
                binding.layoutCenterHeader.visibility = View.GONE
                binding.layoutIgEffects.visibility = View.VISIBLE
                binding.layoutPipActions.visibility = View.GONE

                // Dock buttons for IG
                binding.btnDock1.setImageResource(R.drawable.ic_ig_camera)
                binding.btnDock1.setBackgroundResource(android.R.color.transparent)
                binding.btnDock1.setColorFilter(Color.WHITE)

                binding.btnDock2.setImageResource(R.drawable.ic_mic_mute)
                binding.btnDock2.setBackgroundResource(android.R.color.transparent)
                binding.btnDock2.setColorFilter(Color.WHITE)

                binding.btnDock3.setImageResource(R.drawable.ic_media_share)
                binding.btnDock3.setBackgroundResource(android.R.color.transparent)
                binding.btnDock3.setColorFilter(Color.WHITE)

                binding.btnDock4.setImageResource(R.drawable.ic_flip_camera)
                binding.btnDock4.setBackgroundResource(android.R.color.transparent)
                binding.btnDock4.setColorFilter(Color.WHITE)
            }
            CallTemplate.TELEGRAM -> {
                binding.rootCallView.setBackgroundColor(ContextCompat.getColor(this, R.color.tg_bg_dark))
                binding.btnMinimize.setImageResource(R.drawable.ic_chevron_down)
                binding.btnMinimize.setBackgroundResource(android.R.color.transparent)
                binding.btnAddPerson.setBackgroundResource(android.R.color.transparent)
                binding.btnHeaderMore.visibility = View.GONE
                binding.layoutCenterHeader.visibility = View.VISIBLE
                binding.layoutIgEffects.visibility = View.GONE
                binding.layoutPipActions.visibility = View.GONE

                // Dock buttons for TG
                binding.btnDock1.setImageResource(R.drawable.ic_more_horiz)
                binding.btnDock1.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnDock1.setColorFilter(Color.WHITE)

                binding.btnDock2.setImageResource(R.drawable.ic_videocam)
                binding.btnDock2.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnDock2.setColorFilter(Color.WHITE)

                binding.btnDock3.setImageResource(R.drawable.ic_speaker)
                binding.btnDock3.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnDock3.setColorFilter(Color.WHITE)

                binding.btnDock4.setImageResource(R.drawable.ic_mic_mute)
                binding.btnDock4.setBackgroundResource(R.drawable.bg_circle_dark_button)
                binding.btnDock4.setColorFilter(Color.WHITE)
            }
        }
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startFrontCameraPreview()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startFrontCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = binding.cameraPreviewView.surfaceProvider
                }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, currentCameraSelector, preview)

                binding.cardCameraPip.visibility = View.VISIBLE
                binding.cardCameraPip.bringToFront()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Enables touch drag-and-drop movement for the camera preview window anywhere on the screen!
     */
    private fun setupDraggableCameraPip() {
        binding.cardCameraPip.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY
                    view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener {
            acceptCall()
        }

        binding.btnDecline.setOnClickListener {
            endCall()
        }

        binding.btnEndCall.setOnClickListener {
            endCall()
        }

        binding.btnMinimize.setOnClickListener {
            endCall()
        }

        binding.btnAddPerson.setOnClickListener {
            Toast.makeText(this, "Add Call Participants", Toast.LENGTH_SHORT).show()
        }

        binding.btnHeaderMore.setOnClickListener {
            Toast.makeText(this, "Instagram Call Options", Toast.LENGTH_SHORT).show()
        }

        binding.btnPipFlipCamera.setOnClickListener {
            flipCamera()
        }

        binding.btnPipMagicWand.setOnClickListener {
            Toast.makeText(this, "Face Filter & Effects Active", Toast.LENGTH_SHORT).show()
        }

        // IG Left Sidebar Effects Click Handlers
        binding.btnIgSparkles.setOnClickListener {
            Toast.makeText(this, "Instagram Sparkles Effect", Toast.LENGTH_SHORT).show()
        }

        binding.btnIgMagicWand.setOnClickListener {
            Toast.makeText(this, "Instagram Touch Up Effect", Toast.LENGTH_SHORT).show()
        }

        binding.btnIgAvatars.setOnClickListener {
            Toast.makeText(this, "Instagram Avatar Effect", Toast.LENGTH_SHORT).show()
        }

        binding.btnIgMediaShare.setOnClickListener {
            Toast.makeText(this, "Instagram Watch Together Feature", Toast.LENGTH_SHORT).show()
        }

        // Dock Buttons
        binding.btnDock1.setOnClickListener {
            if (selectedTemplate == CallTemplate.INSTAGRAM) {
                toggleVideoCamera()
            } else {
                Toast.makeText(this, "Call Options", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDock2.setOnClickListener {
            if (selectedTemplate == CallTemplate.INSTAGRAM) {
                it.isSelected = !it.isSelected
                binding.btnDock2.alpha = if (it.isSelected) 0.5f else 1.0f
            } else {
                toggleVideoCamera()
            }
        }

        binding.btnDock3.setOnClickListener {
            if (selectedTemplate == CallTemplate.INSTAGRAM) {
                Toast.makeText(this, "Instagram Media Share", Toast.LENGTH_SHORT).show()
            } else {
                it.isSelected = !it.isSelected
                binding.btnDock3.alpha = if (it.isSelected) 0.5f else 1.0f
            }
        }

        binding.btnDock4.setOnClickListener {
            if (selectedTemplate == CallTemplate.INSTAGRAM) {
                flipCamera()
            } else {
                it.isSelected = !it.isSelected
                binding.btnDock4.alpha = if (it.isSelected) 0.5f else 1.0f
            }
        }
    }

    private fun toggleVideoCamera() {
        binding.cardCameraPip.visibility = if (binding.cardCameraPip.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun flipCamera() {
        currentCameraSelector = if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startFrontCameraPreview()
    }

    private fun acceptCall() {
        isCallConnected = true
        soundVibrationManager.stop()

        binding.layoutRingingActions.visibility = View.GONE
        binding.tvCallStatus.visibility = View.GONE

        binding.layoutActiveActions.visibility = View.VISIBLE
        if (selectedTemplate != CallTemplate.INSTAGRAM) {
            binding.tvCallTimer.visibility = View.VISIBLE
        }

        if (isVideoCall) {
            startCelebrityVideoPlayback()
        }

        secondsElapsed = 0
        callTimerHandler.post(timerRunnable)
    }

    private fun startCelebrityVideoPlayback() {
        try {
            binding.imgCallerAvatar.visibility = View.GONE
            binding.vvCallerVideo.visibility = View.VISIBLE

            // Bring floating camera PIP & overlays to front above the VideoView Surface
            binding.cardCameraPip.bringToFront()
            binding.headerContainer.bringToFront()
            binding.layoutActiveActions.bringToFront()
            binding.layoutIgEffects.bringToFront()

            val videoUri = if (customVideoUriStr != null) {
                Uri.parse(customVideoUriStr)
            } else {
                Uri.parse("android.resource://$packageName/$playerVideoRes")
            }

            binding.vvCallerVideo.setVideoURI(videoUri)
            binding.vvCallerVideo.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                binding.vvCallerVideo.start()
            }
            binding.vvCallerVideo.setOnErrorListener { _, _, _ ->
                binding.vvCallerVideo.visibility = View.GONE
                binding.imgCallerAvatar.visibility = View.VISIBLE
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.vvCallerVideo.visibility = View.GONE
            binding.imgCallerAvatar.visibility = View.VISIBLE
        }
    }

    private fun endCall() {
        soundVibrationManager.stop()
        if (binding.vvCallerVideo.isPlaying) {
            binding.vvCallerVideo.stopPlayback()
        }
        if (isCallConnected) {
            callTimerHandler.removeCallbacks(timerRunnable)
        }

        AdManager.showInterstitialAd(this) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundVibrationManager.stop()
        if (binding.vvCallerVideo.isPlaying) {
            binding.vvCallerVideo.stopPlayback()
        }
        callTimerHandler.removeCallbacks(timerRunnable)
    }
}
