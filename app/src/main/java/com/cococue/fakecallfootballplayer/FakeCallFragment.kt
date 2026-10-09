package com.cococue.fakecallfootballplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cococue.fakecallfootballplayer.adapter.PlayerAdapter
import com.cococue.fakecallfootballplayer.databinding.DialogAddCustomPlayerBinding
import com.cococue.fakecallfootballplayer.databinding.FragmentFakeCallBinding
import com.cococue.fakecallfootballplayer.model.CallTemplate
import com.cococue.fakecallfootballplayer.model.FootballPlayer
import com.cococue.fakecallfootballplayer.utils.AdManager
import com.cococue.fakecallfootballplayer.utils.CustomPlayerManager

class FakeCallFragment : Fragment() {

    private var _binding: FragmentFakeCallBinding? = null
    private val binding get() = _binding!!

    private var selectedPlayer: FootballPlayer? = null
    private var selectedTemplate = CallTemplate.WHATSAPP
    private var selectedTimerSeconds = 0
    private var isVideoCall = true

    private var allPlayers = mutableListOf<FootballPlayer>()
    private var playerAdapter: PlayerAdapter? = null

    private var tempAvatarUri: Uri? = null
    private var tempVideoUri: Uri? = null
    private var currentDialogBinding: DialogAddCustomPlayerBinding? = null

