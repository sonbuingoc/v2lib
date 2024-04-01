package com.sonbn.admobutilslibrary.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import com.sonbn.admobutilslibrary.R
import com.sonbn.admobutilslibrary.databinding.DialogRatingBinding

class DialogRating() : DialogFragment() {
    private var _binding: DialogRatingBinding? = null
    private val binding get() = _binding!!
    private lateinit var ctx: Context
    private var star = 4

    var feedbackEmail: String = ""
    var appName: String = ""
    var versionCode: String = ""
    var versionName: String = ""

    var mCallback: Callback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        intListener()
    }

    private fun initView() {

    }

    private fun intListener() {
        binding.apply {
            imgStar1.setOnClickListener { setupStar(1) }
            imgStar2.setOnClickListener { setupStar(2) }
            imgStar3.setOnClickListener { setupStar(3) }
            imgStar4.setOnClickListener { setupStar(4) }
            imgStar5.setOnClickListener { setupStar(5) }

            btnCancel.setOnClickListener { dismiss() }
            btnRate.setOnClickListener { onClickRate() }
        }
    }

    private fun onClickRate() {
        if (star >= 4) {
            reviewInApp()
        } else {
            feedback()
        }
        mCallback?.onClickRate(star)
        dismiss()
    }

    private fun setupStar(number: Int) {
        val index = number.coerceIn(1, 5)
        star = index
        val anim = AnimationUtils.loadAnimation(ctx, R.anim.scale_anim)
        binding.apply {
            imgStar1.setImageResource(if (index >= 1) R.drawable.ic_checked_rating else R.drawable.ic_un_check_rating)
            imgStar2.setImageResource(if (index >= 2) R.drawable.ic_checked_rating else R.drawable.ic_un_check_rating)
            imgStar3.setImageResource(if (index >= 3) R.drawable.ic_checked_rating else R.drawable.ic_un_check_rating)
            imgStar4.setImageResource(if (index >= 4) R.drawable.ic_checked_rating else R.drawable.ic_un_check_rating)
            imgStar5.setImageResource(if (index >= 5) R.drawable.ic_checked_rating else R.drawable.ic_un_check_rating)

            /**/
            imgSticker.setImageResource(if (index <= 3) R.drawable.ic_sad else if (index == 4) R.drawable.ic_smile else R.drawable.ic_fun)
            imgSticker.startAnimation(anim)

            btnRate.text = if (index < 4) "Feedback" else "Rate"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun reviewInApp() {
        val reviewManager = ReviewManagerFactory.create(ctx)
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener { _ ->

                }
            } else {
                requireActivity().runOnUiThread {
                    rating()
                }
            }
        }
    }

    private fun feedback() {
        val appInfo =
            "App: $appName versionCode: $versionCode versionName: $versionName device: ${Build.MODEL} sdk: ${Build.VERSION.SDK}"
        val email = Intent(Intent.ACTION_SENDTO)
        email.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(feedbackEmail)
        )
        email.putExtra(Intent.EXTRA_SUBJECT, appInfo)
        email.data = Uri.parse("mailto:")
        startActivity(Intent.createChooser(email, appInfo))
    }

    private fun rating() {
        val appPackageName: String = ctx.packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: Throwable) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    interface Callback {
        fun onClickRate(star: Int)
    }
}