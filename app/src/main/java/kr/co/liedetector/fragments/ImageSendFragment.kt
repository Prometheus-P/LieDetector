package kr.co.liedetector.fragments

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.regions.Regions
import kr.co.liedetector.HttpConnect
import kr.co.liedetector.R
import kr.co.liedetector.S3Util
import kr.co.liedetector.databinding.AnswerPageBinding
import java.io.File
import java.lang.Exception


class ImageSendFragment : Fragment(),TransferListener {

    /** AndroidX navigation arguments */
    private val args: ImageSendFragmentArgs by navArgs()
    private lateinit var file: File
    private lateinit var alertDialog: AlertDialog
    private lateinit var answerPageBinding: AnswerPageBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        answerPageBinding=AnswerPageBinding.inflate(layoutInflater)

        return answerPageBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        answerPageBinding.text1.text= ""
        answerPageBinding.text2.text= ""
        answerPageBinding.text3.text= ""
        answerPageBinding.text4.text= ""
        answerPageBinding.text5.text= ""
        answerPageBinding.textAnswer.text=""
        attachBackPressedCallback()
        answerPageBinding.backwardButton.setOnClickListener {
//            requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
//            requireActivity().supportFragmentManager.popBackStack()
            lifecycleScope.launchWhenStarted {

                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    ImageSendFragmentDirections.actionImageViewerFragmentToCameraFragment(0.toString(), 256))
            }
        }
//        attachBackPressedCallback()
        Log.d("filePath",args.filePath)
        file= File(args.filePath)
        S3Util.instance.setKeys("AKIAZKVUOJ2AFWGYZB4E", "PO/Jy4s+W3yerOR6kjTFSoSoDjjRTdripEQ2RLZ3")
            .setRegion(Regions.AP_NORTHEAST_2)
            .uploadWithTransferUtility(
                requireContext(),
                "maple-lie-detector",
                "photo",
              file ,
                "",
                this
            );
    }

    private fun attachBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                requireActivity().supportFragmentManager.beginTransaction().remove(this@ImageSendFragment).commit()
//                requireActivity().supportFragmentManager.popBackStack()
                lifecycleScope.launchWhenStarted {

                    Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                        ImageSendFragmentDirections.actionImageViewerFragmentToCameraFragment(0.toString(), 256))
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
private fun turnOnAlertDialog(){
    alertDialog = AlertDialog.Builder(requireContext()).run {
        setTitle("")
        setMessage("")
        setNegativeButton("") { _, _ ->
            alertDialog.dismiss()
            lifecycleScope.launchWhenStarted {
                Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
                    ImageSendFragmentDirections.actionImageViewerFragmentToCameraFragment(0.toString(), 256))
            }
        }
        create()
    }
    alertDialog.show()
}
    companion object {
        var isFinished=false
    }

    override fun onStateChanged(id: Int, state: TransferState?) {
        when(state){
            TransferState.COMPLETED -> {
                val imageUrl = "https://maple-lie-detector.s3.ap-northeast-2.amazonaws.com/photo/"+file.name
                HttpConnect(answerPageBinding,"https://maple-solver.vercel.app/api/solver",imageUrl, file.extension).sendHttp()
            }
//            TransferState.WAITING
//            TransferState.IN_PROGRESS
//            TransferState.PAUSED
//            TransferState.RESUMED_WAITING
//            TransferState.CANCELED
//            TransferState.FAILED
//            TransferState.WAITING_FOR_NETWORK
//            TransferState.PART_COMPLETED
//            TransferState.PENDING_CANCEL
//            TransferState.PENDING_PAUSE
//            TransferState.PENDING_NETWORK_DISCONNECT
//            TransferState.UNKNOWN
        }
        Log.d("filePath",args.filePath)
    }

    override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
        Log.d("filePath",args.filePath)
    }

    override fun onError(id: Int, ex: Exception?) {
        Log.d("filePath",args.filePath)
    }
}