    private val samplePlayers = listOf(
        FootballPlayer("1", "C. Ronaldo", "Al Nassr / Portugal", R.drawable.wallpaper_football_star_6, R.raw.video_ronaldo, "+7 999 123 4567"),
        FootballPlayer("2", "L. Messi", "Inter Miami / Argentina", R.drawable.wallpaper_football_star_3, R.raw.video_messi, "+1 305 789 1234"),
        FootballPlayer("3", "K. Mbappé", "Real Madrid / France", R.drawable.wallpaper_football_star_11, R.raw.video_mbappe, "+33 6 12 34 56 78"),
        FootballPlayer("4", "Neymar Jr", "Al Hilal / Brazil", R.drawable.wallpaper_football_star_7, R.raw.video_neymar, "+55 11 98765 4321"),
        FootballPlayer("5", "E. Haaland", "Man City / Norway", R.drawable.wallpaper_football_star_5, R.raw.video_haaland, "+47 912 34 567"),
        FootballPlayer("6", "J. Bellingham", "Real Madrid / England", R.drawable.wallpaper_football_star_4, R.raw.video_bellingham, "+44 7700 900077")
    )

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tempAvatarUri = it
            currentDialogBinding?.imgCustomAvatarPreview?.setImageURI(it)
        }
    }

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tempVideoUri = it
            currentDialogBinding?.tvCustomVideoStatus?.text = getString(R.string.video_selected)
            currentDialogBinding?.tvCustomVideoStatus?.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.primary_accent)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFakeCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPlayers()
        setupTemplateSelectors()
        setupCallTypeToggle()
        setupTimerGroup()

        // Load AdMob Native Ad in Native Ad Container
        AdManager.loadNativeAd(requireContext(), binding.flNativeAdContainer)

        binding.btnStartCall.setOnClickListener {
            startFakeCallProcess()
        }
    }

    private fun loadPlayers() {
        val customPlayers = CustomPlayerManager.getCustomPlayers(requireContext())
        allPlayers = (samplePlayers + customPlayers).toMutableList()
        if (selectedPlayer == null || !allPlayers.contains(selectedPlayer)) {
            selectedPlayer = allPlayers.firstOrNull()
        }
        setupPlayerAdapter()
    }

    private fun setupPlayerAdapter() {
        val adapter = PlayerAdapter(
            players = allPlayers,
            onPlayerSelected = { player ->
                selectedPlayer = player
            },
            onAddCustomPlayerClicked = {
                showUnlockCustomPlayerDialog()
            }
        )
        playerAdapter = adapter
        binding.rvPlayers.adapter = adapter
    }

    private fun showUnlockCustomPlayerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.custom_player_dialog_title)
            .setMessage(R.string.custom_player_dialog_msg)
            .setPositiveButton(R.string.watch_rewarded_ad) { dialog, _ ->
                dialog.dismiss()
                AdManager.showRewardedAd(requireActivity()) {
                    showAddCustomPlayerDialog()
                }
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddCustomPlayerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_custom_player, null)
        val dialogBinding = DialogAddCustomPlayerBinding.bind(dialogView)
        currentDialogBinding = dialogBinding

        tempAvatarUri = null
        tempVideoUri = null

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialogBinding.btnPickAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        dialogBinding.btnPickVideo.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        dialogBinding.btnCancelCustom.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSaveCustom.setOnClickListener {
            val name = dialogBinding.etCustomName.text?.toString()?.trim()
            if (name.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.please_enter_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tempAvatarUri == null) {
                Toast.makeText(requireContext(), R.string.please_select_photo, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (tempVideoUri == null) {
                Toast.makeText(requireContext(), R.string.please_select_video, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPlayer = FootballPlayer(
                id = System.currentTimeMillis().toString(),
                name = name,
                clubInfo = "Custom Star",
                phoneNumber = "+1 555 " + (1000..9999).random(),
                customAvatarUri = tempAvatarUri.toString(),
                customVideoUri = tempVideoUri.toString()
            )

            CustomPlayerManager.saveCustomPlayer(requireContext(), newPlayer)
            selectedPlayer = newPlayer
            loadPlayers()
            Toast.makeText(requireContext(), R.string.add_custom_player_success, Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupTemplateSelectors() {
        binding.cardTemplateWA.setOnClickListener {
            selectedTemplate = CallTemplate.WHATSAPP
            updateTemplateSelectionUI()
        }
        binding.cardTemplateIG.setOnClickListener {
            selectedTemplate = CallTemplate.INSTAGRAM
            updateTemplateSelectionUI()
        }
        binding.cardTemplateTG.setOnClickListener {
            selectedTemplate = CallTemplate.TELEGRAM
            updateTemplateSelectionUI()
        }
    }

    private fun updateTemplateSelectionUI() {
        val ctx = requireContext()
        binding.cardTemplateWA.strokeWidth = if (selectedTemplate == CallTemplate.WHATSAPP) 6 else 0
        binding.cardTemplateWA.strokeColor = ContextCompat.getColor(ctx, R.color.wa_green)

        binding.cardTemplateIG.strokeWidth = if (selectedTemplate == CallTemplate.INSTAGRAM) 6 else 0
        binding.cardTemplateIG.strokeColor = ContextCompat.getColor(ctx, R.color.ig_pink)

        binding.cardTemplateTG.strokeWidth = if (selectedTemplate == CallTemplate.TELEGRAM) 6 else 0
        binding.cardTemplateTG.strokeColor = ContextCompat.getColor(ctx, R.color.tg_blue)
    }

    private fun setupCallTypeToggle() {
        binding.toggleCallType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isVideoCall = checkedId == R.id.btnVideoCall
            }
        }
    }

    private fun setupTimerGroup() {
        binding.chipGroupTimer.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedTimerSeconds = when (checkedIds.firstOrNull()) {
                R.id.chip5s -> 5
                R.id.chip10s -> 10
                R.id.chip30s -> 30
                R.id.chip60s -> 60
                else -> 0
            }
        }
    }

    private fun startFakeCallProcess() {
        val player = selectedPlayer ?: samplePlayers[0]

        if (selectedTimerSeconds == 0) {
            launchCallActivity(player)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.call_scheduled, selectedTimerSeconds),
                Toast.LENGTH_SHORT
            ).show()

            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) {
                    launchCallActivity(player)
                }
            }, selectedTimerSeconds * 1000L)
        }
    }

    private fun launchCallActivity(player: FootballPlayer) {
        val intent = Intent(requireContext(), FakeCallActivity::class.java).apply {
            putExtra("EXTRA_PLAYER_NAME", player.name)
            putExtra("EXTRA_PLAYER_AVATAR", player.avatarRes)
            putExtra("EXTRA_PLAYER_VIDEO", player.videoRes)
            putExtra("EXTRA_CUSTOM_AVATAR_URI", player.customAvatarUri)
            putExtra("EXTRA_CUSTOM_VIDEO_URI", player.customVideoUri)
            putExtra("EXTRA_TEMPLATE", selectedTemplate.name)
            putExtra("EXTRA_IS_VIDEO", isVideoCall)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialogBinding = null
        _binding = null
    }
}
