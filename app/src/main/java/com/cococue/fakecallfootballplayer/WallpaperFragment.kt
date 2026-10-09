package com.cococue.fakecallfootballplayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cococue.fakecallfootballplayer.adapter.WallpaperAdapter
import com.cococue.fakecallfootballplayer.databinding.FragmentWallpaperBinding
import com.cococue.fakecallfootballplayer.model.WallpaperItem

class WallpaperFragment : Fragment() {

    private var _binding: FragmentWallpaperBinding? = null
    private val binding get() = _binding!!

    private val allWallpapers = listOf(
        WallpaperItem("1", "Ronaldo Matchday HD 4K", R.drawable.wallpaper_football_star_1, isVideo = false, category = "Photo"),
        WallpaperItem("2", "Ronaldo Golden Magic 4K", R.drawable.wallpaper_football_star_2, isVideo = false, category = "Photo"),
        WallpaperItem("3", "Messi Style 4K", R.drawable.wallpaper_football_star_3, isVideo = false, category = "Photo"),
        WallpaperItem("4", "Bellingham Style 4K", R.drawable.wallpaper_football_star_4, isVideo = false, category = "Photo"),
        WallpaperItem("5", "Haaland  HD", R.drawable.wallpaper_football_star_5, isVideo = false, category = "Photo"),
        WallpaperItem("6", "Ronaldo Style 4K", R.drawable.wallpaper_football_star_6, isVideo = false, category = "Photo"),
        WallpaperItem("7", "Neymar Style 4K", R.drawable.wallpaper_football_star_7, isVideo = false, category = "Photo"),
        WallpaperItem("8", "Football Legend Stadium 4K", R.drawable.wallpaper_football_star_8, isVideo = false, category = "Photo"),
        WallpaperItem("9", "CR7 Celebration HD 4K", R.drawable.wallpaper_football_star_9, isVideo = false, category = "Photo"),
        WallpaperItem("10", "Golden Trophy Champions 4K", R.drawable.wallpaper_football_star_10, isVideo = false, category = "Photo"),
        WallpaperItem("11", "Mbappe Style 4K", R.drawable.wallpaper_football_star_11, isVideo = false, category = "Photo")

    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWallpaperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateWallpaperList(allWallpapers)

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val filtered = when (checkedIds.firstOrNull()) {
                R.id.chipPhoto -> allWallpapers.filter { !it.isVideo }
                R.id.chipVideo -> allWallpapers.filter { it.isVideo }
                else -> allWallpapers
            }
            updateWallpaperList(filtered)
        }
    }

    private fun updateWallpaperList(items: List<WallpaperItem>) {
        val adapter = WallpaperAdapter(items) { wallpaper ->
            val intent = Intent(requireContext(), WallpaperDetailActivity::class.java).apply {
                putExtra("EXTRA_TITLE", wallpaper.title)
                putExtra("EXTRA_RES_ID", wallpaper.resId)
                putExtra("EXTRA_IS_VIDEO", wallpaper.isVideo)
            }
            startActivity(intent)
        }
        binding.rvWallpapers.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
