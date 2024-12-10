package com.mushscope.view.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mushscope.R
import com.mushscope.databinding.FragmentProfileBinding
import com.mushscope.utils.ViewModelFactory
import com.mushscope.view.history.HistoryActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Set up Toolbar as ActionBar
        val toolbar: Toolbar = binding.root.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.profile_title)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe user session
        profileViewModel.getUserSession().observe(viewLifecycleOwner) { userModel ->
            Log.d("ProfileFragment", "Photo URL: ${userModel.photoUrl}")
            // Update name
            binding.tvName.text = userModel.name ?: getString(R.string.profile_name)

            // Update profile photo if available
            if (!userModel.photoUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(userModel.photoUrl)
                    .placeholder(R.drawable.ic_foto_profile)
                    .error(R.drawable.ic_foto_profile)
                    .transform(CropCircleTransformation()) // Apply circle transformation
                    .into(binding.imgProfile)
            } else {
                // If no photo URL, set the default profile image
                binding.imgProfile.setImageResource(R.drawable.ic_foto_profile)
            }
        }

        // Observe theme settings
        profileViewModel.getThemeSettings().observe(viewLifecycleOwner) { isDarkModeActive ->
            binding.switchTheme.isChecked = isDarkModeActive
        }

        // Set the listener for the theme switch
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            profileViewModel.saveThemeSetting(isChecked)
        }

        // Set the click listener for the history button
        binding.btnHistory.setOnClickListener {
            animateButton(binding.btnHistory) // Animasi tombol
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        // Set the click listener for the logout button with animation
        binding.btnLogout.setOnClickListener {
            animateButton(binding.btnLogout) // Animasi tombol
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.logout))
            setMessage(getString(R.string.logout_confirmation))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                profileViewModel.logout()
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun animateButton(button: View) {
        val scaleAnimation = ScaleAnimation(
            1f, 0.9f, 1f, 0.9f, // Membesar
            Animation.RELATIVE_TO_SELF, 0.5f, // Titik pusat animasi horizontal
            Animation.RELATIVE_TO_SELF, 0.5f  // Titik pusat animasi vertikal
        ).apply {
            duration = 50 // Durasi animasi
            repeatCount = 0 // Tidak ada pengulangan animasi
            fillAfter = true // Agar animasi tetap pada ukuran akhir setelah selesai
        }

        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Bisa menambahkan logika lain jika perlu
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Kembali ke ukuran normal setelah animasi selesai
                button.postDelayed({
                    val resetAnimation = ScaleAnimation(
                        0.9f, 1f, 0.9f, 1f, // Kembali ke ukuran asli
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = 50
                    }
                    button.startAnimation(resetAnimation)
                }, 50) // Memberi sedikit delay sebelum animasi kembali ke normal
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // Tidak digunakan dalam kasus ini
            }
        })

        button.startAnimation(scaleAnimation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
